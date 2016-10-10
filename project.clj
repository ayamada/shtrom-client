(defproject shtrom-client "0.1.0-SNAPSHOT"
  :description "A client library for shtrom (a histogram data store that is specialized for short read coverage)"
  :url "https://github.com/chrovis/shtrom-client"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/tools.logging "0.3.1"]
                 [clj-http "3.3.0"]
                 [slingshot "0.12.2"]
                 [cheshire "5.6.3"]]
  :resource-paths []
  :profiles {:dev {:resource-paths ["resources"]
                   :dependencies [[org.clojure/clojure "1.8.0"]]}
             :test {:resource-paths ["test-resources"]
                    :dependencies [[midje "1.8.3"]
                                   [shtrom "0.1.0-SNAPSHOT"]]
                    :plugins [[lein-cloverage "1.0.6"]
                              [lein-midje "3.2"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  :jar-exclusions [#".+?\.config\.clj"])
