(ns shtrom.client.t-util
  (:require [midje.sweet :refer :all]
            [shtrom.client.util :as util])
  (:import [java.nio ByteBuffer]))

(fact "gen-byte-buffer"
  (instance? ByteBuffer (util/gen-byte-buffer)) => truthy)

(fact "str->int"
  (util/str->int nil) => 0
  (util/str->int "123") => 123
  (util/str->int "abc") => 0)
