


# Java interop basics



This chapter brings some fundations about Java interop that we will actively reuse during the book. I'm going to highlight basic rules on how to operate on Java objects as well as share some good practices on the subject.

To use a class, you've got to import it first. There are two ways to import a
class: in a namespace declaration which is desirable and in run-time using
`import` function.

```clojure
(ns com.project.module
  (:import java.util.File))
```

or

```clojure
(import 'java.util.File)
```

Pay attention, in the first example we specify a class without quoting it
because the whole `ns` statement is a macro so everything inside it is
quoted. Instead, when calling `import` function in runtime we pass a symbol
which's name stands for a class we need.

Importing classes on the fly might be a bit confusing and not obvious. It's
better keep all the imports and requirements in the `ns` declaration at the top
of a file.

To be imported, a class should present in your classpath. A classpath is a
special parameter of Java virtual machine. It's a list of paths separated with
colons where the machine should search classes for. Usualy you don't worry about
configuring classpath manualy because modern development tools like `lein` or
`deps.edn` take care of it by their own.

The tools mentioned above scan through a list of dependencies declared in a
config file. They download required artifacts into a special folder on your
machine and start JVM passing classpath extended with the libraries you
specified.

Classes that belong to the `java.lang` package are not required to being
imported. They are available by default, so `java.lang.String` shortens to just
`String`.

Creating an instance of a class works in two ways: `new` or trailing dot
macroses. The `new` form takes a class followed by its arguments. It looks like
the standard `new` operator in Java. The trailing dot macro requires putting a
dot at the end of a class name skipping `new` at the begging. As the result,
it's a bit shorter then the first `new` form.

Let's fixate everything told so far with examples. We will use the standard
classes distributed with Java SDK by default.

Importing a single class:

```clojure
(ns project.into
  (:import java.io.File))
```

Importing several classes from the same package at once:

```
(ns project.into
  (:import java.io.File
           (java.util Date UUID)))
```

Pay attention at extra parens around `java.util` path. It's mandatory to put
them here. They help Clojure reader to not get confused when parsing imports.

Let's initiate some of the classes we've imported:

```clojure
(def file (File. "/Users/ivan/.emacs"))

;; evaluating the `file` variable in repl prints
#object[java.io.File 0xbfef89d "/Users/ivan/.emacs"]

(def date (new Date))

;; evaluating `date` returns
#inst "2018-08-25T08:20:40.412-00:00"
```

Some Java classes provide static methods. Calling such a method doesn't require
to create an instace of a class. Access them via slash as follows:

```clojure
(def uuid (UUID/randomUUID))

#uuid "fb5876a6-b3c6-47dc-89d6-10dafcaf0888"
```

[java-system]:https://docs.oracle.com/javase/9/docs/api/java/lang/System.html

The standard [System class][java-system] carries plenty of static methods useful
for general purposes. This class belongs to `java.lang` package ans thus is
available from everywhere without importing it.

To stop the program completelly call its `exit` method passing an exit
code. Running it in a REPL session will terminate it.

```clojure
(System/exit 0)
```

The `getenv` static methods either returns a single environ varialble or the
whole map depending on arity (the number of passed parameters):

```clojure
(System/getenv "HOME")
"/Users/ivan"

(into {} (System/getenv))
{"LEIN_VERSION" "2.6.1"
 "HOME"         "/Users/ivan"
 "USER"         "ivan"
 "LEIN_HOME"    "/Users/ivan/.lein"
 ;; truncated
 }
```

In the second case, we convent a Java native map into a Clojure map to make the
output better.

A simple wrapper to get the current number of seconds since 1 Jan 1970 which
which also known as Unix timestamp or epoch:

```clojure
(defn epoch
  []
  (quot (System/currentTimeMillis) 1000))

(epoch)
1535186375
```

Having an initiated class, usualy you are interested in calling its methods. To
access an ordinary non-static method, put its name with leading dot at the first
place of a lisp form followed by the instance and the rest arguments.

Here is how you may know a file's absolute path:

```clojure
(def file (File. "book.txt"))

(.getAbsolutePath file)
"/Users/ivan/drafts/project/book.txt"
```

To check if it really exists:

```clojure
(.exists file)
true
```

Or rename (move) it:

```clojure
(.renameTo file (File. "/Users/ivan/ready/project/book-ready.txt"))
true
```

Sometimes, a class may declare a nested class. To access it, put dollar sign
between their names. For example, if a class `Foo` has a nested class `Bar`, a
syntax to reach it will be:

```clojure
(ns ...
  (:import com.project.Foo$Bar))

(def bar (Foo$Bar. param1 param2))
```

There is a specil Dot form that acts like a universal access to Java classes. It
keeps the same syntax structure even for different purposes.

To read a static field:

```clojure
(. File pathSeparator)
":"
```

To call a static method:

```clojure
(. File createTempFile "temp" ".txt")
#object[java.io.File 0x4c2544bc "/var/folders/94/rwlfpkhx4n12vfjb0d0kxspw0000gn/T/temp7711291656500566792.txt"]
```

To call a method of an instance:

```clojure
(. file getAbsolutePath)
"/Users/ivan/drafts/project/book.txt"
```

Or touch a field of an instance. Pay attention at leading hyphen:

```clojure
(. obj -value)
;; the same as `obj.value` in pure Java
```

There is also a `set!` form that works in pair with the dot macro. Use it to
write a new value to a field:

```clojure
(set! (. obj -value) 42)
;; the same as `obj.value = 42` in pure Java
```

The last two cases with reading and writing fields are not common due to Java
design patters. Exposing fields to the outter world is considered as bad
practice. Instead, most of Java programmers provide special methods to regulate
how a certain field is being gotten or set. Thus, you will call for `(.getValue
obj)` or `(.setValue obj 42)` more ofthen than accessing raw fields.

The double dot macros acts similar to the single dot form. It chanis results
between multiple expressions so the next form takes a value procuded in a
previous form. It is similar to the threading `->` macro that probably you are
familiar with.

```clojure
(.. file toPath getFileSystem getClass getName)
"sun.nio.fs.MacOSXFileSystem"
```

Under the hood, it turns into a nested extression as follows:

```clojure
(. (. (. (. (. file toPath) toPath) getFileSystem) getClass) getName)
```

which is really difficult to read since your eyes have to jump here and there.

Each method in a chain is called on an object reveived from a previous
method. If any extra arguments are requied, the method expression is put into
parens:

```clojure
(.. obj (some-method "foo") (other-method "bar" 42))
```

To know what class an object belongs to, call `class` function:

```clojure
(class file)
java.io.File
```

Probably you would like to get a class and make some checks, for example if was
a string path, do this, and if it was a `File` instance, do that. The function
`instance?` checks if an object is an instance of certain class so it could
reduce the code. Insead of writing something like that:

