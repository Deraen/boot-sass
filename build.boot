(set-env!
  :resource-paths #{"src"}
  :dependencies   '[[org.clojure/clojure "1.6.0"       :scope "provided"]
                    [boot/core           "2.0.0-rc3"   :scope "provided"]
                    [adzerk/bootlaces    "0.1.8"       :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.2.0")

(bootlaces! +version+)

(task-options!
  pom {:project     'deraen/boot-less
       :version     +version+
       :description "Boot task to compile Less code to Css. Uses LESS.js through Nashorn."
       :url         "https://github.com/deraen/boot-less"
       :scm         {:url "https://github.com/deraen/boot-less"}
       :license     {:name "Eclipse Public License"
                     :url  "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask dev
  "Dev process"
  []
  (comp
    (watch)
    (repl :server true)
    (pom)
    (jar)
    (install)))
