(ns project.io
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clojure.java.jdbc :as jdbc]
            [clj-http.client :as client])
  (:import java.util.regex.Pattern
           org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
           org.apache.commons.compress.archivers.ArchiveEntry
           org.jsoup.Jsoup))


(def files-page "http://download.cms.gov/nppes/NPI_Files.html")


(defn find-url
  []
  (let [doc (.get (Jsoup/connect files-page))
        selector "a[href~=NPPES_Data_Dissemination_\\w+_\\d{4}\\.zip]"
        links (.select doc selector)]

    (some-> links
            first
            (.absUrl "href"))))


(defn get-file-stream
  [url]
  (:body (client/get url {:as :stream})))


(defn ->zip-stream
  [stream]
  (new ZipArchiveInputStream stream))


(def re-csv #"(?i)npidata_pfile_\d{8}-\d{8}\.csv$")


(defn- seek-stream
  [^ZipArchiveInputStream stream ^Pattern re]
  (loop []
    (when-let [^ArchiveEntry entry (.getNextEntry stream)]
      (let [filename (.getName entry)]
        (if (re-find re filename)
          {:name (.getName entry)
           :size (.getSize entry)
           :dir? (.isDirectory entry)}
          (recur))))))


(defn clean-header-field
  [field]
  (-> field
      str/trim
      str/lower-case
      (str/replace #"[^a-z0-9 _]" "")
      (str/replace #"\s+" "_")
      keyword))


(defn clean-row-field
  [field]
  (when-not (or (= field "") (= field "<UNAVAIL>"))
    field))


(defn read-csv
  [stream]
  (let [reader (io/reader stream)
        rows (csv/read-csv reader)
        header (map clean-header-field (first rows))]
    (for [row (rest rows)]
      (zipmap header (map clean-row-field row)))))


(defn ->model
  [row]
  (select-keys
   row [:npi
        :entity-type-code
        :provider-first-name
        :provider-credential-text
        ;; other fields...
        ]))


(defn by-chunks
  [coll n]
  (partition n n [] coll))


(def db
  {:dbtype "postgresql"
   :dbname "clj-db"
   :host "127.0.0.1"
   :user "clj-user"
   :password "clj-pass"})


(def insert-multi! (partial jdbc/insert-multi! db))


(defn get-models
  []
  (let [file-url (find-url)
        stream-bin (get-file-stream file-url)
        ztream-zip (->zip-stream stream-bin)
        entry (seek-stream ztream-zip re-csv)]

    (assert entry (format "file %s not found" re-csv))

    (let [rows (read-csv ztream-zip)]
      (map ->model rows))))


(defn save-models
  [models]
  (doseq [chunk (by-chunks models 1000)]
    (insert-multi! :models chunk)))
