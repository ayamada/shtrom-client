(ns shtrom.t-client
  (:require [midje.sweet :refer :all]
            [shtrom.client :as client]
            [shtrom.t-fixture :as t-fixture]))

(fact "validate-position"
  (client/validate-position nil) => (throws Throwable)
  (client/validate-position 0) => 0
  (client/validate-position 1) => 1
  (client/validate-position -1) => 0
  (client/validate-position 99999) => 99999
  (client/validate-position -99999) => 0
  (instance? Long (client/validate-position 123)) => truthy)

(fact "config file not found"
  (client/shtrom-init) => (throws java.lang.RuntimeException))

(fact "shtrom-init"
  (client/shtrom-init t-fixture/test-client-config-filename) => anything
  client/host => "localhost"
  client/port => 13001
  client/uri-root => "http://localhost:13001")
