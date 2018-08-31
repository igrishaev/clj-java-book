# Boosting up your JDBC driver

In this class, we will turn to the database facilities. Most likely your application works with some sort of a database. There are plenty of DB types nowadays. For example, proven relational MySQL or Postgres that have been developed for decades; modern non-relational Mongo or CouchDB; datalog-based Datomic that brings Clojure-specific features to the scene.

Having flexible abstractions above a low-level DB driver is crucial. The move features your database module provides, the less code you have to write in the business logic. For example, if a DB engine supports JSON data natively, you shouldn't serialise data into a string by hands. There should be a middleware though. Don't fall into constructing your own ORM since the complexity will beat you while the business features don't progress.

In this chapter, we'll examine how to add more features to the standard JDBC driver in conjunction with PostgreSQL backend. Both technologies have passed a long way of development. JDBC has become a standard in Java world. It acts as a foundation for every DB-related solution. PostgreSQL is a powerful database with dozens of features and flexible data types. It is a free and open-source product.

[clj-jdbc]:https://github.com/clojure/java.jdbc

JDBC and its [Clojure wrapper][clj-jdbc] provide general APIs that let you work with any DB backend using the same approach. At first glance, the API looks a bit clumsy, but the reason for that is JDBC supports a huge variety of backends and their versions plus legacy.

## Setting up the database

Let's get started with the database module in the project. I believe you have PostgreSQL installed on your local machine. If you don't, get it with your package manager with:

{lang=bash, linenos=off}
sudo apt-get install postgresql
~~~

or

{lang=bash, linenos=off}
~~~
brew install postgresql
~~~

[pg-download]:https://www.postgresql.org/download/

depending on if you use Ubuntu or Mac. In case you've got Windows desktop or something went as not expected, visit the [official Download page][pg-download] for the latest info.

Add the JDBC wrapper and the driver Java library into dependencies:

{lang=clojure, linenos=off}
~~~
[org.clojure/java.jdbc "0.6.1"]
[org.postgresql/postgresql "42.1.3"]
~~~

These two lines highlight the very architecture of database access. JDBC wrapper (`java.jdbc`) provides a top-level API to query the database. The driver (`postgresql`) knows how to perform them against a specific backend. For MySQL, you'll need to replace `postgresql` driver dependency with:

{lang=clojure, linenos=off}
~~~
[org.clojure/java.jdbc "0.6.1"]
[mysql/mysql-connector-java "8.0.12"]
~~~

Let's quickly prepare a new database for our experiments. Switch to the `postgres` user first:

{lang=bash, linenos=off}
~~~
su - postgres
~~~

The `createuser` tool is distributed with PostgreSQL installation. Use it to create a new database user. The `-S` flag says it's a superuser with the ultimate set of capabilities. The command will prompt you for a password:

{lang=bash, linenos=off}
~~~
createuser -S -W clj-user
~~~

Another `createdb` tool also comes within PostgreSQL setup. It creates a new database which's owner is the user we've just created:

{lang=bash, linenos=off}
~~~
createdb -O clj-user clj-db
~~~

Now that we have a user within a database, login into PostgreSQL console and create an empty table:

{lang=bash, linenos=off}
~~~
psql clj-db clj-user
~~~

{lang=sql, linenos=off}
~~~
create table test (id serial primary key);
~~~

So far, all the preparations are done. Switch to your text editor to go on with Clojure part.

## Testing DB from Clojure

Create a separate Clojure module to keep all the database features in one place:

