# Sanitizing HTML

Moving on to the second class, we will talk about sanitizing HTML which is a tricky topic. It involves working with XML documents and XPath expression.

The need to sanitize HTML occurs when you fetch it from RSS feeds or parse websites. Or maybe your commenting system allows users to submit their replies flavoured with HTML tags. If it's a service for developers, most likely it supports Markdown which compiles to HTML.

## The problem of raw HTML

Why filter HTML content? Because if such data comes from a source you cannot trust, there are dozens of ways to attack a user who is about to read it.

Obviously, the threat number one is `<script>` tags. They can deserve in many ways: mining Bitcoin on your machine, redirecting you to malicious sites, stealing cookies, DDOS'ing somebody, etc.

Sometimes, even CSS styles carry inline Javascript to calculate the width of a page for example. Such properties as `background-image` may refer a URL to your browser makes HTTP GET requests. There is no guarantee somebody didn't put a weird code on the server side.

Event-based attributes like `onclick` run Javascript code when you click on an element. Other ones e.g. `onmouseover` are triggered just by hovering a mouse pointer on them.

Iframes show any third-party HTML resource and thus include all the Javascript or CSS issues mentioned above.

In short words, unfiltered HTML is a huge security hole. You should never accept any HTML input until you equip the server with tools to filter it.

In this class, we'll talk mostly about sanitizing HTML data fetched from RSS feeds. Usually, they represent a teaser for a full blog post and consist from common HTML tags: headers, paragraphs, links and images. For example:

{lang=html, linenos=off}
~~~
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
~~~

There is a bunch of problems in this sample, namely:

1. The image tag brings attributes that we don't need (`data-type`, `data-load`)
2. The same image has got width and height attributes which we don't need since
   the layout of your page differs from the origin.
3. The image is has got a relative URL. If we show it on our server, it will
   either return 404 response or show another image.
4. Links suffer from the same issues: excess attributes, relative URL.
5. We don't want tables to appear in the text.
6. There should be a way to get the bare text without any tags.
7. We should preserve iframes which reference YouTube embedded player.

To solve all of them, let's google for a stable, proven Java library that deals with HTML.

## First steps with Jsoup

[owasp-site]:https://github.com/OWASP/java-html-sanitizer

[jsoup-site]:https://jsoup.org/

There is a couple of great HTML tools in Java world: [OWASP Java HTML Sanitizer][owasp-site] and [Jsoup][jsoup-site]. I've have worked with both of them and found the latter being a bit more friendly although it is a matter of personal choice. Jsoup can do a lot with HTML including sanitation. We will go on with it through the session. Add the library into the project:

{lang=clojure, linenos=off}
~~~
:dependencies [[org.jsoup/jsoup "1.11.3"]]
~~~

Add a separate namespace so none of the other modules interacts with Jsoup directly. Here is a draft:

{lang=clojure, linenos=off}
~~~
(ns project.sanitize
  (:import org.jsoup.Jsoup
           (org.jsoup.safety Whitelist Cleaner)
           (org.jsoup.nodes Element Document)))
~~~

These are all the classes we need to complete the task.

You may sanitize documents with Jsoup in several ways. The simplest one is to call the static `clean` method with an HTML string and an instance of a `Whitelist` class:

{lang=clojure, linenos=off}
~~~
(defn sanitize-none
  [html]
  (when html
    (Jsoup/clean html (Whitelist/none))))
~~~

A whitelist is an entity that knows how to process tags and attributes. Out from the box, Jsoup ships several templates that cover most of the business requirements: `none`, `simpleText`, `basic`, `basicWithImages` and `relaxed`. Their naming demonstrates how HTML restrictions unwind to each level. For example, `none` template wipes all the tags leaving the just bare text. The `simpleText` one keeps minor tags used mostly for text formatting, and so forth: more and more tags and attributes are allowed.

The function `sanitize-none` covers one of the requirements we've discussed before. It turns any HTML data to plain text that's great for preview widgets that do not allow HTML:

{lang=clojure, linenos=off}
~~~
(sanitize-none html-sample)

"Benchmark results This study compares computer performance..."
~~~

Let's experiment a bit with another template:

