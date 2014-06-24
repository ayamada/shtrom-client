(defproject shtrom-client "0.1.0-SNAPSHOT"
  :description "client library for shtrom"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.0"]
                 [aleph "0.3.2"]]
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}}
  :jar-exclusions [#".+?\.config\.clj"])
