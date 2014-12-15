(ns deraen.boot-less
  {:boot/export-tasks true}
  (:require
   [clojure.java.io :as io]
   [boot.pod        :as pod]
   [boot.core       :as core]
   [boot.util       :as util]
   [boot.tmpdir     :as tmpd]))

(def ^:private deps
  '[[slingshot "0.12.1"]])

(core/deftask less
  "Compile Less code."
  []
  (let [tmp         (core/temp-dir!)
        p           (-> (core/get-env)
                        (update-in [:dependencies] into deps)
                        pod/make-pod
                        future)
        last-less   (atom nil)]
    (core/with-pre-wrap fileset
      (let [less (->> fileset
                      (core/fileset-diff @last-less)
                      core/input-files
                      (core/by-ext [".less"]))]
        (reset! last-less fileset)
        (when (seq less)
          (util/info (str "Compiling {less}..." (count less) " changed files."))
          (let [main-files (->> fileset
                                core/input-files
                                (core/by-ext [".less"])
                                (core/not-by-re [#"_.*"]))]
            (doseq [f main-files]
              (pod/with-call-in
                @p
                (deraen.boot-less.impl/less-compile
                  ~(.getPath (tmpd/file f))
                  ~(.getPath tmp)
                  ~(tmpd/path f))))))
        (-> fileset
            (core/add-resource tmp)
            core/commit!)))))
