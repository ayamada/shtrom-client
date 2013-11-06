(ns shtrom.client.core
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as logging]
            [clj-http.client :as client]
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
       (def uri-root (:uri-root conf)))))

(defn hist-uri
  [key ref bin-size]
  (format "%s/%s/%s/%d" uri-root key ref bin-size))

(defn- validate-position
  [val]
  (if (neg? val)
    (long 0)
    (long val)))

(defn load-hist
  [key ref bin-size start end]
  (try
    (let [res (client/get (hist-uri key ref bin-size)
                          {:query-params {:start (validate-position start)
                                          :end (validate-position end)}
                           :as :byte-array})
          len (-> (get (:headers res) "content-length")
                  str->int)
          bytes (:body res)
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
      (logging/warn "Lost shtrom connection")
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
      (client/post (hist-uri key ref bin-size)
                   {:body (.array bb)})
      (catch java.net.ConnectException e
        (logging/warn "Lost shtrom connection")
        nil))
    nil))

(defn reduce-hist
  [key ref bin-size]
  (try
    (client/post (str (hist-uri key ref bin-size) "/reduction"))
    (catch java.net.ConnectException e
      (logging/warn "Lost shtrom connection")
      nil)
    (catch Exception e
      (throw (RuntimeException. (format "Invalid key, ref or bin-size: %s %s %d" key ref bin-size)))))
  nil)
