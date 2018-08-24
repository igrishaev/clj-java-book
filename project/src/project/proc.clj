(ns project.proc
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
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
      (.put env-map (kw->env key) (str val)))))


(defn ^Process
  proc-start

  [args & [{:keys [env path-out path-err] :as opt}]]

  (let [command (args->command args)
        builder (ProcessBuilder. command)]

    (when env
      (set-env builder env))

    (when path-out
      (. builder redirectOutput (io/file path-out)))

    (when path-err
      (. builder redirectError (io/file path-err)))

    (.start builder)))


(defn start-chrome
  []
  (let [port 9999
        args ["/Users/ivan/Downloads/chromedriver"
              (str "--port=" port)
              "--verbose"]
        path-out "./chrome-out.txt"
        path-err "./chrome-err.txt"]

    (proc-start args {:path-out path-out
                      :path-err path-err})))


(def base-url "http://127.0.0.1:9999")

(defn make-url [& args]
  (str/join "/" (into [base-url] args)))

(defn init-session
  []
  (-> (client/post
       (make-url "session")
       {:as :json
        :content-type :json
        :form-params
        {:desiredCapabilities {}}})
      :body
      :sessionId))

(defn goto-url
  [session url]
  (client/post
   (make-url "session" session "url")
   {:as :json
    :content-type :json
    :form-params {:url url}}))


(defn- find-element
  [session selector]
  (-> (client/post
       (make-url "session" session "element")
       {:as :json
        :content-type :json
        :form-params
        {:using "xpath" :value selector}})
      :body
      :value
      :ELEMENT))


(defn input-text
  [session selector text]
  (let [element (find-element session selector)]
    (client/post
     (make-url "session" session "element" element "value")
     {:as :json
      :content-type :json
      :form-params {:value (vec text)}})))


(defn click
  [session selector]
  (let [element (find-element session selector)]
    (client/post
     (make-url "session" session "element" element "click"))))


(defn delete-session
  [session]
  (client/delete
   (make-url "session" session)))


(defn stop-process
  [^Process p]
  (when (.isAlive p)
    (.destroy p)
    (.waitFor p)
    (println (.exitValue p))))


(defmacro with-process
  [[bind & params] & body]
  `(let [~bind (proc-start ~@params)]
     (try
       ~@body
       (finally
         (stop-process ~bind)))))


(comment
  (with-process
    [proc ["chromedriver" "-p" 9999] {:env {:debug 1}}]
    (let [session (init-session)]
      (goto-url session "http://exampple.com")
      ;; any other code
      (delete-session session))))
