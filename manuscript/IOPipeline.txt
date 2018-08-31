# IO Pipeline

In that chapter, we will work a lot with IO operations. I'm going to consider a case when you need to process large amounts of data flow through the network.

As we discussed before, Clojure is a guest system that doesn't try to implement low-level capabilities of its host. Streaming data from remote resources is one of such things that Clojure cannot perform by design. However, Java provides rich capabilities for any imaginable IO operation. So to perform accurate IO in Clojure we've got to borrow some Java stuff.

## The task briefing

Imagine a zipped CSV file that holds 7 million records somewhere on a remote server. You need to download and process all the data from it. Probably, your first steps would be to download an archive, unpack it, and then process a file. That's alright for a quick and draft solution when you are only interested in how to make it work.

In general, this step-by-step algorithm suffers from long blocking operations. Downloading a 600 Mb file will probably take a couple of minutes. Extracting 7 Gb file lasts for about the same. When a program starts, it should check if a file was already downloaded to prevent doing it again. It also should clean all the traces left on a disk afterwards.

Another pitfall would be to rely a lot on shell utilities like `wget`, `unzip`, `sed` or whatever. Although most of them are really a piece of art, it is difficult to embed them into Clojure and build the pipeline. In fact, even minor usage of shell utilities turns the whole code into a bash script.

The behaviour of shell tools varies on their version and OS family. At the moment of writing this, the `unzip` command on my laptop cannot handle the subject file which is larger than 2 Gb due to some known bug. You just cannot foresee such a case when sharing the codebase across Mac and dozens of Linux distributions. Instead, the JVM code works as expected on all machines.

What I propose is to process the data on the fly using Java IO capabilities. Since CSV is a plain text, there is no need to wait until the whole file is downloaded and unpacked.

## Technical proposal

First, we send an HTTP request for that file specifying we would like to process its body, not as a string but a binary stream. In Java, a stream is an abstraction that produces data on demand from some source without flooding the whole memory. In our case, such a stream represents a binary zip file.

To read zip's content, we wrap the stream with a special `ZipArchiveInputStream` class that knows how to treat its binary content. The archived stream doesn't read the whole input but only a few leading bytes to know what's inside it. Having that table of content, we may seek for a file we need by its name and get its content as a stream again.

To parse CVS data into rows, we pass that stream into a CSV reader that consumes it and fetches native Clojure data structures. Then we wrap them with a function that cleans unnecessarily fields and coerces values to proper types. Finally, those clear final maps will be saved to the database by partitions of 1000.

What I would like to highlight is the whole pipeline starts working immediately. A sequence of data that we have in the end is lazy and works on demand. For example, you may take just ten leading records without downloading the whole file. Another benefit is you may show the progress printing how may records have been fetched so far. Finally, the data doesn't touch the disk so you are not bound to free space limitations. If a file grew ten times in size, you could still process it.

## Preparing a draft

Add dependencies we need:

{lang=clojure, linenos=off}
~~~
[org.apache.commons/commons-compress "1.5"]
[clj-http "3.7.0"]
[org.clojure/data.csv "0.1.4"]
[org.jsoup/jsoup "1.11.3"]
[org.clojure/java.jdbc "0.6.1"]
[org.postgresql/postgresql "42.1.3"]
~~~

That's a bit more than we used before. Let's go through the list quickly:

- `commons-compress` is Java library to handle zipped data. Yet Java SDK ships similar functionality out from the box, the external Apache library handles some issues with encoding and thus is more reliable;

- `clj-http` is a great Clojure wrapper around Apache HTTP client to send HTTP requests;

- `data.csv` is a simple but useful library to read and write CSV data;

- `jsoup` is a tool you met before to clean HTML data. In that chapter, you will find another way to use it;

- `java.jdbc` and `postgresql` you are familiar with is to write the results into the database.

Prepare a new module with all the stuff imported:

{lang=clojure, linenos=off}
~~~
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
~~~

[npi-files]:http://download.cms.gov/nppes/NPI_Files.html

The file we are going to process is known as "NPI Registry" and represents data about American practitioners and healthcare organizations. A link to that file is updated monthly and might be found on the official [NPPES site][npi-files].

The first problem we are going to solve is to deal with a URL pointing to that file. It includes the current month name, e.g. August or September. Generally, it is not a problem to substitute it but sometimes the file is updated with a delay so it is still "August" whereas September has come. So we parse an HTML page to find a target link using Jsoup.

{lang=clojure, linenos=off}
~~~
(def files-page
  "http://download.cms.gov/nppes/NPI_Files.html")

(defn find-url
  []
  (let [doc (.get (Jsoup/connect files-page))
        selector "a[href~=NPPES_Data_Dissemination_\\w+_\\d{4}\\.zip]"
        links (.select doc selector)]
    (some-> links
            first
            (.absUrl "href"))))
~~~

This function returns a full URL we need as a string. At the moment of writing, it was `http://download.cms.gov/nppes/NPPES_Data_Dissemination_August_2018.zip`.

## Building IO pipeline

To get the file's binary stream, we send an HTTP request passing special options:

{lang=clojure, linenos=off}
~~~
(defn get-file-stream
  [url]
  (:body (client/get url {:as :stream})))
~~~

The result will be a special Java object represents a stream:

{lang=clojure, linenos=off}
~~~
#object[clj_http.core.proxy$java.io.FilterInputStream$ff19274a 0x4b3d87e3
  "clj_http.core.proxy$java.io.FilterInputStream$ff19274a@4b3d87e3"]
