(ns project.proc
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [cheshire.core :as json])
  (:import (java.lang ProcessBuilder Process)
           java.util.Map
           java.io.File))


(defn ^"[Ljava.lang.String;"
  args->command
  [args]
  (into-array String (map str args)))


(defn kw->env
  [kw]
  (-> kw
      name
      (str/replace "-" "_")
      (str/upper-case)))


(defn set-env
  [^ProcessBuilder builder env]
  (let [^Map env-map (.environment builder)]
    (doseq [[key val] env]
      (.put env-map (kw->env) (str val)))))


(defn ^Process
  proc-start

  [args & [{:keys [env path-out path-err] :as opt}]]

  (let [command (args->command args)
        builder (ProcessBuilder. command)]

    (when env
      (set-env builder env))

    (when path-out
      (.redirectOutput builder (File. path-out)))

    (when path-err
      (.redirectError​ builder (File. path-err)))

    (.start builder)))


(defn start-chrome
  []
  (let [port 9999
        args ["/Users/ivan/Download/chromedriver"
              (str "--port=" port)
              "--verbose"]
        path-out "./chrome-out.txt"
        path-err "./chrome-out.err"]

    (proc-start args {:path-out path-out
                      :path-err path-err})))
