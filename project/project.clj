(defproject project "0.1.0"

  :description "Code samples"
  :url "http://grishaev.me/" ;; todo

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]

                 ;; user agent
                 [net.sf.uadetector/uadetector-core "0.9.10"]
                 [net.sf.uadetector/uadetector-resources "2014.10"]

                 ;; better zip support
                 [org.apache.commons/commons-compress "1.5"]

                 ;; db
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.postgresql/postgresql "42.1.3"]

                 ;; html
                 [org.jsoup/jsoup "1.11.3"]

                 ;; misc
                 [clj-http "3.7.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [cheshire "5.6.3"]])
