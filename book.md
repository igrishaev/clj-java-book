

Intro


Many people who have just started with Clojure or just think to start

I often hear that question especially from those who are new with Clojure: do I
need to know Java?


I clearly remember that same thoughts have been worried me before I dove into
the Clojure world. I knew Clojure relies on Java VM havily but unfortunately
I've got poor Java experience in the past. Just some occational fixes of legacy
code and one Android app almost completely copy-pasted from public manuals.

Those bits of Java knowledge were not sufficient to feel confident with the
platform, I thought. How will I tweak the garbage collector? What if I need to
call some legacy Java class from Clojure? Will my Java-skilled teammates
laughing at me pointing out I just conjure on magic language without any
knowledge of what's going on under the hood?

My eagerness for Clojure finaly won, and today I'd like to summarize the
folling. The short answer to the question of our topic is no, you don't need to
be proficient in Java when starting with Clojure. If you are new to that
language, don't be afraid of all this Java machinery will suddenly come down in
you. Perhaps you'll touch Java gust one day, but it will happen in many days or
months since you write the first line in Clojure. I think most of you who have
been doubting feel more confident now.

By default, Clojure brings everything you need for a quick start just out from
the box. There are great collections, shortcuts to read and write files and
special reference types to keep state. Plenty of useful libraries might be
connected to your project by adding a single line into a config file. Most of
them provide clear API that usualy take a map and return some native Clojure
data.

For example, to start a web-server, you declare a handler function that takes a
map and returns a map. You only need to write the document describes demantics
of each field of those maps. The same approach works for fetching data via HTTP
protocol: again, the input and output data are just maps probably with nested
vectors or maps. Sor far, most of the problems you are trying to solve have
already been completed with great libraries.

Let me stress it again: no, you don't need to be a Java expert. If Clojure
backons you, start your jurney with no doubts.

The long answer will take a bit longer since it tries to be more realistic. If
you program in Clojure regulary and it has become you primary language, sooner
or later you will have to deal with Java. This is inevitable. The more business
problems you are trying to solve, the more tools and levers you should operate
on. The underlying Java world is full of such tools and you would be better to
get close to them.

Java is a platform has been developing for several decades. There are both
positive and negative opinions on it,

Let's briefly go though a list of cases when you might need Java
interoperability.

Reuse Java code

For the whole history of Java, millions lines of code have been written. A
significant part of that code is open source. Thouthands of people work every
day on fixing bugs and adding features. Some of the libraries haven't been
updating for several years not because they are abandoned but because they are
complete. They solve the problem they were aimed at with all the known issues
solved.

In Clojure, one day you will face such a non-standard task as parsing some weird
binary format or communicate with a third party service. Everytime you start
writing your own solution for that, please check if you invent a wheel. Because
most likely there is a Java library for that, being developed and debugged for a
year or two. You might think your version is better and clearer, but no, it is
not. Qualitative sofware really takes years to become at least sufficient, not
even great. Such a simple task as parsing an RSS feed or User-Agent header keeps
dozens of hidden tricks. For your own sanity, you'd better not to open Pandora
box by writing everything from scratch.

Many well-known Clojure libraries are just thin wrappers around solid Java
code. Inside, it might look ugly, full of state and tricks, getters/setters,
etc. But it works, which is most important. Reusing Java code really boosts
Clojure development process, consumes time and nerves.

On of the chapters of that book provides a real example of how to tame Java code
and pack into a Clojure library.


IO

Another reason for writing low-level Java code is to take control over IO
operations. Java provides mighty capabilities for sending and accepting data no
matter if we deal with files, network or processes. Instead, Clojure was
designed in terms of being a guest system. It re-uses the host environment (Java
in our case) to provide high-level abstractions, e.g flexible collections, smart
multimethod dispatching, etc. But it doesn't try to overshadow such the original
low-level features as input and output, interaction with operation system and
processes.

Clojure ships some wrappers around Java's native IO subsystem and usualy they
are enough to write or read a file. But one may deal a situation when they need
streams and even piped streams when huge data flows though a pipeline being
transformed on the fly without touching the disk. This is the case wneh you
defenitely need Java interop.

One of the examples of that book clearly shows a real case with multiple streams
and how to control them in Clojure.


