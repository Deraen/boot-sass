(ns deraen.boot-less.impl
  (:require
    [clojure.java.io :as io]
    [clojure.string :as s]
    [slingshot.slingshot :refer [throw+ try+]])
  (:import
    [java.net URL]
    [javax.script ScriptEngineManager ScriptEngine ScriptContext Bindings]
    [jdk.nashorn.api.scripting ScriptObjectMirror JSObject]))

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
    (io/make-parents output-file)
    (eval! @stored-engine (format "lessc.compile('%s', '%s');" path output-file))))

(defn error! [error message]
  (println error message))

(defn nashorn->clj [obj]
  (into {} (map (fn [k]
                  [(keyword k) (.get obj k)])
                (.getOwnKeys obj true))))

(defrecord Import [path])

(defn load-local-file [file current-dir]
  (let [f (io/file current-dir file)]
    (if (.exists f)
      (map->Import {:path (.getAbsolutePath f)}))))

(defn load-webjars [_ _]
  nil)

(defn load-resource [file _]
  (if-let [r (io/resource file)]
    (map->Import {:path (.toString r)})))

(defn load-in-jar-file
  "E.g. variables.less, jar:file:/home/juho...bootstrap.jar!META-INT/..."
  [file current-dir]
  (try
    ; Throws a exception if file is not found on the jar
    ; But creates inputstream if found. But nothing is read from stream?
    (.getContent (URL. (str current-dir "/" file)))
    (map->Import {:path (str current-dir "/" file)})
    (catch Throwable _
      nil)))

(defn find-import [file current]
  (let [{:keys [currentDirectory]} (nashorn->clj current)]
    (or (load-local-file file currentDirectory)
        (load-webjars file currentDirectory)
        (load-resource file currentDirectory)
        (load-in-jar-file file currentDirectory))))
