---
title: Grails Framework Exciting Updates
date: Jan 22, 2021
description: Grails Product Development Lead, Puneet Behl, shares a number of updates regarding the current state of the Grails framework, as well as plans for the future.
author: Puneet Behl
image: grails-blog-index-1.png
---

# [%title]

[%author]

[%date]

Tags: #grails

We wish everyone a healthy and happy new year 2021 and thank all our users and contributors for your love and support of the Grails framework. We are excited to announce some of the upcoming changes around Grails releases, which will benefit the community and help with maintainability. 

## Adoption of Semantic Versioning

Starting with version 4.0.0, the Grails framework is following semantic versioning 2.0.0 to make the release process more predictable and simple. To understand what that means, please see [the specification documentation](https://semver.org/) 

## Announcing Grails 5! 

To avoid violating semantic versioning, what had been planned to be Grails 4.1 will now be released as Grails 5. Once Grails 5 is released, we recommend users update their applications to benefit from the latest features and improvements. We believe that updating from Grails 4 to 5 should be a smooth experience as we have not made any major breaking changes in Grails 5 itself.
 
A major highlight of this release is the update to Apache Groovy 3, which includes support for Java 15 and significant other improvements around static compilation. We have also upgraded to the latest stable releases of some third-party libraries, including: 

- Apache Groovy 3, which offers several [enhancements](https://groovy-lang.org/releasenotes/groovy-3.0.html) to the language, including GDK improvements, a new parrot parser – far more flexible than the previous versions – and a collection of other new features and capabilities. 

- SpringBoot 2.4: This release adds a significant number of new features and improvements, such as improvements to the config file processing, a new startup actuator endpoint to help identify beans that are taking longer than expected to start, and an update to Spring Framework 5.3. For more information, check out [SpringBoot 2.4 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.4-Release-Notes).

- Gradle 6, which brings faster incremental Java and Groovy compilation, better version ordering, many bug fixes, and more. Refer to [Gradle Release Notes](https://docs.gradle.org/6.5/release-notes.html) for more information.

## New Release Cadence

Going forward, the Grails framework will adhere to the following release cadence:

- A patch release every 3 weeks, which will include only bug fixes and will not introduce new features or breaking changes
- A minor release every 6 weeks, which will introduce new features/functionality in a backward compatible manner
- A major release once every year, which may introduce breaking changes

This disciplined approach to releases will make the upgrade path for future minor and major releases easier.

## Grails Framework EOL Schedule

In 2021, we will be scheduling “End Of Life” for Grails 2 and 3 as follows:

- Grails 2 will be EOL after June 30th, 2021.
- Grails 3 will be EOL after September 30th, 2021.

Going forward, with each major release, the previous major release will go into maintenance and support mode. 

## The Road Ahead

We are excited to continue to advance the Grails framework in the year 2021 with the support of the new Grails Foundation and great community. We will be focusing on improving the performance of the Grails application and user experience. In addition, we are planning a number of enhancements including:

- Making GSP more modular and independent of the Grails framework
- Transaction support in GORM for MongoDB 
- Native support for web-sockets
- Continuously improving Grails documentation (especially around plugins, Micronaut configuration, HTTP Client, and other features)
- Better integration with Micronaut, such as declarative HTTP Client
- Better support for Kafka, including creating Kafka listeners through a plugin and more