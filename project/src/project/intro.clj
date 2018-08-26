(ns project.into
  (:import java.io.File
           (java.util Date UUID)))

(def file (File. "/Users/ivan/.emacs"))

(def date (new Date))

(def uuid (UUID/randomUUID))

(defn epoch
  []
  (quot (System/currentTimeMillis) 1000))

(def file? (partial instance? java.io.File))
