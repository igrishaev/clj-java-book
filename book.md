

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
it is a matter of personal attitute. We will go on with Jsoup in that session.

Add the library into the project:

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
           org.jsoup.nodes.Entities$EscapeMode))
```

These are classes we will need to complete the task.













Sometimes, complicated business logic require some tags with the same name be
either kept or dropped depending on their attributes.









task

Phases: library; invoce; result

1. the lib

2. sample code

3. result

Now, deal with iframes YouTube

Now, deal with coub

Conclusion


Extend JDBC

Task

Json

PostGis


Streams

Intro

The task

Problem

US NPI

binary stream -> zip-stream -> csv-stream -> model stream


Process

Task

Problem (sh is poor)

Sample code



Conclusion
