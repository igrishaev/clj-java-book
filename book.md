


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
