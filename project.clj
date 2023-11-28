(defproject zk "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"]
                 [org.clojure/data.json "2.4.0"]
                 ;[org.clojure/tools.logging "1.2.4"]
                ;[com.vladsch.flexmark/flexmark "0.64.8"]
                 [com.vladsch.flexmark/flexmark-ext-definition "0.64.8"]
                 [cybermonday "0.1-fork"] ; "0.6.213" ; com.kiranshila/
                 ]
  :main ^:skip-aot zk.core
  :target-path "target/%s"
  :profiles {:dev {}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