{lang=clojure, linenos=off}
~~~
(defn sanitize-generic
  [whitelist html]
  (when html
    (Jsoup/clean html whitelist)))

(def sanitize-basic-images
  (partial sanitize-generic
           (Whitelist/basicWithImages)))

(sanitize-basic-images html-sample)
~~~

The result would be:

{lang=html, linenos=off}
~~~
<img width="640" height="480"> Benchmark results
<p>This study compares computer performance across the following platforms:</p>
<ul>
 <li>Python</li>
 <li>Go</li>
 <li>Clojure</li>
</ul>     Memory Timing Requests
<a rel="nofollow">Read more</a>
~~~

The result looks different: some of HTML entities took their place. But it is a bit ugly because the template dropped all the relative `src` attributes. We have to either to extend the template or resolve the links absolute which are more desirable.

Pay attention to how did we declare `sanitize-basic-images` function. It's just partial application of more generic `sanitize-generic`. Since the whole logic of a function depends on a single parameter, it is fine to put it in the first place and declare as many partials as you want.

The pre-defined whitelists are good for quick and dirty solutions but they cannot cover everything we need. We've got to create our own whitelist.

## Custom sanitizing logic

We will do it in several steps. Declare tags we'd like to keep as a vector of strings:

{lang=clojure, linenos=off}
~~~
(def tags-allowed
  ["a" "b" "blockquote" "br" "code"
   "h1" "h2" "h3" "h4" "h5" "h6"
   "i" "iframe" ;; !!!
   "img" "li" "p" "pre"
   "small" "span" "strike" "strong"
   "sub" "sup" "u" "ul"])
~~~

A note next to `iframe` means this tag is dangerous and we're going to process it manually.

Declare what attributes we would like to keep in form of a map of vectors:

{lang=clojure, linenos=off}
~~~
(def attrs-allowed
  {"img"    ["src"]
   "iframe" ["src" "allowfullscreen"]
   "a"      ["href"]})
~~~

Declare the network protocols allowed. On the top level there is a tag name, then an attribute and a list of schemas:

{lang=clojure, linenos=off}
~~~
(def proto-allowed
  {"a"      {"href" ["http" "https" "mailto" "ftp"]}
   "img"    {"src"  ["http" "https"]}
   "iframe" {"src"  ["https"]}})
~~~

For example, an `<a>` element can refer to an HTTP(s) or FTP resources or be an email. But an `<iframe>` references only a secured `https://` resource.

A small wrapper that converts a Clojure vector into a typed Java array of Strings:

{lang=clojure, linenos=off}
~~~
(def ->array (partial into-array String))
~~~

Now that with all the data prepared we declare an instance of a `Whitelist` class:

{lang=clojure, linenos=off}
~~~
(def ^Whitelist whitelist-custom
  (let [wl (new Whitelist)]

    ;; set tags
    (.addTags wl (->array tags-allowed))

    ;; set attributes
    (doseq [[tag attrs] attrs-allowed]
      (.addAttributes wl tag (->array attrs)))

    ;; set protocols
    (doseq [[tag mapping] proto-allowed]
      (doseq [[attr protocols] mapping]
        (.addProtocols wl tag attr (->array protocols))))

    wl))
~~~

What catches the eye here is Clojure code has become imperative. That's normal because we operate on Java objects that are imperative by their nature. Wrap the whitelist with a `Cleaner` class:

{lang=clojure, linenos=off}
~~~
(def ^Cleaner cleaner-custom
  (Cleaner. whitelist-custom))
~~~

Here is the function that cleans the source data using our own rules:

{lang=clojure, linenos=off}
~~~
(defn sanitize-custom
  [html page-url]
  (when html
    (let [page-url (or page-url "")
          ^Document doc-src (Jsoup/parse html page-url)]
      ;; (process-iframes doc-src)
      (let [^Document doc-out (.clean cleaner-custom doc-src)]
        (.. doc-out body html)))))
~~~

It accepts a raw HTML string and its source URL and returns cleaned HTML string. We need the source to fix relative links so they become absolute. For example, if the source page was `http://example.com/pages/story` and there is an image with the attribute `src="/images/something.jpeg"`, it becomes `src="http://example.com/images/something.jpeg"` which is one of our business requirements.