{lang=clojure, linenos=off}
~~~
(ns project.db
  (:require
   [clojure.java.jdbc :as jdbc])
~~~

Declare a variable that is known as a *database spec*. It's a map holds DB credentials and probably additional options:

{lang=clojure, linenos=off}
~~~
(def db
  {:dbtype "postgresql"
   :dbname "clj-db"
   :host "127.0.0.1"
   :user "clj-user"
   :password "clj-pass"})
~~~

Every JDBC call requires you to pass that spec as the first argument. To make the code a bit shorter, add some shortcuts using `partial` application:

{lang=clojure, linenos=off}
~~~
(def query (partial jdbc/query db))

(def insert! (partial jdbc/insert! db))

(def execute! (partial jdbc/execute! db))
~~~

A quick check:

{lang=clojure, linenos=off}
~~~
(query "select 42 as the_answer")

({:the_answer 42})
~~~

The result confirms the database is ready for further experiments.

## Basic Java types

Generally speaking, the problem we are trying to solve is to establish a mapping between database types and Clojure collections. The Clojure's JDBC wrapper brings several protocols to let the database know how to treat certain objects. If we extend them wisely, we will build a seamless connection between Clojure data and it's binary representation in the DB and vice versa.

Let's start with something simple. By default, JDBC doesn't know how to process instances of `java.net.URL` or `java.util.UUID` classes. Every time you'd like to save such an object into the database you need to coerce it to a string. This is fine but a bit annoying.

To let the database know to treat those classes, extend the `jdbc/ISQLValue` protocol as follows:

{lang=clojure, linenos=off}
~~~
(extend-protocol jdbc/ISQLValue

  java.net.URL
  (sql-value [url]
    (str url))

  java.util.UUID
  (sql-value [uuid]
    (str uuid)))
~~~

To check if the changes have been accepted, add new fields to our table and insert something:

{lang=clojure, linenos=off}
~~~
(execute! "alter table test add column url text")
(execute! "alter table test add column uuid text")

(insert! :test {:url (java.net.URL. "http://example.com")
                :uuid (java.util.UUID/randomUUID)})

(query "select * from test")

({:id 1,
  :url "http://example.com",
  :uuid "3e54df1f-b961-4303-a512-5485044a3576"})
~~~

Both types have been transformed successfully and put their place in text columns. So far, it was simply because everything we've done was coercing objects to a string.

The things start getting tougher when non-string based types come into play. Take dates, for example. Each database backend stores them in their own way so turning a date to a string won't work.

By default, JDBC returns dates from queries without troubles:

{lang=clojure, linenos=off}
~~~
(execute! "alter table test add column created_at timestamp default now()")

(query "select * from test")

({:id 1,
  :url "http://example.com",
  :uuid "3e54df1f-b961-4303-a512-5485044a3576",
  :created_at #inst "2018-08-20T14:30:31.748138000-00:00"})
~~~

But passing a date as a parameter

{lang=clojure, linenos=off}
~~~
(insert! :test {:created_at #inst "2017-01-01"})
~~~

causes an error:

{lang=text, linenos=off}
~~~
PSQLException Can't infer the SQL type to use for an instance of
java.util.Date. Use setObject() with an explicit Types value
to specify the type to use.
~~~

This looks a bit inconsistent but let's fix it. JDBC awaits for a special SQL-flavored class `java.sql.Timestamp`. Turning one type to another is done by converting the source date into milliseconds and restoring the timestamp from them:

{lang=clojure, linenos=off}
~~~
(extend-protocol jdbc/ISQLValue

  java.util.Date
  (sql-value [val]
    (java.sql.Timestamp. (.getTime val))))
~~~

Now the native Date class should work:

{lang=clojure, linenos=off}
~~~
(insert! :test {:created_at #inst "2017-01-01"})

({:id 3,
  :url nil,
  :uuid nil,
  :created_at #inst "2017-01-01T00:00:00.000000000-00:00"})
~~~

## Dealing with enumeration types

Another example of inconsistency with types in JDBC is enum columns. In Postgres, an enum is a special type that has only certain values. Let's create a simple enum type bound to a new column:

{lang=clojure, linenos=off}
~~~
(execute! "create type type_color as enum ('red', 'green', 'blue')")
(execute! "alter table test add column color type_color")
(execute! "update test set color = 'red'")

(query "select id, color from test")

({:id 1, :color "red"}
 {:id 2, :color "red"}
 {:id 3, :color "red"})
~~~

That works fine, all the colour values are strings as expected. But trying to insert a new row...

{lang=clojure, linenos=off}
~~~
(insert! :test {:color "blue"})
~~~

...causes an error:

{lang=text, linenos=off}
~~~
PSQLException ERROR: column "color" is of type type_color
but expression is of type character varying
~~~

This is because JDBC treats `type_color` as something that really differs from a string. To send an enum value into the database, we need to wrap it with a special `PGObject` class.

`PGObject` is a low-level object represents Postgres-specific value. It has just two meaningful fields: a type and its value, both strings. At the beginning of our module, add a new import line:

{lang=clojure, linenos=off}
~~~
(:import org.postgresql.util.PGobject)
~~~

and create a bit of wrapper:

{lang=clojure, linenos=off}
~~~
(defn ->pgobject
  [type value]
  (doto (PGobject.)
    (.setType type)
    (.setValue value)))

(def ->color (partial ->pgobject "type_color"))

(def enum-R (->color "red"))
(def enum-G (->color "green"))
(def enum-B (->color "blue"))
~~~

Now you may pass these `enum-X` values into queries:

{lang=clojure, linenos=off}
~~~
(insert! :test {:color enum-B})

({:id 4,
  ;; skipped
  :color "blue"})
~~~

Blue, as expected.

## Meet database json(b) types

Since you are familiar with `PGObject`, let's bring one of the main Postgres features to Clojure. I'm talking about `json(b)` type. In the latest Postgres versions, you can store JSON data not as a plain string but as a structured type. There are dozens of functions and operators to query its subfields, merge two objects into one, etc. The more advanced `jsonb` type stores its body as a binary structure so most of the operations perform even faster.

Personally, I'm not a big fan of storing everything in JSON. A strict schema is the main benefit of Postgres so using JSON a lot turns your database into MongoDB which is fuzzy. You may end up with a situation when one-half of your data has a certain field but the rest doesn't. Scanning the whole database with a script is annoying and fragile.

On the other hand, in some cases, you can succeed by dumping some minor data into a `json` column. A good example is working with Instant PayPal Notifications or IPN. When a user does something, PayPal triggers a handler on your server. It sends plenty of data in such a notification. There might be up to 30 fields to specify all the user info, their local and business addresses, product fields, tax info and so forth.

Depending on a kind of an event, a set of fields may vary. Maintaining a SQL table with 30+ columns would be a mess. The better approach would be to take only the most important fields from that set (e.g username, product ID, the total sum) and save them into dedicated columns. But to prevent the rest of the data from being lost, just dump it into a `json` column. If it turns out you need more info from that data, you will get it with a couple of SQL lines.

The following example highlights how you can detach a nested JSON field into a separate column:

{lang=sql, linenos=off}
~~~
alter table ipns add column user_email text;
udpate ipns set user_email = data#>>'{user,email}';
~~~

## Connecting json(b) with Clojure

The great idea would be to read and write JSON using Clojure maps. We've got to plug in a new Clojure library for processing JSON:

{lang=clojure, linenos=off}
~~~
;; in your project deps
[cheshire "5.6.3"]

;; at the top of our namespace
(:require [cheshire.core :as json])
~~~

Let's add a new column to our test table and extend certain protocols:

{lang=clojure, linenos=off}
~~~
(execute! "alter table test add column data json")

(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentMap
  (sql-value [val]
    (->pgobject "json" (json/generate-string val))))
~~~

Now that you pass a native Clojure map as a parameter for the `data` field, it will turn into a `PGObject` that Postgres driver knows how to treat. But querying the table still returns `PGObject` which is not what we expect. There is another `IResultSetReadColumn` protocol to specify how the data come from the driver should be processed:

{lang=clojure, linenos=off}
~~~
(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata index]
    (pgobj->clj pgobj)))
~~~

For each `PGobject` value received from a database, the protocol calls `result-set-read-column` function. It accepts that `PGObject` object, a metadata which is some sort of additional info and an integer index specifies value's position in the result set. The function should return any Clojure value that takes its place in the final result.

Since `PGObject` represents not only `json(b)` column but any non-primitive entity, it would be a mistake to treat it as JSON. Instead, we've got to implement some sort of sub-dispatching mechanism so only objects of `json` type are processes as JSON strings. Later, you can extend it to handle other DB types.

In our case, we dispatch an instance of `PGObject` not by its type (obviously, it will always be the same) but rather by a value returned from the `(.getType)` method. There is a great opportunity for a multimethod to get onboard:

{lang=clojure, linenos=off}
~~~
;; a multimethod with a dispatch function
(defmulti pgobj->clj
  (fn [pgobj]
    (.getType pgobj)))

;; common logic for json/jsonb types
(defn- json->clj
  [pgobj]
  (-> pgobj .getValue (json/parse-string true)))

;; extending json
(defmethod pgobj->clj "json"
  [pgobj]
  (json->clj pgobj))

;; extending jsonb
(defmethod pgobj->clj "jsonb"
  [pgobj]
  (json->clj pgobj))
~~~

Since `json` and `jsonb` are different types in JDBC perspective, we have to extend the `pgobj->clj` multimethod twice for each of them. Their internal logic is the same so we wrap it into `json->clj` function to prevent copy-paste issues.

A quck check:

{lang=clojure, linenos=off}
~~~
(insert! :test {:data {:foo {:bar {:baz [1 2 3 true false nil "hello"]}}}})

(query "select id, data from test where data is not null")

({:id 7, :data {:foo {:bar {:baz [1 2 3 true false nil "hello"]}}})
~~~

That's a really great feature we've implemented so far. Any data that is JSON-serializable can be dumped into the database and restored to the original Clojure form. Use `json` type for storing fuzzy data that is subject to change over the time. But don't abuse the feature since it leads to missing the main benefit of PostgreSQL: a strict schema protecting you from dull errors.

## Theory of database arrays

The last trick you will learn in that session is how to work with Postgres arrays. Postgres provides nice typed arrays with dozens of functions to operate on them. Here is a short list of their benefits and use cases.

**Arrays are strictly typed.** If you declare an array of integers, a string cannot take a place in such an array. Instead, a `json`-based vector may contain literally everything inside.

Arrays support special operators to **concatenate or subtract** them, to find an intersection or to check if one array is a superset of other. Sometimes it simplifies the logic of a query.

Imagine you've got job and CV entities in the database. Let a job has `skills_required` column which is an array of skill ids. So does a CV entity with `skills_owns` column of the same type.

To select CVs that satisfy a certain job in skills perspective, compose a query in such a way:

{lang=sql, linenos=off}
~~~sql
select job.*
from
  cvs cv,
  jobs job
where
  cv.id = ?
  and cv.skills_required <@ job.skills_owns
~~~

The `<@` operator means "is contained by" and returns true if all the elements from the left array are found in the right side array. If you would like to reduce the strictness of a query, use `&&` "overlaps" operator that returns true if both arrays have at least one common element.

One more benefit of using arrays is they prevent you from creating extra bridge tables. If you store skills in a separate table you will have to declare two bridge tables `jobs_skills` and `cv_skills` and link entities through them. The complexity of the DB schema and queries will increase. But storing skill IDs as an `intarray` column will keep the things simple.

## Practising with arrays

Moving to practice, let's create an array field and tie it to a Clojure vector. At the moment, we cannot insert arrays with Clojure so we do it with plain SQL:

{lang=clojure, linenos=off}
~~~
(execute! "alter table test add column skill_ids integer[]")
(execute! "insert into test (skill_ids) values ('{1,2,3}'::integer[])")
~~~

Querying the database returns a special Java object represents an array:

{lang=clojure, linenos=off}
~~~
(query "select id, skill_ids from test where id = 8")

({:id 8,
  :skill_ids
  #object[org.postgresql.jdbc.PgArray 0x4afa8619 "{1,2,3}"]})
~~~

To convert arrays to Clojure we have to extend the `IResultSetReadColumn` protocol with the `PgArray` class. Import the class first:

{lang=clojure, linenos=off}
~~~
(:import org.postgresql.util.PGobject
         org.postgresql.jdbc.PgArray)
~~~

And extend the protocol:

{lang=clojure, linenos=off}
~~~
(extend-protocol jdbc/IResultSetReadColumn

  PgArray
  (result-set-read-column [pgarray metadata index]
    (let [array-type (.getBaseTypeName pgarray)
          array-java (.getArray pgarray)]
      (with-meta
        (vec array-java)
        {:sql/array-type array-type}))))
~~~

The code gets Java native array from the `PgArray` and turns it into a Clojure vector calling `vec` function. An interesting detail here is we preserve the base type of Postgres array in the result's metadata:

{lang=clojure, linenos=off}
~~~
(def _res
  (query "select id, skill_ids from test where id = 8"))

({:id 8, :skill_ids [1 2 3]})

(-> _res first :skill_ids meta)

#:sql{:array-type "int4"}
~~~

Turning a Clojure vector into a DB array is a bit tricky. First, we need to know what type the array of. Second, we have to refer the current connection to create an instance of the database array:

{lang=clojure, linenos=off}
~~~
(extend-protocol jdbc/ISQLParameter

  clojure.lang.IPersistentVector
  (set-parameter [val stmt ix]
    (let [conn (.getConnection stmt)
          array-java (into-array Object val)
          array-type (-> val meta :sql/array-type)
          array-pg (.createArrayOf conn array-type array-java)]
      (.setArray stmt ix array-pg))))
~~~

In the snippet above, the `val` is a Clojure vector we are about to send to the database. We turn it into a Java array of `Object` elements. The `array-type` variable is DB-specific array type which is "int4" in our case. Calling `createArrayOf` method creates an instance of `PgArray` class. It cannot be initialized manually because some connection-specific parameters are required. The final `.setArray` method assigns the new array as a parameter of prepared statement with the position of `ix`.

A quick check:

{lang=clojure, linenos=off}
~~~
(insert! :test {:skill_ids ^{:sql/array-type "int4"} [10 20 30]})

(query "select id, skill_ids from test where id = 9")

({:id 9 :skill_ids [10 20 30]})
~~~

We may skip the metadata when passing a vector yet JDBC still handles it properly. But to prevent weird things from happening, it's better to specify a type of array anyway. Pay attention that most of the data-processing functions do not preserve metadata belongs to the source. So if you take a vector from the query result and process it somehow, you'd better stash its metadata and attach it to the final result sent to the database.

## Conclusion

[source]: https://github.com/igrishaev/clj-java-book/blob/master/project/src/project/db.clj

So far, we've made significant progress in boosting up our database module. We established a connection between low-level Postgres types and native Clojure collections. The whole code took less than 100 lines and follows the Clojure way. You are welcome to examine its code [on GitHub][source].

It's much more convenient to have a domain that you may tweak from project to project focusing on what you exactly need rather than dealing with a monstrous system designed for everything. The won't' be a problem to make the driver work with PostGIS or any other tricky DB stuff. The approach of extending the driver on demand keeps the things simple and easy to understand which is the most valuable trait of code.
