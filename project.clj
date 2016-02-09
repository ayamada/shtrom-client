(defproject shtrom-client "0.1.0-SNAPSHOT"
  :description "A client library for shtrom (a histogram data store that is specialized for short read coverage)"
  :url "http://github.com/chrovis/shtrom-client"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/tools.logging "0.3.1"]
                 [clj-http-lite "0.3.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :test {:resource-paths ["test-resources"]
                    :dependencies [[midje "1.8.3"]]
                    :plugins [[lein-cloverage "1.0.6"]
                              [lein-midje "3.2"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  :jar-exclusions [#".+?\.config\.clj"])