```clojure
(case (class source)

  java.io.File
  ;; do this

  String
  ;; do that
)
```

you do this:

```clojure

(def file? (partial instance? java.io.File))

(cond
  (file? source)
  ;; do this

  (string? source)
  ;; do that
  )
```

which is more readable and neat.

An important note refers to those Java methods that accept arbitrary number of
arguments. They are marked with ellipsis in Java signatures and represent an
array of objects when accessing them. A good example is `format` method of the
`String` class:

```java
static String    format(String format, Object... args)
```

This of way of calling such a method in Cojure won't work:

```
(String/format "%s %s %s" "foo" "bar" "baz")
```

The exception's message will say `No matching method: format`.

This is because in Clojure terms, the `args` parameters should be passed as a
single array. It should be a native Java array but not a native Clojure
collection. To make your life a bit easier, there are already some wrappers that
do it for you, e.g. `make-array` that turns a Clojure collection into a Java
typed array:

```clojure
(String/format "%s %s %s" (into-array ["foo" "bar" "baz"]))
"foo bar baz"
```

By default, `into-array` builds an array of `Objects` that satifies the method's
signature in our case. When you need an array if some certain type, you pass its
class as the first parameter to that function:

```clojure
(String/format "%s %s %s" (into-array String ["foo" "bar" "baz"]))
```

Yet Clojure is a language with dynamic type system its runtime relies on types a
lot. When it knows that type an object belongs to, it doesn't spend extra time
on reflection and thus performs faster. There is a way you might help the
compiler by adding type hints. A hint might be put almost everywhere a variable
occurs: in parameters, in `let` bindings as well as for result of a function:

TODO

```clojure
(defn ^String foo
  [^String aaa ^Number ]
  (let []
   ()))
```

There is no need to put tags everywhere you physically can. The compiler is
smart enough to make decitions on further type of an expression when there is
enough metadata about its arguments.

Adding type hints may prevent a programm from getting into a bottleneck. When a
function is being called constantly, check if it has type hints. It may reduce
the total time needed to complete the programm significantly.

A type hint may be any class that has been imorted before. But sometimes, some
complex Java signatures are required for example to specify an array of certain
type. So the hint takes a form of a string:

```clojure
(defn ^"[Ljava.lang.String;"
  args->command
  [args]
  (into-array String (map str args)))
```

-- best practices

One more benefit that tags bring to a project, they are useful to get into the
codebase quickly. When you operate on ordinary Java primitives and collections,
a type of a varialbe might be guesses by its name withe ease, e.g. `opt`,
`params` (usualy a map of options), `items`, `users` (a collection of some
entitites) or `url`, `path` (a string).

That's fine until you pass a parsed HTML document which is an instance of
`org.jsoup.nodes.Document` class. A lonely name `doc` won't say anything to a
programmer who has got to maintain the code. Is it a map, a vector or comes from
any third-party library? Instead, the `org.jsoup.nodes.Document doc` declaration
clearly expresses the very nature of the `doc` parameters to it saves plenty of
time.

Another good practive when dealing with Java interop is to isolate it in a
separate namespace or even a library if you can affort it. Such a namespace
should provide clean functions that take and receive Clojure data. Java interop
is considered as a low-level feature so it is always a subject to change. So if
anything changed in a namespace's guts, none if other modules would fail.

Usualy, Clojure code that owns Java mixins grows in length because of
longNamedMethods. As the result, it looks clumsy and might be difficult to
read. The most undesirable thing that may happen with Clojure code is when it
turns into Java spagetty full of mitable state. The rule "one namespace per one
business domain" prevents the codebase from dumping dozens of Java classes all
together.

-- summary

So far, we have passed through the most part of Clojure capabilities for Java
interop. There are still such topics that I didn't mention as proxying and
reifying classes and interfaces or something more. But this book is not aimed at
just enumarating features. Instead, everything in that material is about
practice. I cannot guarantie we will cover all the possible Java interop
features, but everything you will learn will be done for sure. I persuade to
start with coding sessions as soon a possible. If any unmentioned feature
appear, we will deal with it on the fly.





Sanitize HTML

Moving on to the second class, we will talk in sanitizing HTML which is tricky
topic. It involves working with XML documents and XPath expression.

The need to sanitize HTML may occur if you show text fetched from RSS feeds or
miscellaneous web-crawlers. Maybe, your commenting system allows users to input
their text with some subset of HTML. Or if it is a service for developers, most
likely it supports Makrdown which compiles to HTML.

So what is the need to filter HTML data? Because if such a data comes from
source you cannot trust, there are dozens of ways to attack a user who is goint
to view it.

Obviosly, the threat number one is `<script>` tags. There might be plenty of
malicious code: mining Bitcoin on you machine, redirecting you to online casino
sites, stealing cookies.

CSS styles also carry inline Javascript sometimes to calculate width of a
page. There is no guarantee somebody didn't put weird code there.

Such attributes as `onclick` run Javascript code once you click on an
element. Other ones e.g. `onmouseover` are triggered just by moving a mouse
pointer upon them.

Iframes may show ads or let you run Javascrip that belongs to a malicious site.

In short words, unfiltered HTML is a huge security hole. So you'd better not to
accept any user input untill you armed the server with tools to fiter it.

In this class, I'll talk mostly on HTML data fetched from RSS or Atom
feeds. Usualy it represents a teaser for a full blog post and consists of common
HTML tags: headers, paragraps, links and images. For example:

```html
TODO
```

There is a bunch of problems in this sample fragment, namely:

1. The image tag brings attributes that we don't need (`data-type`, `data-load`)
2. The same image has got width and height attributes which we don't need since
   the layout of your page differs from the origin.
3. The image is has got relative URL. If we show it on our server, it will
   either return 404 reponse or show another image.
4. The same remarks for the `<a>` tag: excess attributes, relative URL.
5. We don't want tables to appear in the text.
6. There should be a way to get bare text without any tags.
7. We should preserve iframes which reference YouTube embedded player.

[owasp-site]:https://github.com/OWASP/java-html-sanitizer
[jsoup-site]:https://jsoup.org/

To solve all of them, let's google for a stable, prooven Java library that deals
with HTML. These are [OWASP Java HTML Sanitizer][owasp-site] and [Jsoup][]. I've
have worked with both of them and found the latter a bit more friendly although
it is a matter of personal attitute.

Jsoup is a generic tool that can do a lot with HTML including sanitazion. We
will go on with Jsoup in that session. Add the library into the project:

```clojure
:dependencies [;; some deps
               [org.jsoup/jsoup "1.11.3"]]
```

Add a separate namespace so none of other modules can access Jsoup
directly. Here is a draft:

```clojure
(ns project.sanitize
  (:import org.jsoup.Jsoup
           (org.jsoup.safety Whitelist Cleaner)
           (org.jsoup.nodes Element Document)))
```

These are all classes we need to complete the task.

