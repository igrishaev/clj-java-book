# Introduction

I often hear that question especially from those who are new to Clojure. Phasing varies in time but the meaning is the same:

> I fell in love with Clojure and would like to program on it on daily basis. But I've got no Java experience in the past. Do I need to know Java? Should I spend some time learning Java first?

I clearly remember same thoughts have been worried me before I dove into the Clojure world. I knew Clojure relies on Java VM heavily but I've got poor Java experience in the past, unfortunately. Just some occasional work on legacy code and an Android app almost completely copy-pasted from public manuals.

Those bits of Java knowledge were not sufficient to feel confident with the platform, I thought. What if I need to call Java code from Clojure? Will my Java-skilled teammates be laughing at me pointing out I just conjure on magic stuff without any knowledge of what's going on under the hood?

## Two answers

My eagerness for Clojure finally won, and today I'd like to summarize the following. The short answer to that question is **no**, you don't need to be proficient in Java when starting with Clojure. If you are new to that language, don't be afraid that the whole Java machinery suddenly comes down on you. Perhaps you'll touch Java guts one day, but it will happen in many days or months since the first line of code written.

I hope most of you who have been doubting feel more confident now.

By default, Clojure brings everything you need for a quick start just out from the box. There are great collections, shortcuts to read and write files and special reference types to keep state. Plenty of useful libraries might be connected to your project by adding a single line into a config file. Most of them provide clear API that usually takes a Clojure map and return a map as well.

Clojure has been developing for ten years so far and thus most of the problems you will be trying to solve have already been completed with great libraries. Serving HTTP requests, reading and writing any file formats, fetching data from a local or remote resource, etc might be set up with relative ease by re-using Clojure code developed by programmers all over the world.

Clojure idioms persuade you to develop simple APIs that are easy to use and *understand*. For example, to start a web-server, you declare a handler function that takes and returns a map. You only need to read the document describes the semantics of each field for those maps. The same approach works for fetching data via HTTP protocol. Again, the input and output data are just Clojure structures consist of maps and vectors.

Let me stress it again: no, you don't need to be a Java expert. If Clojure beckons you, start your journey with no doubts.

The full response to the subject question takes a bit longer since it tries to be more realistic. If you program in Clojure regularly and it has become your primary language, sooner or later you will have to deal with Java. This is inevitable, period. The more business problems you are trying to solve, the more tools and levers you should operate on. The underlying Java world is full of such tools and you would be better to get closer to them.

Java is a platform has been developing for several decades. There are both positive and negative opinions on its nature, so arguing across the Internet never stops. But the facts are, Java is a really mature platform with incredible amounts of time and money invested into it. Rewriting everything that has been solved with Java before would be a crazy idea. Let's briefly go through a list of cases when you might need to refer to Java rather than coding from scratch.

## Reusing Java code

For the whole history of Java, millions of lines of code have been written. A significant part of that code is open source. Thousands of people work every day on fixing bugs and adding features. Some of the libraries haven't been updating for several years not because they are abandoned but because they are complete. They solve problems they were aimed at without any claim.

In Clojure, one day you will face such a non-standard task as parsing some legacy binary format or communicate with a third party service. Every time you start writing your own solution for that, check if you are inventing the wheel first. Because most likely there is a Java library for that, being developed and debugged for several years. You might think your own solution is better and clearer, but no it is not. Qualitative software really takes years to become at least sufficient not even great. Such a simple task as parsing an RSS feed or User-Agent header conceals dozens of hidden tricks. For your own sanity, you'd better not to open Pandora box by writing everything from scratch.

Many well-known Clojure libraries are just thin wrappers around solid Java code. Inside, they might look ugly and full of mutable state and weird tricks, etc. But they work, which is most important. Reusing Java code really boosts the Clojure development process, consumes time and nerves.

## IO capabilities

Another reason for writing low-level Java code is to take control over IO operations. Java provides mighty capabilities for sending and accepting data to and from no matter if we deal with files, network or processes. Instead, Clojure was designed in terms of being just a guest system. It reuses the host environment (Java in our case) to provide high-level abstractions, e.g flexible collections, smart multimethod dispatching, etc. But it doesn't try to overshadow such the original low-level features as input and output or interaction with the operating system.

