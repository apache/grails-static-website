---
title: Grails 5.0.0 RC1 Released
date: August 11, 2021
description: The Grails Foundation is pleased to announce the first release candidate of Grails framework 5.0.0.
author: Puneet Behl
image: grails-blog-index-3.png
---

# [%title]

[%author]

[%date]

The Grails® Foundation is pleased to announce the first release candidate of [Grails framework 5.0.0](https://github.com/apache/grails-core/releases/tag/v5.0.0.RC1)! With this release, we are one step closer to the Grails framework's next GA release!

## Overview

Grails framework 5 represents a significant upgrade, with the Framework now rebased on top of an updated minimum set of dependencies including:

- Spring 5.3.9
- Spring Boot 2.5.3
- Apache Groovy 3
- Hibernate 5.5
- Gradle 6.9

A major highlight of Grails framework 5 is support for Apache Groovy 3, which comes with the brand new Parrot parser and a bunch of new features and capabilities. The Parrot parser supports additional syntax and language features, such as lambda expressions, default methods with interfaces, and a lot more. See [the release notes for Groovy 3.0](https://groovy-lang.org/releasenotes/groovy-3.0.html#releasenotes) for details.

Grails framework 5 also upgrades to Spring 5.3.9 and Spring Boot 2.5.3, which have a minimum Gradle requirement of 6.8 or later and a default Apache Groovy version of 3. We strongly recommend that users check out the following Spring technologies release notes for more information: 

- [Spring Boot 2.5](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.5-Release-Notes)
- [Spring Framework 5.3](https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-5.x#upgrading-to-version-53)

This release also provides support for Java 15.

Many Grails plugins should work with Grails framework 5. For those in the Grails plugin community, now is the time to upgrade your plugins and verify they continue to work with Grails framework 5.

## Important Changes

- Dot-based(a.b.c) access to config is deprecated and will be removed in future.
- The grails-publish-plugin has been removed.
- The grails-gradle-plugin has been moved out of grails-core and may follow a separate versioning from grailsCore, so you should decouple the grailsVersion Gradle property from grailsGradlePluginVersion in existing applications.
- `grails.util.Metadata` includes breaking changes as part of improvements.  

##  Download
The easiest way to get started is to use [SDKMAN! (The Software Development Kit Manager)](https://sdkman.io/) to download Grails 5.0.0.RC1.
Alternatively, you could download the Grails Binary directly from the [Github Release](https://github.com/apache/grails-core/releases/download/v5.0.0.RC1/grails-5.0.0.RC1.zip) page.

## Conclusion

Thanks to all those who contributed to this release. Please [follow us on Twitter @grailsframework](https://twitter.com/grailsframework) for the most up-to-date information on the framework. 
