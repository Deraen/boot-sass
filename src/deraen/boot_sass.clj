(ns deraen.boot-sass
  {:boot/export-tasks true}
  (:require
   [clojure.java.io :as io]
   [boot.pod        :as pod]
   [boot.core       :as core]
   [boot.util       :as util]
   [boot.tmpdir     :as tmpd]))

(def ^:private deps
  '[[deraen/sass4clj "0.1.0-SNAPSHOT"]])

(defn by-pre
  [exts files & [negate?]]
  ((core/file-filter #(fn [f] (.startsWith (.getName f) %))) exts files negate?))

(defn- find-mainfiles [fs]
  (by-pre ["_"]
          (->> fs
               core/input-files
               (core/by-ext [".scss"]))
          true))

(core/deftask sass
  "Compile Sass code."
  []
  (let [output-dir  (core/tmp-dir!)
        p           (-> (core/get-env)
                        (update-in [:dependencies] into deps)
                        pod/make-pod
                        future)
        prev        (atom nil)]
    (core/with-pre-wrap fileset
      (let [sources (->> fileset
                         (core/fileset-diff @prev)
                         core/input-files
                         (core/by-ext [".scss"]))]
        (reset! prev fileset)
        (when (seq sources)
          (util/info "Compiling {sass}... %d changed files.\n" (count sources))
          (doseq [f (find-mainfiles fileset)]
            (pod/with-call-in @p
              (sass4clj.core/sass-compile-to-file
                ~(.getPath (tmpd/file f))
                ~(.getPath output-dir)
                ~(tmpd/path f)
                {:verbosity ~(deref util/*verbosity*)})))))
        (-> fileset
            (core/add-resource output-dir)
            core/commit!))))
