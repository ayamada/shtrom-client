(ns shtrom.client.util
  (:import [java.nio ByteBuffer ByteOrder]))

(defn ^ByteBuffer gen-byte-buffer
  ([]
     (gen-byte-buffer 8))
  ([size]
     (.order (ByteBuffer/allocate size) ByteOrder/BIG_ENDIAN)))

(defn str->int [str]
  (try
    (Integer. (re-find  #"\d+" str))
    (catch Exception e 0)))
