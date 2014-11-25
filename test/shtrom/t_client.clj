(ns shtrom.t-client
  (:use [midje.sweet])
  (:require [shtrom.client :as client]))

(def test-key "0")
(def test-ref "test")
(def test-bin-size 64)
(def test-values [345 127 493 312])

(defn test-shtrom-init
  []
  (client/shtrom-init "test.shtrom-client.config.clj"))

(def long-test-refs ["test-long-a" "test-long-b" "test-long-c" "test-long-d" "test-long-e" "test-long-f" "test-long-g" "test-long-h"])
(def max-value 128)
(def long-test-values (take 100000 (repeatedly #(rand-int max-value))))

(def ^:private bin-sizes [64
                          128
                          256
                          512
                          1024
                          2048
                          4096
                          8192
                          16384
                          32768
                          65536
                          131072
                          262144
                          524288])

(defn concurrent-reduce
  [key refs bin-size initial-values]
  (doseq [r refs]
    (client/save-hist key r bin-size initial-values))
  (doall
   (pmap
    (fn [r]
      (doseq [s bin-sizes]
        (client/reduce-hist key r s)))
    refs))
  nil)

(with-state-changes [(before :facts (test-shtrom-init))]
  (fact "save/load/reduce histogram"
    (client/save-hist test-key test-ref test-bin-size []) => (throws RuntimeException "Empty values")
    (client/save-hist test-key test-ref test-bin-size test-values) => nil
    (client/load-hist "not" "found" test-bin-size 0 256) => [0 0 (list)]
    (client/load-hist test-key test-ref test-bin-size 0 256) => [0 256 test-values]
    (client/reduce-hist "not" "found" test-bin-size) => (throws RuntimeException #"Invalid key, ref or bin-size")
    (client/reduce-hist test-key test-ref test-bin-size) => nil
    (client/delete-hist test-key) => nil))

(with-state-changes [(before :facts (test-shtrom-init))]
  (fact "concurrently reduce histogram"
    (concurrent-reduce test-key long-test-refs test-bin-size long-test-values) => nil
    (client/delete-hist test-key) => nil))
