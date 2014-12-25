(ns deraen.boot-less.impl
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [slingshot.slingshot :refer [throw+ try+]])
  (:import
    [org.webjars WebJarAssetLocator]
    [java.net URL JarURLConnection]
    [javax.script ScriptEngineManager ScriptEngine ScriptContext Bindings ScriptException]
    [jdk.nashorn.api.scripting ScriptObjectMirror JSObject]))

(def less-js "deraen/boot_less/less-rhino-1.7.2.js")
(def lessc-js "deraen/boot_less/lessc.js")

;; From lein-less
(defn- get-class [class-name]
  (try (Class/forName class-name)
       (catch ClassNotFoundException _ nil)))

(defmacro dynamic-instance?
  "Given a class name, attempts a dynamic lookup of the class, and if found does an instance? test against the object."
  [class-name obj]
  `(some-> (get-class ~class-name) (instance? ~obj)))

;; From lein-less
(defn error!
  [error message]
  (let [cause (when (instance? Throwable error) (.getCause ^Throwable error))]
    (cond
      (not (instance? Throwable error))
      (throw+ {:type :less-error :message (str message)})

      (= (:type (meta error)) :less-error)
      (throw error)

      (instance? ScriptException error)
      (if cause
        (recur cause message)
        (throw+ {:type :less-error :message (str message) :original error}))

      (or (dynamic-instance? "jdk.nashorn.api.scripting.NashornException" error)
          (dynamic-instance? "sun.org.mozilla.javascript.internal.JavaScriptException" error))
      (throw+ {:type :less-error :message (str message) :original error})

      (or (dynamic-instance? "org.mozilla.javascript.WrappedException" error)
          (dynamic-instance? "sun.org.mozilla.javascript.internal.WrappedException" error))
      (recur (.getWrappedException error) message)

      :default (throw error))))

(def ^:private ^ScriptEngineManager engine-manager (ScriptEngineManager.))

(defn create-engine []
  (.getEngineByName engine-manager "nashorn"))

(defn eval!
  [engine js-expression]
  (try
    (.eval engine js-expression)
    (catch Exception e
      (error! e (.getMessage e)))))

(defn eval-file!
  [engine resource]
  (let [resource-name (if resource (.getPath resource))
        reader (io/reader resource)
        bindings (.getBindings engine ScriptContext/ENGINE_SCOPE)]
    (try
      (if resource-name
        (.put bindings ScriptEngine/FILENAME resource-name))
      (.eval engine reader)
      (catch Exception e
        (error! e (.getMessage e)))
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
  (string/replace path #"\.[^\.]+$" (str "." new-ext)))

(defn less-compile [path target-dir relative-path]
  (js-engine)
  (let [output-file (io/file target-dir (change-file-ext relative-path "css"))]
    (io/make-parents output-file)
    (try+
      (eval! @stored-engine (format "lessc.compile('%s', '%s');" path output-file))
      (catch [:type :less-error] {:keys [message] :as e}
        (println message)))))

(defn nashorn->clj [obj]
  (into {} (map (fn [k]
                  [(keyword k) (.get obj k)])
                (.getOwnKeys obj true))))

(defrecord Import [path parent])

(defn find-local-file [file current-dir]
  (let [f (io/file current-dir file)]
    (when (.exists f)
      (map->Import {:path (.getPath f)
                    :parent (.getParent f)}))))

(defn- get-parent [url]
  (string/replace url #"/[^/]*$" ""))

(defn- jar-url-parent [url]
  (let [^JarURLConnection jar-url (.openConnection url)]
    (get-parent (.getEntryName jar-url))))

(defn find-resource [file current-dir]
  (when-let [url (or (io/resource file) (io/resource (str current-dir "/" file)))]
    (if (= (.getProtocol url) "jar")
      (map->Import {:path (.toString url)
                    ; As parent, use the path inside jar
                    ; e.g. instead of jar:file:.../bootstrap-3.3.1.jar!/META-INF/resources/webjars/bootstrap/3.3.1/less
                    ; just META-INF/resources/webjars/bootstrap/3.3.1/less
                    ; then files referenced by this file can be found easily
                    :parent (jar-url-parent url)}))))

; Source: https://github.com/cljsjs/boot-cljsjs/blob/master/src/cljsjs/impl/webjars.clj

(def ^:private webjars-pattern
  #"META-INF/resources/webjars/([^/]+)/([^/]+)/(.*)")

(defn- asset-path [resource]
  (let [[_ name version path] (re-matches webjars-pattern resource)]
    (str name "/" path)))

; FIXME: Singleton? :<
(def ^:private asset-map
  (delay (->> (.listAssets (WebJarAssetLocator.) "")
              (map (juxt asset-path identity))
              (into {}))))

(defn find-webjars [file current-dir]
  (if-let [path (get @asset-map file)]
    (find-resource path nil)))

(defn find-import [file current]
  (let [{current-dir :currentDirectory} (nashorn->clj current)]
    (or (find-local-file file current-dir)
        (find-resource file current-dir)
        (find-webjars file current-dir))))
