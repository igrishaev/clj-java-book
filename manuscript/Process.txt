# Processes and Browser automation

Out final coding session is about managing processes. As I have mentioned before, Clojure is not designed to control system processes since these are obligations of a host system that Clojure relies on. But still, with neat Clojure data structures and syntax, we will make wrappers for that.

The task we are going to solve is to automate the browser installed on your computer. The goal is to implement functions like `goto` or `click` to force a browser to open a web-page or click on a link. They should work directly from REPL.

## What is a Webdriver

To perform these actions, we need to start a command line utility called a Webdriver. It is a general name and every browser has its own implementation. For Google Chrome it is `chromedriver`, Firefox has got its `geckodriver`, Safari browser ships `safaridriver` out from the box.

When launched, the utility finds its browser installed on your computer and establishes a low-level connection with it through a socket. At the same time, it starts a web server that handles HTTP REST requests. These requests tell the browser to perform certain actions, say open a page or click on a link. Under the hood, the Webdrver translates high-level requests into binary data that satisfy the browser's internal protocol.

The whole pipeline we are going to go through is:

- spawn a new process by launching a Webdriver utility;
- connect to the local HTTP server and init a new session;
- having that session ID, send some requests to the server;
- close the session, quit the process.

## Obtaining a Webderiver

We are going to work with Google Chrome and its `chromedriver` binary tool. I'm sure you have Chrome installed already. To achieve the driver, run in your terminal:

{lang=bash, linenos=off}
~~~
brew install chromedriver         # in MacOS
sudo apt-get install chromedriver # in Ubuntu
~~~

[chromedriver-dl]:http://chromedriver.chromium.org/downloads

If you've got Windows system or something went wrong during the installation, download a precompiled binary from the [official page][chromedriver-dl].

After the installation, try to launch the driver manually. In case you installed it from packages it should be in your `PATH` variable. So you can run it from any directory just typing `chromedriver`. If you downloaded the file manually, specify the full path which is `/Users/ivan/Downloads/chromedriver` in my example.

The following message in the terminal indicates the driver works fine:

{lang=bash, linenos=off}
Starting ChromeDriver 2.41.578706 (5f725d1b4f0a4acbf5259df887244095596231db)
on port 9515. Only local connections are allowed.
~~~

Now quit it and go back into the editor.

## Manage a process in Clojure

Add the dependencies we need:

{lang=clojure, linenos=off}
~~~
[clj-http "3.7.0"]
[cheshire "5.6.3"]
~~~

There is everything you are familiar so far. Prepare a new file named `proc.clj` with a namespace declaration:

{lang=clojure, linenos=off}
~~~
(ns project.proc
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [cheshire.core :as json])
  (:import (java.lang ProcessBuilder Process)
           java.util.Map
           java.io.File))
~~~

One note: both `ProcessBuilder` and `Process` classes belong to the `java.lang` package. They are available by default and thus there is no need to import them. But I keep their declarations to stress the fact the logic of the namespace relies on Java capabilities.

The `ProcessBuilder` class prepares a further process step by step before you launch it. At least we need to specify what we would like to start by passing a command. Sometimes, setting additional environment variables are required as well as STDOUT or STDERR redirection into files. Redirection is crucial because some tools generate plenty of data especially when `--verbose` flag is set. Those files play the same role as logs. If a process failed, you can investigate a reason by analysing its STDERR dump.

Taking all together, let's write a simple Clojure wrapper:

{lang=clojure, linenos=off}
~~~
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
~~~

Its logic is really straightforward. First, it turns a collection of args into a Java array of strings. It's important since some arguments might be integers, e.g port numbers. The function `args->command` is trivial except for one thing:

{lang=clojure, linenos=off}
~~~
(defn ^"[Ljava.lang.String;"
  args->command
  [args]
  (into-array String (map str args)))
~~~

Take a look at its type declaration. We have to specify the result is a Java array of strings because otherwise, Clojure compiler will complain it cannot resolve a proper constructor. It happens when there are more than one methods or constructors with the same arity so Clojure needs to know their types. This is a rare case when a type hint is not optional but mandatory.

Next, some imperative code follows. For example, if extra environment variables were passed in an optional `:env` map, we merge them into the builder's variables. Here are the functions that do that:

{lang=clojure, linenos=off}
~~~
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
~~~

Here, the `kw->env` is just an utility function that turns `:foo-bar` into `"FOO_BAR"` for example. The `set-env` takes a builder and a Clojure map. It fetches a Java map from a builder and injects all the values from a Clojure map into it. The builder object keeps a reference to that Java map so we just mutate it.

If any of `:path-out` or `:path-err` strings were passed, we redirect corresponding channels into files wrapping then with a `File` object.

At the end of the function, we spawn and return a process instance. This is the moment when the process starts its job. For example, if it was a GUI application, its window should appear.

The `Process` object provides a few methods for only two of them we are interested in. These are `.destroy` and `.exitValue` to stop the process and check if it has been stopped successfully. When we finish communicating with the driver's HTTP server, we will call them.

## Starting the server

Here is how we start the driver:

{lang=clojure, linenos=off}
~~~
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
~~~

We specified the port number as 9999 with additional verbosity. Also, we redirected the out channels into files that locate in the same folder where a Clojure REPL was started.

Evaluating a `_proc` variable prints something like that:

{lang=clojure, linenos=off}
~~~
#object[java.lang.UNIXProcess 0x168e8647 "java.lang.UNIXProcess@168e8647"]
~~~

It doesn't say anything about if a process is still alive or it failed. Since Java 8, the `Process` class has `.isAlive` method that returns a boolean value. But to make our code work with Java 7 too, let's write our own function:

