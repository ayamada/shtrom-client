(ns shtrom.client
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as logging]
            [aleph.http :refer [http-request]]
            [shtrom.client.util :refer [gen-byte-buffer str->int]]))

(declare uri-root)

(def ^:private default-config-filename "shtrom-client.config.clj")

(defn shtrom-init
  ([]
     (shtrom-init default-config-filename))
  ([f]
     (let [rsrc (io/resource f)
           conf (if (nil? rsrc)
                  (throw (RuntimeException. (str "Configuration file not found: " f)))
                  (read-string (slurp rsrc)))]
       (intern 'shtrom.client 'uri-root (:uri-root conf)))))

(defn hist-uri
  ([key]
     (format "%s/%s" uri-root key))
  ([key ref bin-size]
     (format "%s/%s/%s/%d" uri-root key ref bin-size)))

(defn- validate-position
  [val]
  (if (neg? val)
    (long 0)
    (long val)))

(defn load-hist
  [key ref bin-size start end]
  (try
    (let [res @(http-request {:url (format "%s?start=%d&end=%d"
                                           (hist-uri key ref bin-size)
                                           (validate-position start)
                                           (validate-position end))
                              :method :get})
          len (-> (:headers res)
                  (get "content-length")
                  str->int)
          bytes (.array (:body res))
          bb (doto (gen-byte-buffer len)
               (.limit len)
               (.put bytes 0 len)
               (.position 0))
          left (.getLong bb)
          right (.getLong bb)
          values (map (fn [_] (.getInt bb))
                      (range (quot (- len 16) 4)))]
      [left right values])
    (catch java.net.ConnectException e
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
      @(http-request {:url (hist-uri key ref bin-size)
                      :method :post
                      :body (.array bb)})
      (catch java.net.ConnectException e
        (logging/error "Lost shtrom connection")
        nil))
    nil))

(defn reduce-hist
  [key ref bin-size]
  (try
    (let [res @(http-request {:url (str (hist-uri key ref bin-size) "/reduction")
                              :method :post})]
      (cond
       (= (:status res) 404) (throw (RuntimeException. (format "Invalid key, ref or bin-size: %s %s %d" key ref bin-size)))))
    (catch java.net.ConnectException e
      (logging/error "Lost shtrom connection")))
  nil)

(defn delete-hist
  [key]
  (try
    @(http-request {:url (hist-uri key)
                    :method :delete})
    (catch java.net.ConnectException e
      (logging/error "Lost shtrom connection")))
  nil)