No sources

It might happen that the project you are workin on relies on a special feature
that's difficult to implement. Say, analysing fingerprints, face or voice
recognition. Your customer may have a licence for a commercial library written
with Java. Probably they've been using this library in all their Java projects
so the Clojure solution should also follow it to make result relevant. So you've
got a jar file without souces and an HTML file next to it with a list of
availabe classes and methods.

Surely you can decompile it and look at some of its guts, but probably it won't
change anything. Sometimes, you are really clamped by business demands so
writing your own pure Clojure solution is out of discussion. In that case, you
have to go down to the Java basement.


Your carreer growth

The final and the most general reason for inventing time into Java interop is
your carreer growth. If you program in Clojure for daily basis, the businesses
will offer more and more opportunities for you. To deal with them, you'll need
to broaden your horizons. Diving into low-level Java world is exactly such a
thing that will boosts your skills and thus carreer growth. If you fell in love
with Clojure and feel confident with your intentions, you cannot bypass Java
interop.


That was the long answer for the question if you need to know Java. So far, I
hope you feel much more confident than before and nothing prevents you from
getting started with Clojure.


What is that book about

This paper is focused on one narrow theme: how to use Java capabilities in
Clojure. There are plenty of tutorials and books about Clojure so far, but most
of them consider their readers being new ones with Clojure. Thus, they mostly
consist of such basic subjects as defining functions and transforming
collections so there is no room for extended topics.

Java interop is one of such tipics. When you learn Clojure, usualy you are
focused primarely in the language by its own: functions and namespaces, atoms,
etc. Everything looks simple and elegant so far. But onve you've got a real
Clojure job, it turns out the codebase is full of tricky calls to third-party
Java classes. Here, the real work starts.

You feel confused because the code doesn't look like the tutorials used to set
out. So the aim of that book is to get you prepared for such a
sutiation. Together, we will go through a set of chapters each of them expounds
something usefull on Java interop.


The structre

The first chapter brings basic information about how to import and call Java
classes and methods. We'll consider common Clojure ways to interact with the
host system. I'll operate on such basic Java classes as `java.io.File` or
`java.util.Date`. They have been in Java for decades and thus are familiar to
everyone who has ever taken a look at Java tutorials.

Then, a set of code-driven sessions start. I would like to take apart several
business cases when you solve a problem writing Clojure code relying on Java
capabilites. What I'd like to highligh is all the cases are real and borrowed
from the projects I've been worked personaly. None of them are taken out from my
head just to make my statements louder, no. Everything described in that book is
real.

The first coding session shows how extract as much information as possible from
a User-Agent header comes to the server. This is definitely such a task that
would be better not to do with bare hands. I'll show how to plug in a sensible
Java library, call proper classes and turn the result into plain Clojure data.


The second task affects an intresting topic: how to clean HTML data from
unwanted and malicious data. It doesn't occur often in our daily job, but once
it does, the days and weeks might be taken to overcome it. Again, we will rely
on prooven Java solutions. As a small bonus, I'll show how to deal with treaky
business rules on media resources.

;; TODO no gis

In the next chapter, let's talk about how to extend your JDBC driver to let it
work with non-primitive data types. I consider this chapter being quite
important since a database is the most valuable part of the project. The more
features your database driver can to perform, the less code you'll need in the
business logec.

I'll demonstrate how to build seamless connection between native Clojure data
and PostgreSQL backend. The second feature will be wrapping PostGIS data types
with Clojure so you can control over geo-spacial data.


The fourth code sample highlights Java IO capabilities, namely different types
of input streams and how to conjoin them together. I'll show how to build a
pipeline that pulls out a huge amount of data from a zipped CSV file without
touching a hard drive or consuming the whole available memory.


The final coding chapter explains how to take control over Java processes. Since
most of the business tasks might be solved with Clojure or Java code, sometimes
you need to communicate with a third-party programm installed on your
computer. Since Clojure brings poor capabilites for running external programs,
we'll take a look a close gaze on the subject.


I believe, there has been said enough to make you being intested. I hope you
cannot wait to go further. So am I.



Interop


Parsing User Agent header

