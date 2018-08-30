


# Java interop basics


todo warn!!


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