You may sanitize documents with Jsoup in several ways. The simpliest one is to
call static `clean` method with an HTML string and an instance of a `Whitelist`
class:

```clojure
(defn sanitize-none
  [html]
  (when html
    (Jsoup/clean html (Whitelist/none))))
```

A whitelist is an entity that knows how to process tags and attributes. Out from
the box, Jsoup ships several templates that cover most of the business
requirements: `none`, `simpleText`, `basic`, `basicWithImages` and
`relaxed`. Their naming demonstrates how HTML capabilites grow to each
level. For example, `none` template wipes all the tags and so there is just bare
text in the end. The `simpleText` one adds some minor tags used mostly for text
formatting, and so forth: more and more tags and attributes are allowed.

The function `sanitize-none` covers one of the requrements we've discussed
before. It turns any HTML data to plain text that's great for preview:

```clojure
(defn _text (sanitize-none html-sample))

TODO
```

Let's experiment a bit with another template:

```clojure
(defn sanitize-generic
  [whitelist html]
  (when html
    (Jsoup/clean html whitelist)))

(def sanitize-basic-images
  (partial sanitize-generic
           (Whitelist/basicWithImages)))

(sanitize-basic-images html-sample)
```

The result looks different: now some of HTML entities take their place. Pay
attention how did we declare `sanitize-basic-images` function. It's just partial
application of more generic `sanitize-generic`. Since the whole logic of a
function depends on a single parameter, it is fine to put it at the first place
and declare as many partials as you wish.

Although pre-defined whitelists are good, they cannot cover everything we
need. So let's consider the second low-level way of filtering HTML. First, let's
declare our own whitelist. We will do it in several steps.

Declare tags we'd like to keep, just a vector of strings:


```clojure

(def tags-allowed
  ["a"
   "b"
   "blockquote"
   "br"
   "code"
   "h1"
   "h2"
   "h3"
   "h4"
   "h5"
   "h6"
   "i"
   "iframe" ;; !!!
   "img"
   "li"
   "p"
   "pre"
   "small"
   "span"
   "strike"
   "strong"
   "sub"
   "sup"
   "u"
   "ul"])
```

A note opposite to `iframe` means this tag is dangerous and we're going to process it manually.

Declare what attributes we'd like to keep, a map of vectors:

```clojure
(def attrs-allowed
  {"img"    ["src"]
   "iframe" ["src" "allowfullscreen"]
   "a"      ["href"]})
```

Declare what network protocols. On the top level there is a tag name, then an
attribute and a list of network schemas:

```clojure
(def proto-allowed
  {"a"      {"href" ["http" "https" "mailto" "ftp"]}
   "img"    {"src"  ["http" "https"]}
   "iframe" {"src"  ["https"]}})
```

For example, a link might reference to an HTTP(s) and FTP resources or be an
email. But an iframe may only reference a secured `https://` resource.

A small wraper to convert a Clojure vector into typed Java array of Strings:

```clojure
(def ->array (partial into-array String))
```

Now that with all the data prepared we declare an instance of a `Whitelist`
class:

```clojure
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
```

What catches the eye here is Clojure code has become very imperative. That's
normal since we operate on Java ecosystem that is mostly imperative by its
nature.

Wrap the whitelist with a `Cleaner` class:

```clojure
(def ^Cleaner cleaner-custom
  (Cleaner. whitelist-custom))
```

Here is the function that cleans the source data using our own rules:

```clojure
(defn sanitize-custom
  [html page-url]
  (when html
    (let [page-url (or page-url "")
          ^Document doc-src (Jsoup/parse html page-url)]
      ;; (process-iframes doc-src)
      (let [^Document doc-out (.clean cleaner-custom doc-src)]
        (.. doc-out body html)))))
```

It accepts a raw HTML string and a URL and returns cleaned HTML string. We need
a source URL to fix relative links so the become absolute. For example, if the
source page was `http://example.com/pages/story` and there was an image with the
attribute `src="/images/something.jpeg"`, it becomes
`src="http://example.com/images/something.jpeg"` what is one of our busines
requirements.

The double dot macro acts like a chain of calls to the `doc-out`
varialbe. First, we receive its body which is an instance of an `Element`
class. Then we take its HTML content of that element as a string calling `html`
method.

Let's test the function:

```clojure
(sanitize-custom html-sample "http://example.com/pages/blog.html")
```

The result would be:

```html
TODO
```

This is nice, but we havent' dealt with iframes so far. There is commented line
in the function that signals we're about to fix that. Jsoup doesn't support
conditional statements for tags or attributes so we have to process iframes
manually.

Ok, let's uncomment that line and declare a draft version of a function:

```clojure
(defn process-iframes
  [^Document doc]
  (doseq [^Element el (.select doc "iframe")]
    ;; some logic goes here
    ))
```

Now let's discourse for a while. We need to keep only those iframes that
reference either YouTupe or Coub embedded players. For example:

```html

<iframe width="903" height="508" src="https://www.youtube.com/embed/OhadKfy2RxM" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

<iframe src="//coub.com/embed/5oy44?muted=false&autostart=false&originalSize=false&startWithHD=false" allowfullscreen frameborder="0" width="626" height="480" allow="autoplay"></iframe>
```

So logic would be to get a URL from the `src` attribute and match it against
regular expression. If it matches any of them, we keep that element in a DOM
three. When it doesn't, we remove it.

Declare the expressions and add some shortcuts:

```clojure
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
```

That's it! So far, our code satisfies all the business requirements. Restricted
tags and attributes a wiped out. All the links and images addresses are
absolute. We preserve only those iframes that reference certain media resources:

```clojure
(sanitize-custom html-sample "http://example.com/pages/blog.html")
```

The result:

```html
TODO
```

Despite the exlanation was a bit log, the final code is suprisengly short. The
whole module takes just 100 lines:

```clojure
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
```

TODO GitHub link

You may look it through on GitHub.

My congratulations to you if you've been following the line to the end. So far,
we've learnt how to sanitize HTML reusing Java Jsoup library. The solution looks
solid and neet. It's easy to tweak in different prospectives. Most of the
configuration is stored in Clojure maps or vectors so allowing or restrictint
yet another tag would mean just to add or remove it from a collection. It's
declarative, as functional programmers tend to say. Processing an extra media
iframe, say Vimeo or VKontakte implies adding just one more regex expression.

To the end of the second class, I believe you've made sure how much power Java
brings to the scene. But the most part of the practial lessons are still ahead
of us.



Boosting up your JDBC driver

In this class, we will turn to the database facilities. Most likely your
application works with some sort of a database. There are plenty of DB types
nowadays. For example, such prooven relational systems as MySQL or Postgres have
been developed for decades; modern non-relational Mongo or CouchDB; even
Clojure-aimed Datomic that ships as many Clojure-related features to the schene
as possible.

