(ns shtrom.client.perf
  (:require [shtrom.client :as client]
            [com.climate.claypoole :as cp]))

(def ref-name-prefix "ref")
(def bin-size 64)

(def shtrom-value-max 128)

(defn- random-data [length]
  (vec (repeatedly length #(rand-int shtrom-value-max))))

(defmacro elapsed-time
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr
         elapsed# (/ (double (- (. System (nanoTime)) start#)) 1000000.0)]
     {:ret ret#
      :elapsed-time elapsed#}))

(defn prepare!
  [k ref-length refs-num]
  (let [entries (mapv (fn [_]
                        (random-data ref-length))
                      (range refs-num))]
    (client/create-bucket! k)
    (let [{:keys [ret elapsed-time]}
          (elapsed-time (dotimes [i refs-num]
                          (let [ref-name (str ref-name-prefix i)
                                body (nth entries i)]
                            (client/save-hist k ref-name bin-size body))))]
      (println "Elapsed time(save-hist):" elapsed-time "msecs with" refs-num "refs")
      (println "Elapsed time(save-hist):" (/ elapsed-time (float refs-num)) "msecs / 1 ref"))
    (let [{:keys [ret elapsed-time]}
          (elapsed-time (dotimes [i refs-num]
                          (let [ref-name (str ref-name-prefix i)
                                body (nth entries i)]
                            (client/reduce-hist k ref-name bin-size))))]
      (println "Elapsed time(reduce-hist):" elapsed-time "msecs with" refs-num "refs")
      (println "Elapsed time(reduce-hist):" (/ elapsed-time (float refs-num)) "msecs / 1 ref"))
    (client/build-bucket! k)
    k))

(defn delete! [k]
  (client/delete-hist k)
  nil)


(defn load!
  [k ref-length refs-num]
  ;; Test reading performence of 'load-hist' API.
  (let [times (doall (pmap #(let [[start end] (sort [(rand-int ref-length)
                                                     (rand-int ref-length)])
                                  ref-name (str ref-name-prefix %)
                                  {:keys [ret elapsed-time]} (elapsed-time (client/load-hist k ref-name bin-size start end))]
                              (println "took" elapsed-time "ms to retrieve " (count ret) " from " start " to " end " in " ref-name " of " k)
                              elapsed-time)
                           (range refs-num)))]
    (let [n (count times)
          avg (/ (reduce + times) n)
          med (Math/sqrt (/ (reduce #(+ %1 (* (- %2 avg) (- %2 avg))) times) n))]
      (println "avg:" avg "msecs")
      (println "var:" med "msecs"))
    (println "max:" (apply max times) "msecs")
    (println "min:" (apply min times) "msecs")))

(defn init!
  []
  (client/shtrom-init))

(comment
  (require 'shtrom.client.perf :reload-all)
  (shtrom.client.perf/init!)
  (def k "perf-1")
  (def ref-length 100000)
  (def refs-num 200)
  (shtrom.client.perf/prepare! k ref-length refs-num)
  (shtrom.client.perf/load! k ref-length refs-num)
  (shtrom.client.perf/delete! k)
  )
