(ns shtrom.client
  (:require [clj-http.client :as client]
            [clj-http.conn-mgr :as manager]
            [clojure.tools.logging :as logging]
            [shtrom.client.util :refer [gen-byte-buffer str->int]]
            [cheshire.core :as cheshire]
            [shtrom.client.config :as config])
  (:import [java.nio ByteBuffer]))

(def ^:private default-config-filename "shtrom-client.config.clj")

(def connection-manager (atom nil))

(defn shtrom-init
  ([]
   (shtrom-init default-config-filename))
  ([f]
   (config/load-config f)
   (when-not @connection-manager
     (reset! connection-manager
             (if config/connection
               (manager/make-reusable-conn-manager config/connection)
               (manager/make-reusable-conn-manager {}))))
   nil))

(defn shtrom-destroy
  []
  (when @connection-manager
    (manager/shutdown-manager @connection-manager)
    (reset! connection-manager nil))
  nil)

(defn hist-uri
  ([key]
   (format "%s/%s" config/uri-root key))
  ([key ref bin-size]
   (format "%s/%s/%s/%d" config/uri-root key ref bin-size)))

(defmacro wrap-error
  [& f]
  `(try
     ~@f
     (catch Exception e#
       (if-let [body#
                (-> e#
                    ex-data
                    :body
                    (as-> x# (when x# (cheshire/parse-string x# true)))
                    (as-> x# (if-let [state# (get-in x# [:error :state])]
                              (assoc-in x# [:error :state] (keyword state#))
                              x#)))]
         (throw (ex-info (print-str (:error body#)) (assoc body# :underlying e#)))
         (throw e#)))))

(defn validate-position
  [val]
  (if (neg? val)
    (long 0)
    (long val)))

(defn load-hist
  [key ref bin-size start end]
  (try
    (wrap-error
     (let [res (client/get (hist-uri key ref bin-size)
                           {:query-params {:start (validate-position start)
                                           :end (validate-position end)}
                            :as :byte-array
                            :accept :byte-array
                            :connection-manager @connection-manager})
           len (alength (:body res))
           bb (when res
                (ByteBuffer/wrap (:body res)))
           left (.getLong bb)
           right (.getLong bb)
           values (map (fn [_] (.getInt bb))
                       (range (quot (- len 16) 4)))]
       [left right values]))
    (catch java.io.IOException e
      (logging/error "Lost shtrom connection")
      nil)
    (catch Exception e [0 0 (list)])))

(defn save-hist
  [key ref bin-size values]
  (let [len (* 4 (count values))
        bb (doto (gen-byte-buffer len)
             (.limit len))]
    (when (zero? len)
      (throw (RuntimeException. "Empty values")))
    (doseq [v values]
      (.putInt bb v))
    (.position bb 0)
    (try
      (wrap-error
       (client/post (hist-uri key ref bin-size)
                    {:body (.array bb)
                     :content-type "application/octet-stream"
                     :connection-manager @connection-manager}))
      (catch java.io.IOException e
        (logging/error "Lost shtrom connection")
        nil))
    nil))

(defn reduce-hist
  [key ref bin-size]
  (try
    (wrap-error
     (let [res (client/post (str (hist-uri key ref bin-size) "/reduction")
                            {:throw-exceptions false
                             :connection-manager @connection-manager})]
       (cond
         (= (:status res) 404) (throw (RuntimeException. (format "Invalid key, ref or bin-size: %s %s %d" key ref bin-size))))))
    (catch java.io.IOException e
      (logging/error "Lost shtrom connection")))
  nil)

(defn create-bucket!
  [key]
  (wrap-error
   (client/post (hist-uri key)
                {:connection-manager @connection-manager})))

(defn build-bucket!
  [key]
  (wrap-error
   (client/put (hist-uri key)
               {:connection-manager @connection-manager})))

(defn delete-hist
  [key]
  (try
    (wrap-error
     (client/delete (hist-uri key)
                    {:connection-manager @connection-manager}))
    (catch java.io.IOException e
      (logging/error "Lost shtrom connection")))
  nil)