Having a layer of flexible abstractions above a low-level, system-depended
driver is crutial. The move features your database module brings, the less code
you will have in the rest of the codebase. It's important to not fall into
constructing your own ORM since the complexety will beat you whereas the
business features won't progress.

In this chapter, I'll explain how to add more features to the standard JDBC
driver in conjunction with PostgreSQL backend. Both technologies have passed a
long way of development. Postgres is a powerful database with dozens of features
and flexible data types. It is a free and open-source solution.

JDBC and its Clojure wrapper provide genearal APIs to let you work with any DB
backend using the same approach. At first glanse, the API look a bit clumsy, but
the reason of that is JDBC supports huge veriaty of backends and their versions
plus legacy support.

Let's get started with the database module in the project. I believe you have
PostgreSQL installed on your local machine. If not, install it with your package
manager either with

```bash
sudo apt-get install postgresql
```

or

```bash
brew install postgresql
```

[pg-download]:https://www.postgresql.org/download/

depending on if you use Linux or Mac. In case you've got Windows desktop or
something went as not expected, visit the [official Download page][pg-download]
for the latest info.

Add the JDBC wrapper and the driver into dependencies:

```clojure
[org.clojure/java.jdbc "0.6.1"]
[org.postgresql/postgresql "42.1.3"]
```

These two lines highlight the very architecture of the database access. JDBC
wrapper (`java.jdbc`) provides top-level API to query the database whereas the
driver (`postgresql`) knows to perform againts specific backend. For MySQL
you'll need to replace `postgresql` driver dependency with

```clojure
[org.clojure/java.jdbc "0.6.1"]
[mysql/mysql-connector-java "8.0.12"]
```

Let's quickly prepare a new database for our experiments:

```bash
su - postgres

# create a new DB user; will prompt for a password
createuser -S -W clj-user

# create a new database which's owner is our user
createdb -O clj-user clj-db

# login into that database
psql clj-db clj-user

# create a new table with no meaningful fields so far
create table test (id serial primary key);
```

Create a separate Clojure module to keep all the database capabilities in one
place:

```clojure
(ns project.db
  (:require
   [clojure.java.jdbc :as jdbc])
```

Declare something that is known for the database spec. It's a map with DB
credentials and probably additional options:

```clojure
(def db
  {:dbtype "postgresql"
   :dbname "clj-db"
   :host "127.0.0.1"
   :user "clj-user"
   :password "clj-pass"})
```

And some local shortcuts:

```clojure
(def query (partial jdbc/query db))

(def insert! (partial jdbc/insert! db))

(def execute! (partial jdbc/execute! db))
```

A quick check:

```clojure
(query "select 42 as the_answer")

({:the_answer 42})
```

What works! So the preparation step is done and we are ready to play with Java
machinery again.

Generaly speaking the problem we are trying to solve is to establish a seamless
mapping between the database types and Clojure collections. We will use some
JDBC helpers and Java classes to make it the mapping better.

The Clojure wrapper around the original Java JDBC brings several protocols that
let the database know how to treat certain objects. If you extend them wisely,
you may build a handly connection between a Clojure record and it's binary
representation in the DB and vice versa.

Let's start with something simple. By default, JDBC desn't know how to process
`java.net.URL` or `java.util.UUID` that we use often. Every time you'd like to
write such an object to the database you need to coerce it to a string which is
fine but a bit annoying.

To teach the database how to treat those classes, extend the `jdbc/ISQLValue`
protocol as follows:

```clojure
(extend-protocol jdbc/ISQLValue

  java.net.URL
  (sql-value [url]
    (str url))

  java.util.UUID
  (sql-value [uuid]
    (str uuid)))
```

To check if the changes came into play, add new fields to our table and insert
something there as follows:

```clojure
(execute! "alter table test add column url text")
(execute! "alter table test add column uuid text")

(insert! :test {:url (java.net.URL. "http://example.com")
                :uuid (java.util.UUID/randomUUID)})

(query "select * from test")

({:id 1,
  :url "http://example.com",
  :uuid "3e54df1f-b961-4303-a512-5485044a3576"})
```

Both types have been transformed successfuly. So far, it was simple because
everything we've done was coercing objects to a string.

The things start getting harder when non-string based types come into play. Take
dates, for example. Each database backend stores them in their own way so
squashing a date into a string won't work.

By default, JDBC returns dates from queries without troubles:

```clojure
(execute! "alter table test add column created_at timestamp default now()")

(query "select * from test")

({:id 1,
  :url "http://example.com",
  :uuid "3e54df1f-b961-4303-a512-5485044a3576",
  :created_at #inst "2018-08-20T14:30:31.748138000-00:00"})
```

But passing a date as a parameter...

```clojure
(insert! :test {:created_at #inst "2017-01-01"})
```

...causes an error:

```
PSQLException Can't infer the SQL type to use for an instance of
java.util.Date.  Use setObject() with an explicit Types value
to specify the type to use.
org.postgresql.jdbc.PgPreparedStatement.setObject (PgPreparedStatement.java:973)
```

This looks a bit inconsistent but let's fix it. JDBC awaits for a special
SQL-flavored class `java.sql.Timestamp`. Turning one type to another is done by
converting the source date into milliseconds and restoring the type we need from
them:

```clojure
(extend-protocol jdbc/ISQLValue

  java.util.Date
  (sql-value [val]
    (java.sql.Timestamp. (.getTime val))))
```

Now, the native Date class should work:

```clojure
(insert! :test {:created_at #inst "2017-01-01"})

({:id 3,
  :url nil,
  :uuid nil,
  :created_at #inst "2017-01-01T00:00:00.000000000-00:00"})
```

Another example of inconsistancy with types in JDBC is when you deal with DB
enum values. In Postgres, enums are special type that may have only declared
values. Let's create a simple enum type bound to a new column:

```clojure
(execute! "create type type_color as enum ('red', 'green', 'blue')")
(execute! "alter table test add column color type_color")
(execute! "update test set color = 'red'")
(query "select id, color from test")

({:id 1, :color "red"}
 {:id 2, :color "red"}
 {:id 3, :color "red"})
```

That works fine, all the color values are strings as expected. But trying to
insert a new row...

```clojure
(insert! :test {:color "blue"})
```

...causes an error:

```
PSQLException ERROR: column "color" is of type type_color but expression is of type character varying
  Hint: You will need to rewrite or cast the expression.
  Position: 37  org.postgresql.core.v3.QueryExecutorImpl.receiveErrorResponse (QueryExecutorImpl.java:2476)
```

This is because JDBC treats `type_color` type as something that really differs a
string. To send a enum value into the dababase, we need to wrap it with a
special `PGObject` class.

`PGObject` is a low-level object that represents Postres-specific value. It has
just two meaningful fields: a type and and its value, both strings. In the
beginning of our module, add a new import line:

```clojure
(:import org.postgresql.util.PGobject)
```

and create a bit of wrappers:

```clojure
(defn ->pgobject
  [type value]
  (doto (PGobject.)
    (.setType type)
    (.setValue value)))

(def ->color (partial ->pgobject "type_color"))

(def enum-R (->color "red"))
(def enum-G (->color "green"))
(def enum-B (->color "blue"))
```

Now you may pass these `enum-X` values into queries:

```clojure
(insert! :test {:color enum-B})

({:id 4,
  :url nil,
  :uuid nil,
  :created_at #inst "2018-08-20T15:40:13.042937000-00:00",
  :color "blue"})
```

Blue, as expected.

Since you are familiar with `PGObject`, let's bring one of the main Postgres
features to Clojure. I'm talking about `json(b)` type. In the latest Postgres
releases you are welcome to store JSON data not as a plain string but a
structured type. There are dozens of functions and operators to query its
subfields, merge two objects into one, etc. The `jsonb` type even stores its
body as a binary structure rather than a string so most of the operations
perform really fast.

Personaly, I'm not a big fan of storing everything in JSON. Strict schema is the
main benefit of Postgres so using JSON a lot quickly turns your database into
MongoDB. You may easily end up with such a situation when one half of your JSON
dataset has certain field but the rest doesn't. Scanning the whole database with
a script is annoying and frigile.

On the other hand, in some cases you may succseed by dumping some minor data
into a `json` column. A good example might be working with PayPal
notifications. When a user does something, PayPal triggers a handler on your
server that we usualy call a webhook. PayPal sends plenty of data in such a
notification. There might be up to 30 fields to specify all the user info, their
local and business address, product info, tax info and so forth.

Depending of a kind of a business event, a set of fields may vary. Maintaning a
SQL table with 30+ columnds would be a mess. The better approach is to take only
the most important fields from that set (user name, product ID, sum) and save
them in proper columds. But to prevent the rest of the data from being lost, you
may just dump it into the `json` column. If it turns out you need more info out
from there, you will solve it with a couple of SQL lines.

The following example highlights how you can detach a nested JSON field into a
separate column:

```sql
alter table ipns add column user_email text;
udpate ipns set user_email = data#>>'{user,email}';
```

The great idea would be to read and write JSON using CLojure maps. We've got to
plug in a new Clojure library for processing JSON:

```clojure
;; in your project deps
[cheshire "5.6.3"]

;; at the top of our module
(:require [cheshire.core :as json])
```
Let's add a new column to our test table and extend certain protocols:

[cheshire.core :as json]

```clojure
(execute! "alter table test add column data json")

(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentMap
  (sql-value [val]
    (->pgobject "json" (json/generate-string val))))
```

Now that you pass a native Clojure map as parameter for `data` field, it will be
turned into a `PGObject` that Postgres driver knows how to treat. But querying
the table still returns `PGObject` that is not we expect. There is another
`IResultSetReadColumn` protocol to specify how the data come from the driver
should be processed.

```clojure
(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata index]
    (pgobj->clj pgobj)))
```

For each value received from the databse, the protocol calls
`result-set-read-column` function dispatched from the value type. The function
accepts an instance of `PGObject` class, a metadata which is some sort of
additional info and an index of that value as an integer. The function should
return any Clojure value which will take place in the final result when you
query the database.

Since `PGObject` represents not only `json(b)` column type but any non-primitive
entity, it would be a mistake to just parse it as JSON. Instead, we've got to
implement some sort of sub-dispatching mechanism so only objects with of `json`
type are processes as JSON. Once you've added other types you'll be able to
extend the dispatching so other types are processed properly.

In our case, we will dispatch an instance of `PGObject` not by its type
(obviously, it will always be the same) but rather by a value returned from the
`(.getType)` method. There is a great opportunity to take a multimethod onboard:

```clojure
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
```

Since both `json` and `jsonb` differs from JDBC prostective, we have to extend
the `pgobj->clj` multimethid twice for each type. But the logic is the same so
we wrap it into `json->clj` function to prevent copy-paste.

Quck check:

```clojure
(insert! :test {:data {:foo {:bar {:baz [1 2 3 true false nil "hello"]}}}})

(query "select id, data from test where data is not null")

({:id 7, :data {:foo {:bar {:baz [1 2 3 true false nil "hello"]}}})
```

That's a really great feature we've implemented so far. Any data that is
JSON-serializable can be dumped into the database and restored to the original
form. Use `json` type for storing fuzzy data that is subject to change over the
time. But don't abuse that feature since it leads to missing the main benefit of
PostgreSQL: a strict schema protecting you from dull errors.

The last trick you will learn in that session is how to work with Postgres
arrays. Postgres provides nice typed arrays with dozens of functions to operate
on them. Here is a short list of their benefints and use cases.

Arrays are strictly typed. If you declare an array of integers, a string cannot
take a place in such an array. Instead, a `json`-based vector may contain
literaly everything inside.

Arrays support special operators to concatenate or subtract them, to find an
intersection or to check if one array is supset of other. Somethimes it
simplifyes logic of a query. Imagine you've got a job and a CV entities in the
database. Let a job has `skills_required` column which is array IDs. So does a
CV entity that has got a `skills_owns` of the same type.

To select all the CVs that have all the skills specified in a certain job, you
compose a query in such a way:

```sql
select job.*
from
  cvs cv,
  jobs job
where
  cv.id = ?
  and cv.skills_required <@ job.skills_owns
```

The `<@` operator is called "is contained by" and returns true if all the
elements from the left array are found in the right side array.

If you would like to reduce the strictness of a query, use `&&` "overlaps"
operator that returns true if both arrays have at least one common element.

One more benefit of using arrays is they prevent you from creating extra bridge
tables. If you store skills in a separate table you will have to declare two
bridge tables `jobs_skills` and `cv_skills` and link entitites through them. The
complexity of the DB schema and queries will increase. But storing skill IDs as
an `intarray` column will keep the things simple.

Moving to practice, let's create an array field and tie it to a Clojure
vector. We'll insert at least one row using SQL command since we cannot do that
yet with Clojure:

```clojure
(execute! "alter table test add column skill_ids integer[]")
(execute! "insert into test (skill_ids) values ('{1,2,3}'::integer[])")
```

Querying the database returns a new object for an array:

```clojure
(query "select id, skill_ids from test where id = 8")

({:id 8,
  :skill_ids
  #object[org.postgresql.jdbc.PgArray 0x4afa8619 "{1,2,3}"]})
```

For reading arrays from the database we have to extend the
`IResultSetReadColumn` protocol with the `PgArray` class. Import the class
first:

```clojure
(:import org.postgresql.util.PGobject
         org.postgresql.jdbc.PgArray)
```

And extend the protocol:

