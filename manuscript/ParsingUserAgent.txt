
# Parsing User-Agent header

OK, let's do some coding. The problem we are going solve is the following. Imagine we are developing a sort of analytic system that collects as much information about a user as possible. Businesses prescribe us to store data about users' browser and operation system, their version and family if they use mobile devices and what exactly (iOS, Android, etc).

[user-agent]:https://en.wikipedia.org/wiki/User_agent

Most of this information is borrowed from [User-Agent HTTP header][user-agent]. It's a string that represents almost everything we need. Each time a user opens a web-page, their browser sends this string. Here is mine, for example, copied from Google Chrome:

{lang=text, linenos=off}
~~~
Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6)
AppleWebKit/537.36 (KHTML, like Gecko)
Chrome/67.0.3396.99 Safari/537.36
~~~

The problem with User-Agent is, it is quite bad organized. For decades of chaotic web-development, browser manufacturers have been dumping more and more details there. What it ended up with, there is no a single and clear rule to parse User-Agent. Doing that manually is one of those tasks that look simple but quickly turns into a mess. The right decision would be to import a Java library developed for this purpose.

The whole process consists of three stages. At first, we find a proper library and plug it into a project. Then we add a new namespace aimed at wrapping that library. On the third step, we turn the results got from Java into habitual Clojure structures, usually a combination of maps and vectors.

[uadetector-site]: http://uadetector.sourceforge.net/

## Meet UA Detector

The library I chose for that purpose is [UA Detector][uadetector-site]. It has got hundreds of patterns for desktop computers, mobile devices and web-crawlers (Goole, Yahoo, etc). In your `project.clj` file, and the following into the `:dependencies` vector:

{lang=clojure, linenos=off}
~~~
[net.sf.uadetector/uadetector-core "0.9.10"]
[net.sf.uadetector/uadetector-resources "2014.10"]
~~~

It looks strange that we added two lines instead of one. This is because of the architecture of a library. It consists of two parts: common API and resources.

The main `uadetector-core` part provides high-level API that interacts with the underlying `uadetector-resources` part which is a database of known patterns. That layered design brings certain benefits. If a new portion of patters occurs across the Internet (thanks to browser manufacturers), there would be enough to update only `uadetector-resources` keeping the same version of `uadetector-core`.

## Setting up a namespace

Moving to the step two, let's create a separate namespace where all the Java interop will act. Here is the draft:

{lang=clojure, linenos=off}
~~~
(ns project.ua
  (:import [net.sf.uadetector.service
            UADetectorServiceFactory]))

(def ^:private parser
  (UADetectorServiceFactory/getResourceModuleParser))

(defn parse [^String user-agent]
  (.parse parser user-agent))
~~~

The main detail here is, we created a `parser` object which is an instance of `UserAgentStringParserImpl` class. Since most of the Java objects are mutable, it is better to keep that object being private so nobody can affect it from the outside of the namespace.

The `parse` wrapper function accepts a User-Agent string and calls `.parse` method on the `parser` object passing the string as the first argument. Pay attention to a type hint in the signature. When dealing with Java objects, type hints help not only Clojure to dispatch a proper method but also programmers to guess what is going on.

If we call the function with a sample:

{lang=clojure, linenos=off}
~~~
(def ua-sample
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6)
   AppleWebKit/537.36 (KHTML, like Gecko)
   Chrome/67.0.3396.99 Safari/537.36")

(def result (parse ua-sample))
~~~

the result will be an instance of `net.sf.uadetector.UserAgent` class. Its string representation looks like this (truncated):

