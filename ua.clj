(ns project.ua
  (:import [net.sf.uadetector.service
            UADetectorServiceFactory]

           [net.sf.uadetector
            UserAgent
            VersionNumber
            OperatingSystem]))

(def ^:private parser
  (UADetectorServiceFactory/getResourceModuleParser))

(defn parse [^String user-agent]
  (->clj (.parse parser user-agent)))

(def ua-sample
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like
Gecko) Chrome/67.0.3396.99 Safari/537.36")

(def result (parse ua-sample))

(defprotocol ToClojure
  (->clj [x]))

(extend-protocol ToClojure

  UserAgent
  (->clj [ua]
    {:device       (->clj (.getDeviceCategory ua))
     :family       (->clj (.getFamily ua))
     :icon         (.getIcon ua)
     :name         (.getName ua)
     :os           (->clj (.getOperatingSystem ua))
     :producer     (.getProducer ua)
     :producer-url (.getProducerUrl ua)
     :type         (->clj (.getType ua))
     :type-name    (.getTypeName ua)
     :url          (.getUrl ua)
     :version      (->clj (.getVersionNumber ua))})

  OperatingSystem
  (->clj [os]
    {:family (->clj (.getFamily os))
     :family-name (.getFamilyName os)
     :name (.getName os)
     :producer (.getProducer os)
     :producer-url (.getProducerUrl os)
     :url (.getUrl os)
     :version (->clj (.getVersionNumber os))})

  VersionNumber
  (->clj [ver]
    {:bug-fix (.getBugfix ver)
     :extension (.getExtension ver)
     :groups (.getGroups ver)
     :major (.getMajor ver)
     :minor (.getMinor ver)
     :version (.toVersionString ver)})

  java.lang.Enum
  (->clj [e]
    (-> e .name keyword)))