```clojure
(extend-protocol jdbc/IResultSetReadColumn

  PgArray
  (result-set-read-column [pgarray metadata index]
    (let [array-type (.getBaseTypeName pgarray)
          array-java (.getArray pgarray)]
      (with-meta
        (vec array-java)
        {:sql/array-type array-type}))))
```

The code just gets Java native array from the `PgArray` and turns it into a
Clojure vector calling `vec` function. An interesting idea here is to preserve
the base type of Postgres array in the result's metadata:

```clojure
(def _res
  (query "select id, skill_ids from test where id = 8"))

({:id 8, :skill_ids [1 2 3]})

(-> _res first :skill_ids meta)

#:sql{:array-type "int4"}
```

Turning a Clojure vector into a DB array is a bit tricky. First, we need to
refer that array type stored in metadata. Second, we have to refer the current
DB connection to create an instance of the DB array:

```clojure
(extend-protocol jdbc/ISQLParameter

  clojure.lang.IPersistentVector
  (set-parameter [val stmt ix]
    (let [conn (.getConnection stmt)
          array-java (into-array Object val)
          array-type (-> val meta :sql/array-type)
          array-pg (.createArrayOf conn array-type array-java)]
      (.setArray stmt ix array-pg))))
```

A quick check:

```clojure
(insert! :test {:skill_ids ^{:sql/array-type "int4"} [10 20 30]})

(query "select id, skill_ids from test where id = 9")

({:id 9 :skill_ids [10 20 30]})
```

By the way, we may skip the metadata when passing a vector but JDBC still
handles the type properly. But to prevent weird things from happening, it's
better to specify the type anyway. Pay attention that most of the
data-processing functions lose metadata belongs to the origin vector. So if you
take a vector from the query result and process it somehow, you'd better to save
its metadata somewhere and attach it to the result vector sent to the database.

So far, we've made significant progress on boosting up our database module. We
established connection between low-level Postgres types and native Clojure maps
and vectors. What I'd like to highlight again is, the solution looks simple and
easy to tweak. THe whole code takes less than 100 lines and follows the Clojure
way.
It's much more convenient to have a domain that you may tweak from project
to project focusing on what you exactly need rather than dealing with monstrous
ORM library.


Java IO Streams

In that chapter, we will work a lot with IO operations. I'm going to consider a
case when you need to process huge amoints of data flows through the network.

As we discussed before, Clojure is a guest system that doesn't try to implement
low-level capabilitites of its host. Streaming data from remote resources is one
of such things that Clojure cannot perform by design. Instead, Java provies rich
capabilities for any imaginable IO operation. So to perform accurate IO in
Clojure we've got to borrow some Java stuff to the project.

Imagine a zipped CSV file with about 7 millions records somewhere on the
server. You need to download and process all the data from it. Probably, your
first steps would be to download an arhive, then unpack it, abd then process a
file. That's alright for a quick and draft solution when you are only interested
in how to make it work.

In general, this step-by-step algorithm suffers from long blocking
operations. Downloading a 600 Mb file will probably take a couple of
minutes. Extracting 6 Gb file would take about the same time. When a program
stats, it should check if a file was already downloaded to prevent doing it
again. It also should clean all the traces left on disk afterwards.

Another pitfall would be to rely a lot on such shell utilities as `wget`,
`unzip`, `sed` or whatever. Although most of them are really piece of art, it
would be difficult to embed them into Clojure code and build the pipeline. In
fact, even minor usage of shell utilites turn the whole your code into a bash
script.

The behaviour of shell tools vary on their version and OS family. At the moment
of writing this, the `unzip` command on my laptop cannot handle the subject file
which is larger than 2Gb due to some known bug. You just cannot foresee such a
case when sharing the codebase across Mac and dozes of Linux
distributions. Instead, the JVM code works as expected on all machines.

What I propose is to process the data on the fly using Java IO
capabilities. Since CSV is a plain text, there is no need to wait untill whe
whole file is downloaded and unpacked.

First, we send an HTTP request for that file specifying we would like to
proccess ins body not as a string but a binary stream. In Java, a stream is an
abstraction that produces data on demand from some source without flooding the
whole memory. In our case, such a stream represents binary zip file.

To read zip's content, we wrap the binary stream with a special
`ZipArchiveInputStream` stream that knows how to treat the input data. The
archive stream doesn't read the whole input but only few leading bytes to know
what's inside it. Having that table of content, we may seek for a file we need
by its name or extension and get a new stream represents exactrly the file we
were looking for.

To parse CVS data into rows, we pass that last stream into a CSV reader that
consumes it and fetches native Clojure data structures. Here, we will wrap them
with a function that cleans unnesseserely fields and coerces values to proper
types. Finally, those clear final maps will be saved to the database by
partitions of 1000.

What I would like to hightlight is the whole pipeline starts wirking
immedietly. A sequence of data that we have in the end is lazy and works on
demand. For example, you may take just ten first records without downloading the
whole file. Another benefit is you may show the progress printing how may recors
have been fetched so far. Finaly, the data doesn't touch the disk so you are not
bound to free space limitations. If a file grew ten times in size, you would
still process it.

Let's prepare dependencies we need:

```
[org.apache.commons/commons-compress "1.5"]
[clj-http "3.7.0"]
[org.clojure/data.csv "0.1.4"]
[org.jsoup/jsoup "1.11.3"]
[org.clojure/java.jdbc "0.6.1"]
[org.postgresql/postgresql "42.1.3"]
```

That's a bit more than we used before. Let's go through the list quickly:

- `commons-compress` is Java library to handle zipped data. Since Java SDK
  already ships similar functionality out from the box, the external Apache
  library handles some issues with encoding and thus is more reliable;

- `clj-http` is a great Clojure wrapper around Apache HTTP client to send HTTP
  requests;

- `data.csv` is a simple but useful library to read and write CSV data;

- `jsoup` is a tool you have met before to clean HTML data. In that chapter, you
  will find another way to use it.

- `java.jdbc` and `postgresql` are to to write the result into the database.

Prepare a new module with all the stuff imported:

```
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
```

[npi-files]:http://download.cms.gov/nppes/NPI_Files.html

The file we are going to process is known as "NPI Registry" and represents data
about American practitioners and healthcare organizations. A link to that file
is updated monthly and might be found on the official [NPPES site][npi-files].

The first problem we are goint to solve is the URL includes the current month
name. Although it is trivial to retrive it, the date part is a subject to change
and has been altering over time. Instead, we will parse the HTML page and find a
target link using Jsoup.

```clojure
(defn find-url
  []
  (let [doc (.get (Jsoup/connect files-page))
        selector "a[href~=NPPES_Data_Dissemination_\\w+_\\d{4}\\.zip]"
        links (.select doc selector)]

    (some-> links
            first
            (.absUrl "href"))))
```

This function returns a full URL we need as a string. At the moment of writing,
it was `http://download.cms.gov/nppes/NPPES_Data_Dissemination_August_2018.zip`.