{lang=clojure, linenos=off}
~~~
#object[net.sf.uadetector.UserAgent 0x3acfecaa "UserAgent
[deviceCategory=DeviceCategory [category=PERSONAL_COMPUTER,
icon=desktop.png, infoUrl=/list-of-ua/device-detail?device=..."]
~~~

This is definitely not a Clojure structure and thus cannot be used with core functions. Yet it confirms we managed to parse something. Here, step two is finished and we start thinking on how to make the result more Clojure-friendly.

## To Clojure recursive pattern

Let's remind how Java programmers store data. Usually, they create an instance of some top-level class that keeps references to other instances of lower ranks. For example, a `UserAgent` object manages `DeviceCategory` and `OperatingSystem` objects that store other minor data and so forth. What we've got here is a tree of objects.

To convert such an object tree to Clojure, let's write a function. It accepts the top-level object first and returns a map. Its keys are keywords and values are results of calling methods from the object. If a method returns a primitive data type, say integer or string, we keep it as a final value. If it is still a complex object, we pass it to the same function to take it apart on a map again. So a nested object becomes a nested map.

We continue the process until all the values are of primitive types. This is what I name recursive To Clojure pattern.

The function we are going to write is not an ordinary `defn` one but rather a part of a protocol. Each data type we need to convert to Clojure extends this protocol. It assures the function operates on only certain types we need but not any other value. Here is the protocol:

{lang=clojure, linenos=off}
~~~
(defprotocol ToClojure
  (->clj [x]))
~~~

Now import the classes we need to extend. In your namespace declaration, extend the `(:import ...)` statement as follows:

{lang=clojure, linenos=off}
~~~
(ns project.ua
  (:import [net.sf.uadetector.service
            UADetectorServiceFactory]

           [net.sf.uadetector
            UserAgent
            VersionNumber
            OperatingSystem
            DeviceCategory]))
~~~

[javadocs-ua]: http://uadetector.sourceforge.net/modules/uadetector-core/apidocs/net/sf/uadetector/UserAgent.html

Now extend the top-level `UserAgent` class. To know more about its anatomy, take a look at the [Javadocs page][javadocs-ua]. Briefly saying, we are interested in all the getters from that class.

{lang=clojure, linenos=off}
~~~
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
~~~

If we pass the `result` variable into the `->clj` function, we will get a map with a structure shown above. This is great, but most of the values of that map are still complex Java classes. We need to update them too.

## Processing nested objects

Let's start with the `:device` field. Rather than keeping it as a Java object, we wrap it into `->clj` and then extend the `DeviceCategory` class so it returns a map. Fix the previous `:device` value:

{lang=clojure, linenos=off}
~~~
  UserAgent
  (->clj [ua]
    {:device       (->clj (.getDeviceCategory ua))
~~~

Extend `DeviceCategory` as follows:

{lang=clojure, linenos=off}
~~~
(extend-protocol ToClojure

  DeviceCategory
  (->clj [dev]
    {:category (->clj (.getCategory dev))
     :name (.getName dev)})

  java.lang.Enum
  (->clj [e]
    (-> e .name keyword)))
~~~

The `(.getCategory dev)` method returns a enum Java value represented by `net.sf.uadetector.ReadableDeviceCategory.Category` class. There is no need to extend that class exactly because functions that belong to protocols take inheritance into account. Extending just `java.lang.Enum` class which is an ancestor for `ReadableDeviceCategory.Category` would be enough to make sure all the enum values are converted the same.

The whole branch under the `:device` field consists only from nested Clojure maps or primitive values and thus is processed completely.

So you've got the idea: for every Java class, we describe the way it reflects the Clojure world. If two classes are similar or share one ancestor, there might be enough to extend just the ancestor.

Let's finish with the rest of the task. Taking apart an operating system would be:

{lang=clojure, linenos=off}
~~~
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
~~~

Another statement for versioning class:

{lang=clojure, linenos=off}
~~~
(extend-protocol ToClojure

  VersionNumber
  (->clj [ver]
    {:bug-fix   (.getBugfix ver)
     :extension (.getExtension ver)
     :groups    (.getGroups ver)
     :major     (.getMajor ver)
     :minor     (.getMinor ver)
     :version   (.toVersionString ver)}))
~~~

## The final view

The last touches would be to wrap the result of `parse` function with `->clj` so it starts recursive transformation and returns Clojure data instead of `UserAgent` object. The second thing is to clean the code a bit, for example, to join our `extend-protocol` statements into a single one. Here is the final version:

{lang=clojure, linenos=off}
~~~
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
~~~

A quick test if we really receive Clojure data:

{lang=clojure, linenos=off}
~~~
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
~~~

[source]: https://github.com/igrishaev/clj-java-book/blob/master/project/src/project/ua.clj

We've ended up with a single function and extending just four Java classes. Such a tree might easily be printed, sent through a network as a JSON string or stored in a database that supports JSON values, for example, MongoDB or PostgreSQL. Check the source code [on GitHub][source].

## Conclusion

So we've managed to reuse Java code for our business requirements. I believe it's obvious that wrapping Java classes is easier than writing everything from scratch. If you doubt on that, take a weekend and try to write your own User-Agent parser in pure Clojure that handles desktops, tables, and TV consoles. This is a matter of months but not hours.

The pattern we used here transforms Java to Clojure recursively. It takes the top-level Java object and builds a tree of maps which follows the original structure of Java objects. To add a new class into the play, just implement its own logic extending `ToClojure` protocol.

I congratulate that you've passed the first coding session in that book. Do you feel excited? The upcoming tasks are waiting.
