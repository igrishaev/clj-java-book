

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




Parsing User Agent string

Task

Phases: library; invoce; parse result

1 The lib

2 sample code

3 result



Sanitize HTML

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
