(ns project.san

  (:import org.jsoup.Jsoup
           (org.jsoup.safety Whitelist Cleaner)
           (org.jsoup.nodes Element Document)))


(def tags-allowed
  ["a" "b" "blockquote" "br" "code"
   "h1" "h2" "h3" "h4" "h5" "h6"
   "i" "iframe" "img" "li" "p" "pre"
   "small" "span" "strike" "strong"
   "sub" "sup" "u" "ul"])


(def attrs-allowed
  {"img"    ["src"]
   "iframe" ["src" "allowfullscreen"]
   "a"      ["href"]})


(def proto-allowed
  {"a"      {"href" ["http" "https" "mailto" "ftp"]}
   "img"    {"src"  ["http" "https"]}
   "iframe" {"src"  ["https"]}})


(def ->array (partial into-array String))


(def ^Whitelist whitelist-custom
  (let [wl (new Whitelist)]

    (.addTags wl (->array tags-allowed))

    (doseq [[tag attrs] attrs-allowed]
      (.addAttributes wl tag (->array attrs)))

    (doseq [[tag mapping] proto-allowed]
      (doseq [[attr protocols] mapping]
        (.addProtocols wl tag attr (->array protocols))))

    wl))


(def ^Cleaner cleaner-custom
  (Cleaner. whitelist-custom))


(def re-youtube
  #"(?i)youtube.com/embed")


(def re-coub
  #"(?i)coub.com/embed")


(defn media-src?
  [src]
  (or (re-find re-youtube src)
      (re-find re-coub src)))


(defn process-iframes
  [^Document doc]
  (doseq [^Element el (.select doc "iframe")]
    (let [src (.absUrl el "src")]
      (when-not (media-src? src)
        (.remove el)))))


(defn sanitize-custom
  [html page-url]
  (when html
    (let [page-url (or page-url "")
          ^Document doc-src (Jsoup/parse html page-url)]
      (process-iframes doc-src)
      (let [^Document doc-out (.clean cleaner-custom doc-src)]
        (.. doc-out body html)))))


(defn sanitize-generic
  [whitelist html]
  (when html
    (Jsoup/clean html whitelist)))


(def sanitize-none
  (partial sanitize-generic
           (Whitelist/none)))

(def sanitize-basic-images
  (partial sanitize-generic
           (Whitelist/basicWithImages)))


(def html-sample
  "
<img class='thumb-image' data-type='image' data-load='[truncated]'
  width='640' height='480'
  src='/images/articles/preview.jpg' />

<h1>Benchmark results</h1>

<p>This study compares computer performance across the following platforms:</p>

<ul>
  <li>Python</li>
  <li>Go</li>
  <li>Clojure</li>
</ul>

<table>
  <tbody>
    <thead>
      <tr>
        <th>Memory</th>
        <th>Timing</th>
        <th>Requests</th>
      </tr>
    </thead>
  </tbody>
</table>

<a href='/articles/banchmark.html' data-load='[truncated]'>Read more</a>

<iframe width='903' height='508'
  src='https://www.youtube.com/embed/OhadKfy2RxM' frameborder='0'
  allow='autoplay; encrypted-media' allowfullscreen>
</iframe>

<iframe src='https://dangerous.site.com/some/path.html'></iframe>

  ")
