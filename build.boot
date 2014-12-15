(set-env!
  :resource-paths #{"src"}
  :asset-paths #{"resources"}
  :dependencies   '[[org.clojure/clojure "1.6.0"       :scope "provided"]
                    [boot/core           "2.0.0-pre28" :scope "provided"]
                    [adzerk/bootlaces    "0.1.5"       :scope "test"]
                    [com.keminglabs/cljx "0.4.0"       :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.0-SNAPSHOT")

(bootlaces! +version+)

(task-options!
  pom {:project     'deraen/boot-less
       :version     +version+
       :description "Boot task to compile Less code to Css. Uses LESS.js through Nashorn."
       :url         "https://github.com/deraen/boot-less"
       :scm         {:url "https://github.com/deraen/boot-less"}
       :license     {:name "The MIT License (MIT)"
                     :url "http://opensource.org/licenses/mit-license.php"}})

(deftask dev
  "Dev process"
  []
  (comp
    (watch)
    (repl :server true)
    (pom)
    (jar)
    (install)))