To get its binary stream, we send an HTTP request passing special parameter:

```clojure
(defn get-file-stream
  [url]
  (:body (client/get url {:as :stream})))
```

The result will be a special object represents a stream:

```clojure
#object[clj_http.core.proxy$java.io.FilterInputStream$ff19274a 0x4b3d87e3 "clj_http.core.proxy$java.io.FilterInputStream$ff19274a@4b3d87e3"]
```

Then we convert it to a zipped stream:

```clojure
(defn ->zip-stream
  [stream]
  (new ZipArchiveInputStream stream))
```

Now we try to find a file we need in that stream:

```clojure
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
```

Pay attention we are trying to operate on short functions rather than wrapping
the whole logic into a single one. Keeping things apart helps to maintain
simplicity which is crutial.

The function takes a zipped stream and a regex patter. It iterates through zip
entries checking if its name matches the pattern. A zip entry is some sort of
metadata about a file. Switching to the next entry also shifts an internal
pointer of a stream indicates where to start reading from.

Iteration stops once we found an entry with appripriate name. The result will be
either a map with basic entry info or `nil` value meaning we didn't manage to
find an entry which name satisfies the pattern.

A file in archive that we are interested in is called
`npidata_pfile_20050523-20180812.csv` so the regex would be:

```clojure
(def re-csv #"(?i)_\d{8}-\d{8}\.csv$")
```

Right after the stream has been aimed to a proper file, let's process the data
with a CSV reader:

```clojure
(defn read-csv
  [stream]
  (let [reader (io/reader stream)
        rows (csv/read-csv reader)
        header (map clean-header-field (first rows))]
    (for [row (rest rows)]
      (zipmap header (map clean-row-field row)))))
```

First, it reads the first header line and turns its names into keywords passing
them through the `clean-header-field` function. I'll omit its declaration since
it just operates on strings, cleans unnesecery symbols, change the registry and
so on. For example, the `Entity Type Code` caption becomes `:entity-type-code`.

Another `clean-row-field` function subsctitutes dummy values like empty strings
or `"<UNAVAIL>"` to nils.

The final function takes a map with keywork keys and cleaned vales and returns a
some business model. In our example, we take just a certain subset of that map
but in real case we would do something more complicated. We will map a sequence
of CSV rows on that function to get a sequence of business models.

```clojure
(defn ->model
  [row]
  (select-keys
   row [:npi
        :entity-type-code
        :provider-first-name
        :provider-credential-text
        ;; other fields...
        ;; TODO fields
        ]))
```

It's time to save our results to the dabase. Inserting models one-by-one
requires performing 6M inserts which is not good. On the other side, a single
insert obligates us to collect all the dataset in memory what is exactly we are
trying to avoid. The golden middleware would be to insert data by chunks, say
dump each 1000 models into the database.

Here is a short function that takes a lazy sequence and retuns a lazy chunked
sequence:

```clojure
(defn by-chunks
  [coll n]
  (partition n n [] coll))


(by-chunks [1 2 3 4 5] 2)

((1 2) (3 4) (5))
```

To insert multiple records in the database at once, implement a shortut for
JDBC:

```clojure
(def db
  {:dbtype "postgresql"
   :dbname "clj-db"
   :host "127.0.0.1"
   :user "clj-user"
   :password "clj-pass"})

(def insert-multi! (partial jdbc/insert-multi! db))
```

Ok, everything have been implemented so far so it's time to compose a final
combo. Here is how get a sequence of models:

```clojure
(defn get-models
  []
  (let [file-url (find-url)
        stream-bin (get-file-stream file-url)
        ztream-zip (->zip-stream stream-bin)
        entry (seek-stream ztream-zip re-csv)]

    (assert entry (format "file %s not found" re-csv))

    (let [rows (read-csv ztream-zip)]
      (map ->model rows))))
```

Let's fetch some leading models from the server:

```clojure
(take 3 (get-models))

;; truncated
({:npi "1679576722"} {:npi "1588667638"} {:npi "1497758544"})
```

That really works and the data arrives almost immediately. Now imagine you have
to wait for five minutes before accessing the data. That would be unbearable.

Now save the data into the database by chunks:

```
(defn save-models
  [models]
  (doseq [chunk (by-chunks models 1000)]
    (insert-multi! :models chunk)))
```

Of cause, a table `models` with a proper structure should be created in advance
in the database.

Alright, it was a tough route but we've managed to deal with all the
pitfals. Thanks to Java IO capabilities that helped us to build the
pipeline. What might be improved here is handling exceptions on each step and
logging them. I believen you wouldn't like to fail in the middle of process just
because one record is corrupted. Another feature would be to load the data in
parallel using futures or `pmap` to archive perfomance boost. For the rest, the
subject issue is complete so we move to the next class.


Processes and Browser automation


Out final coding session will be about managing processes. As I have mantioned
before, Clojure is not designed to control systems processes since these are
obligations of a host system that Clojure relies on. But still, with neat
Clojure data structues and syntax we may build wrappers for that.

The task we are going to solve is to automate the browser intalled on your
computer. We would like to have such functions as `goto` or `screenshot` force
the browser open a web-page and save its content as an image.

To perform those actions, we need to start a command line utility that generaly
is called a webdriver. For Google Chrome it is `chromedriver`, Firefox has got
its `geckodriver`, Safari browser brings `safaridriver` out from the box.

When launched, each utility finds its own browser installed on your machine and
establishes low-level connection with it through a socket. At the same time, it
starts a web server that handles HTTP REST requests. These requests command the
browser to perform certain actions, say open a page or click on a link. Under
the hood, the webdrver translates high-level requests into binary data satisfies
browser's internal protocol.

The whole pipeline we are going to go through is:

- spawn a new process by launching a webdriver utility;
- connect to the local HTTP server and init a new session;
- having that session ID, send some requests to the server to let the browser do
  what we want;
- close the session, quit the process.

Some preparations first: we are going to work with Google Chrome and its
`chromedriver` binary tool. I'm sure you have Chrome installed already. To
achieve the driver, run in your terminal:

```bash
brew install chromedriver         # Mac
sudo apt-get install chromedriver # Ubuntu
```

[chromedriver-dl]:http://chromedriver.chromium.org/downloads

If you've got Windows system or something went wrong during the installation,
download precompiled binary from the [official page][chromedriver-dl].

After the installation, try to lauch the driver manually. In case you installed
it from packages, it should be in you `PATH` variable so you can run it from any
directory just typing `chromedriver`. If you downloaded the file manually,
specify the full path which is `/Users/ivan/Downloads/chromedriver` in my
example.

The following message in the terminal indicates the driver works fine:

```
Starting ChromeDriver 2.41.578706 (5f725d1b4f0a4acbf5259df887244095596231db) on port 9515
Only local connections are allowed.
```

