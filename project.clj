(defproject shtrom-client "0.1.0-SNAPSHOT"
  :description "client library for shtrom"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["snapshots" {:url "https://nexus.xcoo.jp/content/repositories/snapshots"}]
                 ["releases" {:url "https://nexus.xcoo.jp/content/repositories/releases"}]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [aleph "0.3.0"]]
  :plugins [[lein-midje "3.1.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}}
  :jar-exclusions [#".+?\.config\.clj"])
