(ns shtrom.client.t-core
  (:use [midje.sweet])
  (:require [shtrom.client.core :as core]))

(def test-key "0")
(def test-ref "test")
(def test-bin-size 64)
(def test-values [345 127 493 312])

(defn test-shtrom-init
  []
  (core/shtrom-init "test.shtrom-client.config.clj"))

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
    (core/save-hist key r bin-size initial-values))
  (doall
   (pmap
    (fn [r]
      (doseq [s bin-sizes]
        (core/reduce-hist key r s)))
    refs))
  nil)
(with-state-changes [(before :facts (test-shtrom-init))]
  (fact "save/load/reduce histogram"
        (core/save-hist test-key test-ref test-bin-size []) => (throws RuntimeException "Empty values")
        (core/save-hist test-key test-ref test-bin-size test-values) => nil
        (core/load-hist "not" "found" test-bin-size 0 256) => [0 0 (list)]
        (core/load-hist test-key test-ref test-bin-size 0 256) => [0 256 test-values]
        (core/reduce-hist "not" "found" test-bin-size) => (throws RuntimeException #"Invalid key, ref or bin-size")
        (core/reduce-hist test-key test-ref test-bin-size) => nil))

(with-state-changes [(before :facts (test-shtrom-init))]
  (fact "concurrently reduce histogram"
        (concurrent-reduce test-key long-test-refs test-bin-size long-test-values) => nil))
