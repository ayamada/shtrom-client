(ns shtrom.t-client
  (:require [midje.sweet :refer :all]
            [shtrom.client :as client]))

(def test-config-filename "test.shtrom-client.config.clj")

(fact "validate-position"
  (client/validate-position nil) => (throws Throwable)
  (client/validate-position 0) => 0
  (client/validate-position 1) => 1
  (client/validate-position -1) => 0
  (client/validate-position 99999) => 99999
  (client/validate-position -99999) => 0
  (instance? Long (client/validate-position 123)) => truthy)

(fact "shtrom-init"
  (client/shtrom-init test-config-filename) => anything
  client/host => "localhost"
  client/port => 13001
  client/uri-root => "http://localhost:13001")

(with-state-changes [(before :facts (client/shtrom-init test-config-filename))]
  (fact "hist-uri"
    (client/hist-uri "test-key") => "http://localhost:13001/test-key"
    (client/hist-uri "test-key" "test-ref" 64) => "http://localhost:13001/test-key/test-ref/64")
  (fact "load-hist"
    (client/load-hist "test-key" "test-ref" 64 0 128) => nil)
  (fact "save-hist"
    (client/save-hist "test-key" "test-ref" 64 []) => (throws RuntimeException)
    (client/save-hist "test-key" "test-ref" 64 [0 1 2 3]) => nil)
  (fact "reduce-hist"
    (client/reduce-hist "test-key" "test-ref" 64) => nil)
  (fact "delete-hist"
    (client/delete-hist "test-key") => nil))

;;; More tests which required shtrom server are available on <https://github.com/chrovis/shtrom>
