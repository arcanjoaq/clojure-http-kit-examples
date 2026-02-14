(defproject clojure-http-kit-examples "1.0.0-SNAPSHOT"
  :description "Clojure http-kit Examples"
  :dependencies [[org.clojure/clojure "1.12.4"]
                 [cheshire "6.1.0"]
                 [http-kit/http-kit "2.8.1" :upgrade false]
                 [prismatic/schema "1.4.1"]
                 [camel-snake-kebab "0.4.3"]
                 [org.bouncycastle/bcpkix-jdk18on "1.83"]
                 [org.bouncycastle/bcprov-jdk18on "1.83"]]
  :main ^:skip-aot clojure-http-kit-examples.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[com.github.clojure-lsp/lein-clojure-lsp "1.4.2"]
                             [com.github.clj-kondo/lein-clj-kondo "2023.10.20"]
                             [lein-cloverage "1.2.4"]
                             [lein-ancient "0.7.0"]
                             [com.aphyr/prism "0.1.3"]]

                   :dependencies [[clj-test-containers "0.7.4"]
                                  [org.testcontainers/testcontainers "2.0.3"]

                                  [nubank/matcher-combinators "3.10.0"]
                                  [nubank/state-flow "5.20.2-beta.2"]
                                  [nubank/mockfn "0.7.0"]
                                  [clj-commons/spyscope "0.1.48"]
                                  [com.aphyr/prism "0.1.3"]]
                   :injections [(require 'spyscope.core)]

                   ;; tasks
                   :aliases {"diagnostics"     ["clojure-lsp" "diagnostics"]
                             "format"          ["clojure-lsp" "format" "--dry"]
                             "format-fix"      ["clojure-lsp" "format"]
                             "clean-ns"        ["clojure-lsp" "clean-ns" "--dry"]
                             "clean-ns-fix"    ["clojure-lsp" "clean-ns"]
                             "lint"            ["do" ["format"] ["clj-kondo-lint"] ["clean-ns"]]
                             "lint-fix"        ["do" ["format-fix"] ["clj-kondo-lint"] ["clean-ns-fix"]]
                             "clj-kondo-deps" ["with-profile" "+test" "clj-kondo" "--copy-configs" "--dependencies" "--parallel" "--lint" "$classpath"]
                             "clj-kondo-lint" ["do" ["clj-kondo-deps"] ["with-profile" "+test" "clj-kondo"]]}}})