OK, let's get some coding. The problem we are going solve is the
following. Imagine we are developing a sort of analytic system that should
collect as much information about a user as possible. Businesses prescribe us to
store data about users' operations systems, their verson and family, browsers,
if they were using mobile devices and what exactly (iOS, Android, etc).

Most of this information might be borrowed from a User-Agen HTTP header. It's a
string that brings almost everything we need. Every time a user opens a
web-page, their browser sends this string. Here is mine, copied from Google
Chrome:

```text
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like
Gecko) Chrome/67.0.3396.99 Safari/537.36
```

The problem with User-Agent is, it is quite bad organized. For decades of
chaotic web-development, browser monufacturers have been dumping more and more
data there. What it ended up with, there is no a single and clear rule to parse
User-Agent.

Doing that manually is one of those tasks that look simple, but turns into a
mess full of nested `if`. The right decision would be to import a Java library
made for this purpose.

The whole process consisits of three stages. The first one, we find a proper
library and plug it in into our project. Then we add a new module with code that
calls that library. On the third step, we turn the result into habital Clojure
structures, usualy a combination of maps and vectors.

[uadetector-site]: http://uadetector.sourceforge.net/

The library I decided to pick up is [UA Detector][uadetector-site]. It recognizes
plenty of patterns for desktop computers, mobile devices and web-crawlers
(Goole, Yahoo, etc). In your `project.clj` file, and the following into the
`:dependencies` vector:

```clojure
[net.sf.uadetector/uadetector-core "0.9.10"]
[net.sf.uadetector/uadetector-resources "2014.10"]
```

It looks strange that we added two lines but not just one. This is because of
the architecture of a library. It consists of two parts: common API and
resources.

The common part, `uadetector-core`, provides high-level API so you call them
without bothering what's under the hood. The resource part,
`uadetector-resources`, plays role of a database of known patterns. Such design
brings certain benefets. If a new portion of User Agent patter occures across
the Internet, there would be enough to bump only `uadetector-resources`
dependency keeping `uadetector-core` at the same version.

Moving to the step two, let's create a separate namespace where all the Java
interop will be stored and add some draft lines:

```clojure
(ns project.ua
  (:import [net.sf.uadetector.service
            UADetectorServiceFactory]))

(def ^:private parser
  (UADetectorServiceFactory/getResourceModuleParser))

(defn parse [^String user-agent]
  (.parse parser user-agent))
```

The main detail here is we created a `parser` object which is an instance of
`UserAgentStringParserImpl` class. Since most of the Java objects are mutable,
it is better to keep that object being private so nobody can affect it from the
outside of our namespace.

The `parse` function accepts a User-Agent string and calls `parse` method
against that object passing the string as the first argument. Pay attention on a
type hint. When playing with Java objects, type hints help a lot not only to
compiler to dispatch a proper method, but also to us, human being.

If we call the function with some sensible user agent sample:

```
(def ua-sample
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like
Gecko) Chrome/67.0.3396.99 Safari/537.36")

(def result (parse ua-sample))
```

, the result will be an instance of `net.sf.uadetector.UserAgent` class. Its
string representation looks like this:

```
#object[net.sf.uadetector.UserAgent 0x3acfecaa "UserAgent [deviceCategory=DeviceCategory [category=PERSONAL_COMPUTER, icon=desktop.png, infoUrl=/list-of-ua/device-detail?device=Personal computer, name=Personal computer], family=CHROME, icon=chrome.png, name=Chrome, operatingSystem=OperatingSystem [family=OS_X, familyName=OS X, icon=macosx.png, name=OS X, producer=Apple Computer, Inc., producerUrl=http://www.apple.com/, url=http://en.wikipedia.org/wiki/Mac_OS_X, versionNumber=VersionNumber [groups=[10, 11, 6], extension=]], producer=Google Inc., producerUrl=http://www.google.com/, type=BROWSER, typeName=Browser, url=http://www.google.com/chrome, versionNumber=VersionNumber [groups=[67, 0, 3396, 99], extension=]]"]
```

This definiteley not a Clojure structure and thus cannot be used with core
functions. But the fields inside confirm we managed to parse something. Here,
the step two ends and we start thinking on how to make the result more
Clojure-friendly.