Now quit it and turn back into the editor. Add the dependencies we need:

```
[clj-http "3.7.0"]
[cheshire "5.6.3"]
```

There is everything you are familiar so far. Prepare a new file named `proc.clj`
with a namespace declaration:

```
(ns project.proc
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [cheshire.core :as json])
  (:import (java.lang ProcessBuilder Process)
           java.util.Map
           java.io.File))
```

One note here, since both `ProcessBuilder` and `Process` classes belong to the
`java.lang` package they are available by default and there is no need to import
them. But usualy I keep such declarations to stress the fact the logic of the
namespace relies on Java capabilities.

The `ProcessBuilder` class is aimed to prepare the further process step by step
before you start it. At least we need to specify a command with its
args. Sometimes, setting additional environment variables is required as well as
STDOUT or STDERR redirection into files. Redirection is crutial since most of
the tools send plenty of data into both channles especialy when a `--verbose`
flag is set.

Taking all together, let's write a simple Clojure wrapper:

```clojure
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
```

Its logic is really straintforward. First, it turns a collection of args into a
Java array of strings. It's important since some arguments might be integers,
e.g port numbers. The function `args->command` is trivial except one thing:

```clojure
(defn ^"[Ljava.lang.String;"
  args->command
  [args]
  (into-array String (map str args)))
```

Take a look at its type declaration. We have to specify that the result is Java
array of strings because otherwise Clojure compiler will complain it cannot
detect a proper constructor. It happends when there are more then one methods or
costructors with the same arity to Clojure needs to know their types. This is a
rare case when type hints are not optional but mandatory.

Next, some imperative code follows. For example, if extra env varialbes were
passed in an optional map within an `:env` field, we merge them into the
builder's variables. Here are the functions that do that:

```clojure
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
```

Here, `kw->env` is just an utility function that turns `:foo-bar` into
`"FOO_BAR"` for example.

If any of `:path-out` or `:path-err` strings were passed, we redirect
corresponding channels into files wrapping then fith the `File` object.

In the end of the function, we spawn a process and return its intance. This is
the moment when the process starts. If it was a GUI application, its window
should appear.

The `Process` object provides a few of methods only two of them we are intrested
in. These are `.destroy` and `.exitValue` to stop the process and check it had
stopped successfuly. Once we finish communicating with the driver's HTTP
seriver, we've got to call them.

Here is how I start the driver:

```clojure
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

(def _proc (start-chrome))
```

[w3-webdriver]:https://www.w3.org/TR/webdriver/#list-of-endpoints

Now that the driver is working, I'm gointg to call some of HTTP API to show how
it works. The Webdriver protocol is not the main subject of our topic so I won't
dive deep into it. For those of you interested in I highly recommend looking
through the official [Webdriver documentation][w3-webdriver].

So far, none of Chrome windows have opend because we didn't initiate a
session. Let's obtain it:

```clojure
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

(def _sess (init-session))
```

At the moment, a new black window of Chrome browser should appear. This is
really magic, isn't it?

The `_sess` varialbe is a long string that represents an automation
session. Usualy, a webdriver handles only one session at once.

Let's open Wikipedia:

```clojure
(defn goto-url
  [session url]
  (client/post
   (make-url "session" session "url")
   {:as :json
    :content-type :json
    :form-params {:url url}}))

(goto-url _sess "https://en.wikipedia.org/")
```

The blank window should load the Wikideia content. Let's search for
something. to interact with any element on a page, we need to know its ID
first. Don't mix it with the `id` HTML attribute. Instead, this is a long string
that identitifes a DOM node in browser's memory, for example
"0.5383067151615304-1".

```clojure
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
```

This function finds an element using XPath expression and returns a long ID. It
will be a foundation for two high-level wrappers for inputing text and clicking
on somethinq:

```clojure
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
```

The following code inputs "Clojure" in a search field and clicks a loupe button:

```clojure
(input-text _sess ".//*[@id='searchInput']" "Clojure")

(click _sess ".//*[@id='searchButton']")
```

So the browser is redirected to the `https://en.wikipedia.org/wiki/Clojure`
page. To quit the session, send a new request:

```clojure
(defn delete-session
  [session]
  (client/delete
   (make-url "session" session)))

(delete-session _sess)
```

At this moment, the browser window should dissapear. Remember, the proccess we
started before still works. If anybody tries to start it again, they will get an
error saying the post is already used. So we stop the process manually:

```clojure
(defn stop-process
  [^Process p]
  (when (.isAlive p)
    (.destroy p)
    (.waitFor p)
    (println (.exitValue p))))

(stop-process _p)
```

But forcing a programmer to keep in mind all the resource he or she should
release aftewards is a bit tedious to them. Moreover, an exception might raise
somewhere in the middle when calling the API so the process will hang. It would
be much to create a macros that spawns a process, binds it to local variable and
stops it no matter if there was exception or not.

```clojure
(defmacro with-process
  [[bind & params] & body]
  `(let [~bind (proc-start ~@params)]
     (try
       ~@body
       (finally
         (stop-process ~bind)))))
```

The first argument of that macro is a vector which's fist element stands for a
loca variable a process instance should be bound to. There rest of the vector
are parameters to the `proc-start` function and the `body` is an arbitrary
Clojure code to execute.

```clojure
(with-process
  [proc ["chromedriver" "-p" 9999] {:env {:debug 1}}]
  (let [session (init-session)]
    (goto-url session "http://exampple.com")
    ;; any other code
    (delete-session session)))
```

[etaoin]:https://github.com/igrishaev/etaoin

By the way, what we have implemented so far is a sceleton of a Selenium-like
softaware that automates the browsers. Generaly speaking, it works the same way:
starts a driver's process and sends HTTP requests to the local server. There is
also a pure Clojure library [Etaoin][etaoin] that implements the official
Webdriver API. The code snippets from our session are just simplified fragments
borrowed from the Etaoin's codebase.


Conclusion

So the previous session was the last one on that book. I hope that most of the
readers who have been reading it still have managed to reach these lines. My
praise to them!

It was a tought material I confess. So far, we've dug through lots of Java
classes and protocols which is not you usually read in ordinary
tutorials. Rather than sorting a vector of integers, I tried to bring the very
teste of production development. This is where pure Clojure fundamentals may
ruin a bit. Such a code might be not functional at all, it relies on mutable
objects and their state. It involes reading Javadocs a lot rather than toying
with nice Clojure data structures. But at least Clojure reduces the pain you
usualy suffer from working with other languages.

Getting back to the question from the beginning of a book, let me summarize. No,
you don't have to know Java before you get into Clojure. Yes, professional
Clojure code relies on Java a lot. But don't be afraid of it. Even going in
small steps is reasonable when the road goes uphill. So the Clojure way does.

Ivan Grishaev,
Voronezh, Russia
2018
