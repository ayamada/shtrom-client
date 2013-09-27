(ns shtrom.client.core
  (:require [clj-http.client :as client]
            [shtrom.client.util :refer [gen-byte-buffer str->int]]))

(declare uri-root)

(defn init
  []
  (let [config (read-string (slurp "config/shtrom-client.config.clj"))]
    (def uri-root (:uri-root config))))

(defn hist-uri
  [key ref binsize]
  (format "%s/%s/%s/%d" uri-root key ref binsize))

(defn load-hist
  [key ref binsize start end]
  (let [res (client/get (hist-uri key ref binsize)
                     {:query-params {:start start :end end}
                      :as :byte-array})
        len (-> (get (:headers res) "content-length")
                str->int)
        bytes (:body res)
        bb (doto (gen-byte-buffer len)
             (.limit len)
             (.put bytes 0 len)
             (.position 0))]
    (map (fn [_] (.getInt bb))
         (range (quot len 4)))))