{lang=clojure, linenos=off}
~~~
(defn alive?
  [^Process p]
  (try
    (.exitValue p)
    false
    (catch IllegalThreadStateException e
      true)))

(alive? _proc)
true
~~~

It may look a bit strange that we catch an exception but it is because of Java design. If a process hasn't stopped yet, it throws an `IllegalThreadStateException` exception when we try to get its exit status. So we catch exactly this type of an exception to return a true value.

## Play with Webdriver API

[w3-webdriver]:https://www.w3.org/TR/webdriver/#list-of-endpoints

Now that the driver is working, we are going to call some of its REST API. We won't dive into Webdriver protocol completely since it is a subject for a separate book. For those of you interested in I highly recommend looking through the official [Webdriver documentation][w3-webdriver].

So far, none of the Chrome windows has opened because we didn't initiate a session. Let's obtain it. As usual, we create a couple of functions for that:

{lang=clojure, linenos=off}
~~~
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
~~~

This function performs a POST request to the `http://127.0.0.1:9999/session` endpoint passing a JSON payload with one field:

{lang=json, linenos=off}
~~~
{"desiredCapabilities" {}}
~~~

The object value for the `desiredCapabilities` field specifies additional options for the browser. There are plenty of them, but at the moment we would like to keep everything by default. The server returns a JSON response which is parsed automatically to a map due to `:as :json` parameters. The `sessionId` field is exactly what we need. It's a string that we save in the `_sess` variable. A Webdriver handles only one session at once.

At the moment, a new blank window of Chrome browser should appear. Isn't it really a magic, is it?

Let's open the Wikipedia page. There is a function that makes another POST request to the `http://127.0.0.1:9999/session/XXXXXXXXXXXXX/url` endpoint passing a target URL in JSON payload.

{lang=clojure, linenos=off}
~~~
(defn goto-url
  [session url]
  (client/post
   (make-url "session" session "url")
   {:as :json
    :content-type :json
    :form-params {:url url}}))

(goto-url _sess "https://en.wikipedia.org/")
~~~

The blank window should load the Wikipedia content. Let's search for something. To interact with any element on a page, we need to know its ID first. Don't mix it with the `id` HTML attribute. Instead, this is a long string that identifies a DOM node in the browser's memory, for example, "0.5383067151615304-1".

{lang=clojure, linenos=off}
~~~
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
~~~

This function finds an element using XPath expression and returns the long ID. It will be a foundation for two high-level wrappers for inputting text and clicking on something:

{lang=clojure, linenos=off}
~~~
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
~~~

The following code inputs "Clojure" in a search field and clicks on a loupe button that triggers search request:

{lang=clojure, linenos=off}
~~~
(input-text _sess ".//*[@id='searchInput']" "Clojure")

(click _sess ".//*[@id='searchButton']")
~~~

Since there is only one meaning for term `Clojure` in Wikipedia, it redirects us to the `https://en.wikipedia.org/wiki/Clojure` URL rather than show a search results page.

Combining `goto-url`, `input-text` and `click` functions you may play any scenario to automate some duties or to test your site. There is plenty of other API for example to take a screenshot or to execute Javascript, but they are out of our scope.

To quit the session, send a new request:

{lang=clojure, linenos=off}
~~~
(defn delete-session
  [session]
  (client/delete
   (make-url "session" session)))

(delete-session _sess)
~~~

At this moment, the browser window should disappear.

## Getting down the process

Remember, the process we started before still hangs. If anybody tries to spawn a new chromedriver, they will get an error saying the port 9999 is already used. So we stop the process manually:

{lang=clojure, linenos=off}
~~~
(defn stop-process
  [^Process p]
  (when (alive? p)
    (.destroy p)
    (.waitFor p)
    (println (.exitValue p))))

(stop-process _p)
~~~

The `.destroy` method sends a signal to a process to stop but it might take some before it completes its duties and obeys. The process may continue to work for some time even after it has received such a signal. In other words, stopping a process is an asynchronous operation. The `.waitFor` method blocks the further execution until the process is really stopped.

Forcing a programmer to keep in mind all the resources they must release is a bit tedious. Moreover, an exception might occur when calling the API. As the result, the process will hang forever. It would be great to create a macro that spawns a process, binds it to a local variable and stops it no matter if there was an exception or not.

{lang=clojure, linenos=off}
~~~
(defmacro with-process
  [[bind & params] & body]
  `(let [~bind (proc-start ~@params)]
     (try
       ~@body
       (finally
         (stop-process ~bind)))))
~~~

The first argument of that macro is a binding vector. Its first element stands for a local variable a process instance should be bound to. The rest of the vector are parameters to the `proc-start` function. The `body` is an arbitrary Clojure code to execute. The trick is, we wrap the whole logic into a `let` form that provides an instance of a process bound to some symbol. Body is protected with `try-finally` form that shuts down the process in any case. Here is an example:

{lang=clojure, linenos=off}
~~~
(with-process
  [proc ["chromedriver" "-p" 9999] {:env {:debug 1}}]
  (let [session (init-session)]
    (goto-url session "http://exampple.com")
    ;; any other code
    (delete-session session)))
~~~

[source]: https://github.com/igrishaev/clj-java-book/blob/master/project/src/project/proc.clj

The whole source code is available [on GitHub][source].

## Conclusion

[etaoin]:https://github.com/igrishaev/etaoin

[selenium]:https://www.seleniumhq.org/

You've learnt how to manage OS processes from Clojure. What we have implemented so far is a skeleton of a [Selenium][selenium]-like software that automates browsers. Generally speaking, it works the same way: starts a driver's process and sends HTTP requests to the local server. There is also a pure Clojure [Etaoin][etaoin] library that implements the official Webdriver API. The code snippets for this session were borrowed from the Etaoin's codebase.