Clojure ships some wrappers around Java's native IO subsystem and usually they are enough to write or read a file. But one day you may face a tough situation when plenty of streams are involved all together so you need to orchestrate them. This is the case you definitely need Java interop.

## Source code problems

It might happen that the project you are working on relies on a special feature that's difficult to implement. Say, analysing fingerprints, face or voice recognition. Your customer may own a licence for a commercial library written in Java. Probably they've been using this library in all their Java projects so the Clojure solution should also follow it to make results relevant. So you've got a `*.jar` file without sources and an HTML file next to it describes classes and methods available from the library.

Surely you can decompile it and spend some time looking at its guts, but probably it won't change anything. Sometimes, you are really clamped by business demands so writing your own pure Clojure solution is out of a discussion. In that case, you have to go down to the Java basement.

## Your career growth

The final and the most general reason for inventing time in Java is your career growth. If you program with Clojure on a daily basis, the businesses will offer more and more opportunities for you. To deal with them, you need to broaden your horizons. Diving into low-level Java world is exactly such a thing that raises your skills and thus boosts the career growth. If you in love with Clojure and feel confident with your intentions, you cannot bypass Java interop.

That was the long answer to the question if you need to know Java. So far, I hope you feel more confident than before and nothing prevents you from getting started with Clojure.

## What is that book about

This paper is focused on one narrow topic: how to use Java capabilities in Clojure. There are plenty of tutorials and books about Clojure so far, but most of them consider their readers as new ones in programming. They consist basically of defining functions and transforming collections. There is no room for extended topics as the result.

Java interop is one of such topics. When you learn Clojure, usually you are focused primarily in the language by its own: functions and namespaces, atoms, etc. Everything looks simple and elegant so far. But once you've got a real Clojure job, it turns out the codebase is full of tricky calls to Java objects. Here, *the real work* starts.

You feel confused because the code doesn't look like the tutorials you read. So the aim of that book is to get you prepared for such a situation. Together, we will go through a set of chapters each of them expounds something useful on Java interop.

## The structure

The **initial chapter** explains the basics on how to use Java classes in a Clojure project. There, I provide examples of importing such basic Java files as `java.io.File` or `java.util.Date` and performing simple operations on them. Yet it is not another Java tutorial. Everything we talk about is looked through a Clojure prism. The chapter also shares advice and common practices about organizing namespaces. At the end of the chapter, I expect a reader is familiar with Java interop even he or she has never worked with it (or at least they are not afraid anymore).

Then, a set of code-driven sessions follows. I would like to take apart several business cases. In each of them, you are going to solve a problem by writing Clojure code that relies on Java capabilities. What I'd like to highlight is all the cases are real and borrowed from the projects I've been worked on personally. None of them was invented just to make my statements louder, no. Everything described in that book is for real.

The **first coding session** shows how to extract as much information as possible from a User-Agent HTTP header comes to the server. This is definitely such a task that is better never be done with bare hands. I'll show how a Java library might help by calling proper classes and turning the result into plain Clojure data.

The **second task** affects an interesting topic: how to clean HTML markup out from unwanted and malicious data. It doesn't occur often in our daily job, but once it does, the days and weeks might be spent to overcome the problem. Again, we will rely on proven Java solutions. I'll show how to deal with tricky business rules to prevent certain media resources.

In the **third chapter**, let's talk about how to extend the JDBC driver to let it work with non-primitive data types. I consider this chapter is quite important since a database is the most valuable part of a project. The more features a database can perform, the less code you'll need for the business logic. I'll demonstrate how to build a seamless connection between native Clojure data and PostgreSQL backend.

The **fourth code sample** highlights Java IO capabilities, namely different types of input streams and how to conjoin them together. We will build a pipeline that pulls out a huge amount of data from a zipped CSV file without touching a hard drive or consuming the whole available memory.

The **fifth lesson** explains how to take control over Java processes. Although most of the business tasks might be solved with Clojure or Java code, sometimes you need to communicate with a third-party programme installed on your computer. Clojure brings poor capabilities for running external programs so we'll take a romp with the subject. For a bonus, I'll show how to control your browser with Clojure functions.

There has been said enough to raise your impatience. I hope you cannot wait to go further. So am I.
