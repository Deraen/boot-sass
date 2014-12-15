(ns deraen.boot-less
  {:boot/export-tasks true}
  (:require
   [clojure.java.io :as io]
   [boot.pod        :as pod]
   [boot.core       :as core]
   [boot.util       :as util]
   [boot.tmpdir     :as tmpd]))

(def ^:private deps
  '[])

(core/deftask less
  "Compile Less code."
  []
  (let [rules       [:clj :cljs]
        tmp         (core/temp-dir!)
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
        (if (seq less)
          (do
            (util/info (str "Compiling {less}..." (count less) " changed files."))
            (doseq [r rules
                    f less]
              (pod/with-call-in @p
                (deraen.boot-less.impl/less-compile
                  ~r
                  ~(.getPath (tmpd/file f))
                  ~(.getPath tmp)
                  ~(tmpd/path f))))
            (-> fileset
                (core/add-resource tmp)
                core/commit!))
          fileset)))))
