(defproject rizk "0.1.0-SNAPSHOT"
  :description "A Clojure implementation of a Risk-like strategy game"
  :url "https://github.com/deejayessel/rizk"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ysera "2.0.1"]]
  :main ^:skip-aot rizk.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
