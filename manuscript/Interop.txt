# Java interoperability basics

This chapter explains basic Clojure capabilities to interoperate with Java. We will use them in further practical chapters a lot. I'm going to highlight how to operate on Java objects as well as share some good practices on the subject.

## Importing classes

To use a class, you've got to import it first. There are two ways to import a class: in a namespace declaration which is desirable and in runtime using `import` function. For example:

{lang=clojure, linenos=off}
~~~
(ns com.project.module
  (:import java.util.File))
~~~

or

{lang=clojure, linenos=off}
~~~
(import 'java.util.File)
~~~

In the first example, we specify a class without putting a leading quote. This is because the whole `ns` statement is a macro so everything inside it is quoted. Instead, when calling the `import` function in runtime we pass a symbol which's name stands for a class we need.

Importing classes on the fly might be confusing and not obvious. Thus, it's better to keep all the imports and requirements at the top of a file in `ns` declaration.

To be imported, a class should present in your classpath. A classpath is a special parameter of Java virtual machine. It is a list of locations separated with colons where the machine searches classes for. Usually, you don't worry about configuring classpath manually because modern development tools like `lein` or `deps.edn` take care of that by their own.

The tools mentioned above scan through a list of dependencies declared in a config file. They download required artefacts into a special folder on your machine and start JVM passing with a classpath extended with everything you specified.

Classes that belong to the `java.lang` package are not required to being imported. They are available by default, so `java.lang.String` might be reduced to just `String`.

## How to initiate an object

Creating an instance of a class works in two ways: `new` or trailing dot macros. The `new` form takes a class followed by its arguments. It looks like the standard `new` operator in Java. The trailing dot macro requires putting a dot at the end of a class name skipping `new` at the begging. As the result, it's a bit shorter than the first `new` form.

Let's demonstrate everything told so far with examples. We will use the standard classes distributed with Java SDK.

Importing a single class:

{lang=clojure, linenos=off}
~~~
(ns project.into
  (:import java.io.File))
~~~

Importing several classes from the same package at once:

{lang=clojure, linenos=off}
~~~
(ns project.into
  (:import java.io.File
           (java.util Date UUID)))
~~~

Pay attention at extra parens around the `java.util` path. It's mandatory to put them here. They help Clojure reader to not get confused when parsing imports.

Let's initiate some of the classes we've imported:

{lang=clojure, linenos=off}
~~~
(def file (File. "/Users/ivan/.emacs"))

;; evaluating the `file` variable in repl prints
#object[java.io.File 0xbfef89d "/Users/ivan/.emacs"]

(def date (new Date))

;; evaluating `date` returns
#inst "2018-08-25T08:20:40.412-00:00"
~~~

## Static methods

Some Java classes provide static methods. Calling such a method doesn't require to create an instance of a class. Access them via slash as follows:

{lang=clojure, linenos=off}
~~~
(def uuid (UUID/randomUUID))

#uuid "fb5876a6-b3c6-47dc-89d6-10dafcaf0888"
~~~

[java-system]:https://docs.oracle.com/javase/9/docs/api/java/lang/System.html

The standard [System class][java-system] carries plenty of static methods useful for general purposes. This class belongs to `java.lang` package and thus is available from everywhere without importing it.

To stop the program completely call its `exit` method passing an exit code. Running it in a REPL session will terminate it:

{lang=clojure, linenos=off}
~~~
(System/exit 0)
~~~

The `getenv` static methods either return either a single environ variable or the whole map of them depending on arity (the number of passed parameters):

{lang=clojure, linenos=off}
~~~
(System/getenv "HOME")
"/Users/ivan"

(into {} (System/getenv))
{"LEIN_VERSION" "2.6.1"
 "HOME"         "/Users/ivan"
 "USER"         "ivan"
 "LEIN_HOME"    "/Users/ivan/.lein"
 ;; truncated
 }
~~~

In the second case, we convent a Java native map into its Clojure counterpart to make the output look better.

A simple wrapper to get the current number of seconds since 1 Jan 1970 which is also known as Unix timestamp or epoch:

{lang=clojure, linenos=off}
~~~
(defn epoch
  []
  (quot (System/currentTimeMillis) 1000))

(epoch)
1535186375
~~~

## Object methods

Having an object, usually, you are interested in calling its methods. To access a non-static method, put its name with leading dot at the first place of a lisp form followed by an object and the rest arguments.

Here is how you may know a file's absolute path:

{lang=clojure, linenos=off}
~~~
(def file (File. "book.txt"))

(.getAbsolutePath file)
"/Users/ivan/drafts/project/book.txt"
~~~

To check if it really exists:

{lang=clojure, linenos=off}
~~~
(.exists file)
true
~~~

Or to rename (move) it:

{lang=clojure, linenos=off}
~~~
(.renameTo file (File. "/Users/ivan/ready/project/book-ready.txt"))
true
~~~

## Nested classes

Sometimes, a class declares a nested class. To access it, put a dollar sign between their names. For example, if a class `Foo` carries a nested class `Bar`, a syntax to reach it will be:

{lang=clojure, linenos=off}
~~~
(ns project.into
  (:import com.project.Foo$Bar))

(def bar (Foo$Bar. param1 param2))
~~~

## Universal Java access

There is a special Dot form that acts like a universal access to Java properties. It keeps the same syntax even for different cases.

To read a static field:

{lang=clojure, linenos=off}
~~~
(. File pathSeparator)
":"
~~~

To call a static method:

{lang=clojure, linenos=off}
~~~
(. File createTempFile "temp" ".txt")
#object[java.io.File 0x4c2544bc "/var/folders/94/...6500566792.txt"]
~~~

To call a method of an instance:

{lang=clojure, linenos=off}
~~~
(. file getAbsolutePath)
"/Users/ivan/drafts/project/book.txt"
~~~

Or to read a field of an instance. Pay attention at the leading hyphen:

{lang=clojure, linenos=off}
~~~
(. obj -value)
;; the same as `obj.value` in pure Java
~~~

There is also a `set!` form that works in pair with the Dot macro. Use it to write a new value to a field:

{lang=clojure, linenos=off}
~~~
(set! (. obj -value) 42)
;; the same as `obj.value = 42` in pure Java
~~~

The last two cases with reading and writing a field are not common due to Java design. Exposing fields to the outer world is considered as bad practice. Instead, most Java programmers provide special methods to regulate how a certain field is being read or set. Thus, you call for `(.getValue obj)` or `(.setValue obj 42)` more often than accessing raw fields.

The Double Dot macros acts similar to its Single Dot brother. It chains results between multiple expressions so the next form takes a value produced by a previous form. It is similar to the threading `->` macro that probably you are familiar with.

{lang=clojure, linenos=off}
~~~
(.. file toPath getFileSystem getClass getName)
"sun.nio.fs.MacOSXFileSystem"
~~~

Under the hood, it turns into a nested extression like:

{lang=clojure, linenos=off}
~~~
(. (. (. (. (. file toPath) toPath) getFileSystem) getClass) getName)
~~~

which is difficult to read since your eyes constantly jump to and fro.

Each method in a chain is called from an object received from a previous unit. If any extra arguments are required, the method expression is put into parens:

{lang=clojure, linenos=off}
~~~
(.. obj (some-method "foo") (other-method "bar" 42))
~~~

## Reflection

To know what class an object belongs to, call `class` function:

{lang=clojure, linenos=off}
~~~
(class file)
java.io.File
~~~

The `type` function acts similar but it checks for a variable's metadata first. Let's discuss how it works. If you define a variable with a type hint as shown below:

{lang=clojure, linenos=off}
~~~
(def ^File file (File. "some/path.txt"))
~~~

the metadata of `#'file` variable will contain a `:tag` field with `java.io.File` value:

{lang=clojure, linenos=off}
~~~
(meta #'file)

{:tag java.io.File,
 :line 1778,
 :column 15,
 :file "*cider-repl*",
 :name file,
 :ns #namespace[project.into]}
~~~

Probably you would like to divide logic into different branches depending on the variable's type. For example, a `file` parameter in a function might be of both `String` and `File` types. The function `instance?` checks if an object is an instance of a certain class. So instead of comparing types:

{lang=clojure, linenos=off}
~~~
(case (class source)

  java.io.File
  ;; do this

  String
  ;; do that
)
~~~

you declare predicates:

{lang=clojure, linenos=off}
~~~
(def file? (partial instance? java.io.File))

(cond
  (file? source)
  ;; do this

  (string? source)
  ;; do that
  )
~~~

which is more readable and neat.

## Rest Java arguments

Some Java methods accept an arbitrary number of arguments. They are marked with ellipsis in signatures and represent an array of objects when accessing them. A good example is a `format` method of a `String` class:

{lang=java, linenos=off}
~~~
static String    format(String format, Object... args)
~~~

In Clojure, calling such a method in this way won't work:

{lang=clojure, linenos=off}
~~~
(String/format "%s %s %s" "foo" "bar" "baz")
~~~

An exception will raise saying `No matching method: format`.

This is because, in Clojure terms, the `args` parameters should be passed as a single array. It must be exactly a native Java array but not a Clojure sequence. To make your life a bit easier, there is already a `make-array` function that turns a Clojure collection into a Java typed array:

{lang=clojure, linenos=off}
~~~
(String/format "%s %s %s" (into-array ["foo" "bar" "baz"]))
"foo bar baz"
~~~

By default, the function builds an array of `Objects` that satisfies the method's signature in our case. When you need an array of some certain type, you pass a class as the first parameter to that function:

{lang=clojure, linenos=off}
~~~
(String/format "%s %s %s" (into-array String ["foo" "bar" "baz"]))
~~~

## Type hints

Yet Clojure is a dynamic language its runtime relies on types a lot. When it knows what type an object belongs to, it doesn't spend extra time on reflection and thus performs faster. You can help the compiler to guess types by adding hints. A type hint takes its place in front of a variable in function signatures or `let` bindings.

Here is an example of a function that stops a process which is an instance of `java.lang.Process` class:

{lang=clojure, linenos=off}
~~~
(defn stop-process
  [^Process p]
  (when (.isAlive p)
    (.destroy p)))
~~~

If you remove the `^Process` clause in the signature, the function will still work. But without a hint, Clojure spends extra time to figure out what the kind of an object is there. That's normal but reduces performance. To highlight all such problem places, set a special global variable to true:

{lang=clojure, linenos=off}
~~~
(set! *warn-on-reflection* true)
~~~

If you compile any code that suffers from missing hints, you will see warnings in the REPL:

{lang=text, linenos=off}
~~~
Reflection warning, /src/project/proc.clj:136:5 - reference to field destroy can't be resolved.
~~~

Type hints should definitely be put in bottleneck functions called often. It is a good practice to scan the codebase for missing hints from time to time. Yet there is no need to put tags everywhere you physically can. The compiler is smart enough to guess a further type if it has enough data.

A type hint may be any class imported before. Sometimes, some complex Java signatures are required to specify an array of a certain type for example. So a hint takes a form of a string:

{lang=clojure, linenos=off}
~~~
(defn ^"[Ljava.lang.String;"
  args->command
  [args]
  (into-array String (map str args)))
~~~

## Best practices

One more benefit tags bring to a project is they are useful to get into the codebase quickly. When you operate on ordinary Java primitives and collections, a type of a variable might be guessed by its name with ease. For example, `opt` and `params` are usually maps, `items` and `users` are sequences, `url` and `path` are strings.

That's fine until you pass an object belongs to a third-party library, e.g. a parsed HTML document which is an instance of `org.jsoup.nodes.Document` class. A lonely name `doc` won't say anything to a programmer who is maintaining the code. Is it a map, a vector or something else? Instead, the `org.jsoup.nodes.Document doc` declaration clearly expresses the very nature of the parameter so it saves time.

Try to isolate calls to Java classes in a separate namespace or even a library. Expose only Clojure high-level functions that conceal the inner Java-based logic. Let those functions accept and return native Clojure structures like maps and vectors. Java interop is considered as a low-level feature so it is always a subject to alter. If anything changes in a namespace's guts, none if other modules will fail.

Clojure code that relies on Java a lot grows in size because of `longJavaNamingRules`. As the result, it looks clumsy and might be difficult to read. Keeping such a code into a separate namespace reduces noise in the business logic.

## Conclusion

So far, we have passed through the most part of Clojure capabilities for Java interop. We didn't touch some topics like proxying and reifying interfaces or something else. But this book is not aimed at just enumerating features. Instead, everything in this book is about practice. I cannot guarantee we will cover all the possible Java features, but everything you will learn will be done for sure. I persuade you to start coding now. If any unmentioned feature appears, we will deal with it on the fly.