The double dot macro acts like a chain of calls to the `doc-out` variable. First, we receive its body which is an instance of an `Element` class. Then we take its HTML content calling `html` method.

Let's test the function:

{lang=clojure, linenos=off}
~~~
(sanitize-custom html-sample "http://example.com/pages/blog.html")
~~~

The result would be:

{lang=html, linenos=off}
~~~
<img src="http://example.com/images/articles/preview.jpg">
<h1>Benchmark results</h1>
<p>This study compares computer performance across the following platforms:</p>
<ul>
 <li>Python</li>
 <li>Go</li>
 <li>Clojure</li>
</ul>     Memory Timing Requests
<a href="http://example.com/articles/banchmark.html">Read more</a>
~~~

This looks nice yet we haven't' dealt with iframes so far. There is commented line in the function that signals we're about to fix that. Jsoup doesn't support conditional statements for tags or attributes so we have to process iframes manually.

## Dealing with Iframes

Ok, let's uncomment that line and declare a draft version of a function:

{lang=clojure, linenos=off}
~~~
(defn process-iframes
  [^Document doc]
  (doseq [^Element el (.select doc "iframe")]
    ;; some logic goes here
    ))
~~~

Now let's think for a while. We need to keep only those iframes that reference either YouTupe or Coub embedded players. For example:

{lang=html, linenos=off}
~~~
<iframe width='903' height='508'
  src='https://www.youtube.com/embed/OhadKfy2RxM' frameborder='0'
  allow='autoplay; encrypted-media' allowfullscreen>
</iframe>

<iframe
  src='//coub.com/embed/5oy44?muted=false&autostart=false&originalSize=false&startWithHD=false'
  allowfullscreen frameborder='0'
  width='626' height='480' allow='autoplay'>
</iframe>
~~~

The logic would be to get a URL from the `src` attribute and match it against regular expressions that represent allowed media resources. If it matches any of them, we keep an element in a DOM tree. When it doesn't, we remove it.

Declare the expressions and add some shortcuts:

{lang=clojure, linenos=off}
~~~
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
~~~

That's it! So far, our code satisfies all the business requirements. Restricted tags and attributes a wiped out. All the links and images addresses are absolute. We preserve only those iframes that reference certain media resources. Let's extend the `html-sample` variable by adding some iframes into it:

{lang=clojure, linenos=off}
~~~
(def html-sample
  "
  ;; the same HTML we had before

<iframe width='903' height='508'
  src='https://www.youtube.com/embed/OhadKfy2RxM' frameborder='0'
  allow='autoplay; encrypted-media' allowfullscreen>
</iframe>

<iframe src='https://dangerous.site.com/some/path.html'></iframe>
  ")
~~~

{lang=clojure, linenos=off}
~~~
(sanitize-custom html-sample "http://example.com/pages/blog.html")
~~~

The result is:

{lang=html, linenos=off}
~~~
<img src="http://example.com/images/articles/preview.jpg">
<h1>Benchmark results</h1>
<p>This study compares computer performance across the following platforms:</p>
<ul>
 <li>Python</li>
 <li>Go</li>
 <li>Clojure</li>
</ul>     Memory Timing Requests
<a href="http://example.com/articles/banchmark.html">Read more</a>
<iframe src="https://www.youtube.com/embed/OhadKfy2RxM" allowfullscreen> </iframe>
~~~

As you see, the malicious iframe was dropped whereas the YouTube one is still here. Pay attention we allow `allowfullscreen` attribute to take its place in an iframe so a user is able to expand a video on the whole screen.

[source]: https://github.com/igrishaev/clj-java-book/blob/master/project/src/project/ua.clj

Although the explanations took long, the final code is surprisingly short. The whole module takes less than 100 lines. You are welcome to check it [on GitHub][source].

## Conclusion

So far, we've learnt how to sanitize HTML using Java Jsoup library. The solution looks solid and neet. It's easy to tweak in different prospectives because the most of the configuration is stored in Clojure maps or vectors. Adding or excluding another tag would require to add or remove it from a collection. It's declarative, as functional programmers tend to say. The same is about media resources: to keep Vimeo embedded player in HTML, just add another regular expression.

To the end of the second class, I believe you've become sure about how much power Java brings to the scene. We will use the more in the next lessons.