~~~

Reading manually it has no sense because it emits binary data. We convert it to a zipped stream which knows how to treat zipped data:

{lang=clojure, linenos=off}
~~~
(defn ->zip-stream
  [stream]
  (new ZipArchiveInputStream stream))
~~~

Now we try to find a file we need in that stream. Remember, a zipped stream reads the table of all the files were zipped. In addition to their names and metadata, the table tracks their internal locations so you can jump to a certain file skipping all the rest.

{lang=clojure, linenos=off}
~~~
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
~~~

The function takes a zipped stream and a regex pattern. It iterates through zip entries checking if their name matches the pattern. A zip entry is some sort of a metadata about a file. Switching to the next entry also shifts an internal pointer of a stream indicates where to start reading from.

Iteration stops once we found an entry with an appropriate name. The result will be either a Clojure map with basic entry info or `nil` value meaning we didn't manage to find such an entry.

A file in archive that we are interested in is called `npidata_pfile_20050523-20180812.csv` so the regex would be:

{lang=clojure, linenos=off}
~~~
(def re-csv #"(?i)_\d{8}-\d{8}\.csv$")
~~~

Right after the stream has been pointed to a proper file, let's process its data with a CSV reader:

{lang=clojure, linenos=off}
~~~
(defn read-csv
  [stream]
  (let [reader (io/reader stream)
        rows (csv/read-csv reader)
        header (map clean-header-field (first rows))]
    (for [row (rest rows)]
      (zipmap header (map clean-row-field row)))))
~~~

First, the function reads the first header line and turns column names into keywords passing them through the `clean-header-field` function. We'll omit its declaration since it just operates on strings, wipes unnecessary symbols, changes the registry and so on. As the result, the `Entity Type Code` caption becomes `:entity_type_code` for example. Another `clean-row-field` function substitutes dummy values like empty strings or `"<UNAVAIL>"` to nils.

The final function takes a cleaned map and builds a business model. In our example, we just take a certain subset of that map but in a real case, we would do something more complicated: coerce some values to integers or booleans, unify codes, etc. We map a sequence of CSV rows on that function to get a sequence of business models.

{lang=clojure, linenos=off}
~~~
(defn ->model
  [row]
  (select-keys
   row [:npi
        :entity_type_code
        :provider_first_name
        :provider_credential_text
        ;; other fields...
        ]))
~~~

## Save the results

It's time to save our results to the database. Inserting models one-by-one requires 7 million inserts that is not good. On the other side, a single insert obligates us to collect the whole dataset in memory what is exactly we are trying to avoid. The golden middleware would be to insert data by chunks, say dump every 1000 models into the database.

Here is a minor function that takes a lazy sequence and returns a lazy sequence of chunks:

{lang=clojure, linenos=off}
~~~
(defn by-chunks
  [coll n]
  (partition n n [] coll))

;; usage:
(by-chunks [1 2 3 4 5] 3)

((1 2 3) (4 5))
~~~

To insert multiple records in the database at once, implement a shortcut for JDBC:

{lang=clojure, linenos=off}
~~~
(def db
  {:dbtype "postgresql"
   :dbname "clj-db"
   :host "127.0.0.1"
   :user "clj-user"
   :password "clj-pass"})

(def insert-multi! (partial jdbc/insert-multi! db))
~~~

Ok, with everything has been implemented so far we compose a final combo. Here is how we get a sequence of business models:

{lang=clojure, linenos=off}
~~~
(defn get-models
  []
  (let [file-url (find-url)
        stream-bin (get-file-stream file-url)
        ztream-zip (->zip-stream stream-bin)
        entry (seek-stream ztream-zip re-csv)]

    (assert entry (format "file %s not found" re-csv))

    (let [rows (read-csv ztream-zip)]
      (map ->model rows))))
~~~

Pay attention we put `assert` to ensure we managed to find a file. Passing `nil` value next will cause null pointer exceptions. Another highlight is, we operate on short functions rather than wrapping the whole logic into a single one. Small functions are great material to compose the logic you need. Keeping things apart maintains simplicity which is crucial.

Let's fetch some leading models from the server:

{lang=clojure, linenos=off}
~~~
(take 3 (get-models))

;; the output is truncated
({:npi "1679576722"} {:npi "1588667638"} {:npi "1497758544"})
~~~

That really works and the data arrives almost immediately. Imagine you have to wait for five minutes before accessing the data. That would be unbearable.

Now save the models into the database by chunks.

{lang=clojure, linenos=off}
~~~
(defn save-models
  [models]
  (doseq [chunk (by-chunks models 1000)]
    ;; log or print here to see the progress
    (insert-multi! :models chunk)))
~~~

Putting everything together. It takes about 40 minutes on my laptop:

{lang=clojure, linenos=off}
~~~
(save-models (get-models))
~~~

Of cause, a table `models` with a proper structure should be created in advance in the database.

[source]: https://github.com/igrishaev/clj-java-book/blob/master/project/src/project/io.clj

Examine the final version of the code on [on GitHub][source].

## Conclusion

Alright, it was a tough route but we've managed to succeed. Thanks to Java IO capabilities that helped us to build the pipeline. We could improve the final solution by handling exceptions on each step and logging them. I believe it's wrong to interrupt the whole process just because one record is corrupted. Another feature would be to load the data in parallel using futures or `pmap` for a performance boost. For the rest, the subject issue is complete so we move to the next class.
