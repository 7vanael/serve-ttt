(defproject serve-ttt "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;call this main instead of core?
  :main serve-ttt.main
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [kristi/server "1.0-SNAPSHOT"]
                 [tic-tac-toe "0.1.0-SNAPSHOT"]
                 [com.github.seancorfield/next.jdbc "1.3.1002"]
                 [org.postgresql/postgresql "42.6.0"]]
  :profiles {:dev {:dependencies [[speclj "3.3.2"]]}}
  :plugins [[speclj "3.3.2"]]
  :test-paths ["spec"])
