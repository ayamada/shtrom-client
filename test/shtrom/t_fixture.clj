(ns shtrom.t-fixture)

(def test-client-config-filename "test.shtrom-client.config.clj")
(def test-server-config-filename "test.shtrom.config.clj")

(def client-key "0")
(def client-ref "test")
(def client-bin-size 64)
(def client-values [345 127 493 312])
(def client-values-first [(first client-values)])

(def client-long-refs ["test-long-a" "test-long-b" "test-long-c" "test-long-d" "test-long-e" "test-long-f" "test-long-g" "test-long-h"])
(def client-max-value 128)
(def client-long-values (take 100000 (repeatedly #(rand-int client-max-value))))

(def bin-sizes [64
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
