(ns deraen.boot-less.impl
  (:require
    [clojure.java.io :as io]
    [clojure.string :as s]
    [slingshot.slingshot :refer [throw+ try+]])
  (:import (javax.script ScriptEngineManager ScriptEngine ScriptContext Bindings)))

(def less-js "deraen/boot_less/less-rhino-1.7.2.js")
(def lessc-js "deraen/boot_less/lessc.js")

(def ^:private ^ScriptEngineManager engine-manager (ScriptEngineManager.))

(defn create-engine []
  (.getEngineByName engine-manager "nashorn"))

(defn eval!
  [engine js-expression]
  (.eval engine js-expression))

(defn eval-file!
  [engine resource]
  (let [resource-name (if resource (.getPath resource))
        reader (io/reader resource)
        bindings (.getBindings engine ScriptContext/ENGINE_SCOPE)]
    (try
      (if resource-name
        (.put bindings ScriptEngine/FILENAME resource-name))
      (.eval engine reader)
      (finally (.remove bindings ScriptEngine/FILENAME)))))

(def ^:private stored-engine (atom nil))

(defn js-engine []
  (if (nil? @stored-engine)
    (let [engine (create-engine)]
      (eval-file! engine (io/resource less-js))
      (eval-file! engine (io/resource lessc-js))
      (reset! stored-engine engine)))
  @stored-engine)

(defn- change-file-ext [path new-ext]
  (s/replace path #"\.[^\.]+$" (str "." new-ext)))

(defn less-compile [path target-dir relative-path]
  (js-engine)
  (let [output-file (io/file target-dir (change-file-ext relative-path "css"))]
    (eval! @stored-engine (format "lessc.compile('%s', '%s');" path output-file))))
