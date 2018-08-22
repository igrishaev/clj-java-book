(defproject project "0.1.0"

  :description "Code samples"
  :url "http://grishaev.me/" ;; todo

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]

                 ;; pipeline
                 [org.apache.commons/commons-compress "1.5"]
                 [clj-http "3.7.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.jsoup/jsoup "1.11.3"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.postgresql/postgresql "42.1.3"]

                 ])