To Clojure recursive pattern

Let's remind how do they store data in Java. Usualy it's an instance of a
top-level class that keeps references to other instances of lower ranks. For
example, an `UserAgent` object keeps references to the `DeviceCategory` and
`OperatingSystem` objects. These classes store some other minor data and so
forth. What we've got here is a tree of objects.

To convert such a tree to Clojure, let's write a function. It accepts the
top-level object first and returns a map with keywords for keys and method calls
to that object for values. If a method returns a primitive data type, say
integer or string, we keep that as a final value. If there is a complex object,
we pass it to the same function to take it apart on a map again and so forth
until all the values are of primitive types.

This is what I name "recursive to-Clojure pattern".

The function we are going to write should be not an ordinary `defn` function but
rather a part of a protocol. Each data type we need to conver to Clojue will
implement this protocol. It assures the function will operate on only sertain
types we need but not any possible value.

Here is a protocol we need:

```clojure
(defprotocol ToClojure
  (->clj [x]))
```

Now import the classes we need to extend with that protocol. In your namespace
declaration, extend the `(:import ...)` statement as follows:

```clojure
(ns project.ua
  (:import [net.sf.uadetector.service
            UADetectorServiceFactory]

           [net.sf.uadetector
            UserAgent
            UserAgentType
            VersionNumber
            DeviceCategory
            ReadableDeviceCategory$Category
            UserAgentFamily
            OperatingSystem]))
```


[javadocs-ua]: http://uadetector.sourceforge.net/modules/uadetector-core/apidocs/net/sf/uadetector/UserAgent.html


Now extend the top-level `UserAgent` class. To analyse it's atomony, take a look
at the [Javadocs page][javadocs-ua]. Briefly, we are interested in all the
getters from that class.

```
(extend-protocol ToClojure

  UserAgent
  (->clj [ua]
    {:device       (.getDeviceCategory ua)
     :family       (.getFamily ua)
     :icon         (.getIcon ua)
     :name         (.getName ua)
     :os           (.getOperatingSystem ua)
     :producer     (.getProducer ua)
     :producer-url (.getProducerUrl ua)
     :type         (.getType ua)
     :type-name    (.getTypeName ua)
     :url          (.getUrl ua)
     :version      (.getVersionNumber ua)}))
```

If we pass the `result` value into the `->clj` function, we will get a map with
a structure described above. This is great, but most of the values of that map
are still complex Java classes. We need to simplify them too.

Let's start with the `:device` field. Rather than keeping its value as-is, we
wrap it with `->clj` and then extend the `DeviceCategory` class with to-Clojure
implementation.

Fix the previous `:device` value:

```
  UserAgent
  (->clj [ua]
    {:device       (->clj (.getDeviceCategory ua))
```

Extend `DeviceCategory` and it's nested enum type:

```clojure
(extend-protocol ToClojure

  DeviceCategory
  (->clj [dev]
    {:category (->clj (.getCategory dev))
     :name (.getName dev)})

  ReadableDeviceCategory$Category
  (->clj [cat]
    (-> cat .name keyword))))
```

The threading macro (a single arrow) above gets the enum value returned from
`getCategory` method. Then it takes its name as a string and produces a
keyword. In Clojure, we usualy deal with keywords rather than strings.

No that, all the branch under the `:device` field consist only of nested Clojure
maps and primitive values. So you've got the idea: for every Java class we
describe the way it reflects to the Clojure world.

Moving on to the `:family` field:

```clojure

  ;; wrap with `->clj` that field
  UserAgent
  (->clj [ua]
    {:device       (->clj (.getDeviceCategory ua))
     :family       (->clj (.getFamily ua))

;; implementation

(extend-protocol ToClojure

  UserAgentFamily
  (->clj [fam]
    (-> fam .name keyword)))
```

Now, the `:family` field is not a Java class but a keyword something like
`:CHROME`.

Before we finish the rest of it, let's simplify something. You may notice we've
already faced a enum value a couple of times. These are
`ReadableDeviceCategory$Category` and `UserAgentFamily` classes. The code that
turns them into Clojure looks the same so it can be generalized. Who knows how
many enums we will face in further. Since all the enums exten the basic
`java.lang.Enum` class, let's extend just it leaving `UserAgentFamily` and other
custom enums alone:

```clojure
(extend-protocol ToClojure

  java.lang.Enum
  (->clj [e]
    (-> e .name keyword)))
```

So our previous implementations for `UserAgentFamily` and
`ReadableDeviceCategory$Category` might be wiped from the project in addition to
their imports in the namespace header.

This is a good sign, by the way. Deleting code means you really do something
useful. Instead, adding more code makes things worse.

Let's finish with the rest of our task. Taking apart an operating system would
be:

```clojure
(extend-protocol ToClojure

  OperatingSystem
  (->clj [os]
    {:family       (->clj (.getFamily os))
     :family-name  (.getFamilyName os)
     :name         (.getName os)
     :producer     (.getProducer os)
     :producer-url (.getProducerUrl os)
     :url          (.getUrl os)
     :version      (.getVersionNumber os)}))
```

And the last one for versioning:

```clojure
(extend-protocol ToClojure

  VersionNumber
  (->clj [ver]
    {:bug-fix   (.getBugfix ver)
     :extension (.getExtension ver)
     :groups    (.getGroups ver)
     :major     (.getMajor ver)
     :minor     (.getMinor ver)
     :version   (.toVersionString ver)}))
```

In versioning map, we return components for a version number in separated fields
and the whole version string in a `:version` field.

The last touches would be to add `->clj` into our `parse` function to it returns
a Clojure map rather than a Java object. The second thing would be to clean the
code a bit: remove unused classes and join our `extend-protocol` statements into
a single one.

```clojure
(defn parse [^String user-agent]
  (->clj (.parse parser user-agent)))

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
    {:family       (->clj (.getFamily os))
     :family-name  (.getFamilyName os)
     :name         (.getName os)
     :producer     (.getProducer os)
     :producer-url (.getProducerUrl os)
     :url          (.getUrl os)
     :version      (->clj (.getVersionNumber os))})

  VersionNumber
  (->clj [ver]
    {:bug-fix   (.getBugfix ver)
     :extension (.getExtension ver)
     :groups    (.getGroups ver)
     :major     (.getMajor ver)
     :minor     (.getMinor ver)
     :version   (.toVersionString ver)})

  java.lang.Enum
  (->clj [e]
    (-> e .name keyword)))
```

A quick test:

```clojure
(parse ua-sample)

{:producer "Google Inc."
 :family :CHROME
 :name "Chrome"
 :type :BROWSER
 :icon "chrome.png"
 :producer-url "http://www.google.com/"
 :url "http://www.google.com/chrome"
 :device {:category :PERSONAL_COMPUTER
          :name "Personal computer"}
 :os
 {:family :OS_X
  :family-name "OS X"
  :name "OS X"
  :producer "Apple Computer Inc."
  :producer-url "http://www.apple.com/"
  :url "http://en.wikipedia.org/wiki/Mac_OS_X"
  :version
  {:bug-fix "6"
   :extension ""
   :groups ["10" "11" "6"]
   :major "10"
   :minor "11"
   :version "10.11.6"}}
 :type-name "Browser"
 :version
 {:bug-fix "3396"
  :extension ""
  :groups ["67" "0" "3396" "99"]
  :major "67"
  :minor "0"
  :version "67.0.3396.99"}}
```

We've ended up with a single function and just four classes. Looks solid,
doesn't it? Find the final code on Github.

TODO GitHub.

So we've managed to re-use Java code for our business requirements. I believe
it's obvious that wrapping even clumsy Java classes is easier than writing
everything from scratch. If you doubt on that, take a weekend and try to write
your own User Agent parser in pure Clojure that handles hundlreds of known
desktops, tables, web-crawlers and TV consoles. This is a matter of months but
not hours.

The pattern we used here makes recursive Java to Clojure transformation. It
takes the top-level Java class and builds a tree of maps which's structure
follows the original branch of Java classes. To add a new class into the play,
just implement its own logic for Clojure mapping by extending `ToClojure`
protocol.

It was the first coding session in that book. Do you feed excited? I've got more
interesting things for you.


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



























































Process

Task

Problem (sh is poor)

Sample code



Conclusion
