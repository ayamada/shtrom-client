(ns shtrom.client.config
  (:require [clojure.java.io :as io]))

(declare host port uri-root connection)

(defn load-config
  [f]
  (let [rsrc (let [f-classpath (io/resource f)
                   f-etc (io/file (str "/etc/" f))]
               (cond
                 (not (nil? f-classpath)) f-classpath
                 (.isFile f-etc) f-etc
                 :else nil))
        conf (if (nil? rsrc)
               (throw (RuntimeException. (str "Configuration file not found: " f)))
               (read-string (slurp rsrc)))]
    (intern 'shtrom.client.config 'host (:host conf))
    (intern 'shtrom.client.config 'port (:port conf))
    (intern 'shtrom.client.config 'uri-root (str "http://" (:host conf) ":" (:port conf)))
    (intern 'shtrom.client.config 'connection (:connection conf))))
