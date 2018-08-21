(ns project.db2
  (:require
   [clojure.java.jdbc :as jdbc]
   [cheshire.core :as json])
  (:import org.postgresql.util.PGobject
           org.postgresql.jdbc.PgArray))


(def db
  {:dbtype "postgresql"
   :dbname "clj-db"
   :host "127.0.0.1"
   :user "clj-user"
   :password "clj-pass"})


(def query (partial jdbc/query db))

(def insert! (partial jdbc/insert! db))

(def execute! (partial jdbc/execute! db))


(extend-protocol jdbc/ISQLValue

  java.net.URL
  (sql-value [url]
    (str url))

  java.util.UUID
  (sql-value [uuid]
    (str uuid)))


(extend-protocol jdbc/ISQLValue

  java.util.Date
  (sql-value [val]
    (java.sql.Timestamp. (.getTime val))))


(defn ->pgobject
  [type value]
  (doto (PGobject.)
    (.setType type)
    (.setValue value)))


(def ->color (partial ->pgobject "type_color"))

(def enum-R (->color "red"))
(def enum-G (->color "green"))
(def enum-B (->color "blue"))


(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentMap
  (sql-value [val]
    (->pgobject "json" (json/generate-string val))))


(defmulti pgobj->clj
  (fn [pgobj]
    (.getType pgobj)))


(defn- json->clj
  [pgobj]
  (-> pgobj .getValue (json/parse-string true)))


(defmethod pgobj->clj "json"
  [pgobj]
  (json->clj pgobj))


(defmethod pgobj->clj "jsonb"
  [pgobj]
  (json->clj pgobj))


(extend-protocol jdbc/IResultSetReadColumn

  PGobject
  (result-set-read-column [pgobj metadata index]
    (pgobj->clj pgobj)))

(extend-protocol jdbc/IResultSetReadColumn

  PgArray
  (result-set-read-column [pgarray metadata index]
    (let [array-type (.getBaseTypeName pgarray)
          array-java (.getArray pgarray)]
      (with-meta
        (vec array-java)
        {:sql/array-type array-type}))))


(extend-protocol jdbc/ISQLParameter

  clojure.lang.IPersistentVector
  (set-parameter [val stmt ix]
    (let [conn (.getConnection stmt)
          array-java (into-array Object val)
          array-type (-> val meta :sql/array-type)
          array-pg (.createArrayOf conn array-type array-java)]
      (.setArray stmt ix array-pg))))
