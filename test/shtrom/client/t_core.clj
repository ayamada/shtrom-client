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

(with-state-changes [(before :facts (test-shtrom-init))]
  (fact "save/load/reduce histogram"
        (core/save-hist test-key test-ref test-bin-size []) => (throws RuntimeException "Empty values")
        (core/save-hist test-key test-ref test-bin-size test-values) => nil
        (core/load-hist "not" "found" test-bin-size 0 256) => [0 0 (list)]
        (core/load-hist test-key test-ref test-bin-size 0 256) => [0 256 test-values]
        (core/reduce-hist "not" "found" test-bin-size) => (throws RuntimeException #"Invalid key, ref or bin-size")
        (core/reduce-hist test-key test-ref test-bin-size) => nil))
