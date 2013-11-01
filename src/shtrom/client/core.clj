(ns shtrom.client.core
  (:require [clojure.java.io :as io]
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
                  (slurp rsrc))]
       (def uri-root (:uri-root config)))))

(defn hist-uri
  [key ref binsize]
  (format "%s/%s/%s/%d" uri-root key ref binsize))

(defn- validate-position
  [val]
  (if (neg? val)
    (long 0)
    (long val)))

(defn load-hist
  [key ref binsize start end]
  (try
    (let [res (client/get (hist-uri key ref binsize)
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
    (catch Exception e [0 0 (list)])))

(defn save-hist
  [key ref binsize values]
  (let [len (* 4 (count values))
        bb (doto (gen-byte-buffer len)
             (.limit len))]
    (doseq [v values]
      (.putInt bb v))
    (.position bb 0)
    (client/post (hist-uri key ref binsize)
                 {:body (.array bb)})))

(defn reduce-hist
  [key ref binsize]
  (client/post (str (hist-uri key ref binsize) "/reduction")))
