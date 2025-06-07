---
title: Grails Framework 5.1 Released
date: Dec 18, 2021
description: Grails framework 5.1.0 improves Gradle 7.2 support and upgrades to Spring Boot 2.6.1, GORM 7.2, and Micronaut framework 3.2.0
author: Puneet Behl
image: grails-blog-index-2.png
---

# [%title]

[%author]

[%date]

The Grails Foundation™ is pleased to announce a new minor release: [**Grails framework 5.1**](https://github.com/apache/grails-core/releases/tag/v5.1.0)!

This release of the Grails framework includes a number of bug fixes and Gradle 7.2 compatibility changes (the Gradle task definitions with [incorrectly defined input output will now fail the build](https://docs.gradle.org/7.0/userguide/upgrading_version_6.html#task_validation_problems_are_now_errors)), plus a bunch of dependencies updates. For more information, please check the [**Grails 5.1 release notes**](https://github.com/apache/grails-core/releases/tag/v5.1.0).

## Updated Dependencies

The dependencies that have been updated include:

### Spring Boot 2.6.1
One important change that accompanies Spring Boot 2.6 is that circular references are prohibited. In order to maintain backwards compatibility for Grails users, Grails framework 5.1 explicitly enables circular references by default. This will no longer be the case once Grails framework 6 is released. For more information check the [Spring Boot 2.6 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.6-Release-Notes).

### Micronaut 3.2
Updating to Micronaut 3.2 should not affect Grails applications. Please check [the release notes](https://github.com/micronaut-projects/micronaut-core/releases/tag/v3.2.2) for more details. 

### GORM for Hibernate 7.2, which updates to Hibernate 5.6
This version updates to [Hibernate ORM 5.6.2](https://in.relation.to/2021/12/08/hibernate-orm-562/). There are several other bug fixes and dependency updates. For complete details,  please check [the release notes for GORM for Hibernate 7.2](https://github.com/grails/gorm-hibernate5/releases/tag/v7.2.0).

### GORM for MongoDB 7.2, which updates to underline Mongo driver 4.4
This version updates the underlying Mongo driver to 4.4. Read [What's New — MongoDB Java Sync 4.4](https://docs.mongodb.com/drivers/java/sync/current/whats-new/#std-label-version-4.4) to understand what has changed in the Java driver. We also recommend that you check [the release notes for GORM for MongoDB 7.2](https://github.com/grails/gorm-mongodb/releases/tag/v7.2.0) for a list of all the changes and dependency updates.

### GORM for Neo4j
Please check [the release notes](https://github.com/grails/gorm-neo4j/releases/tag/v7.2.0) for complete information about the changes in this release.

## A Note About Semantic Versioning

Grails framework 4 was the first version of the Framework that followed semantic versioning. Grails framework 5.1.0 is a minor release; thus, it should not contain breaking changes. It contains new features, the patches introduced in Grails 5.0.x releases, and minor upgrades to Grails dependencies, such as Spring Boot, Micronaut, and GORM. Please upgrade quickly to get the latest updates into your Grails 5 applications.

## Upgrading to Grails Framework 5.1

As this is a minor release, updating to Grails framework 5.1 should be pretty straightforward. Here are step-by step instructions:

In your `gradle.properties` file,

- Update property `grailsVersion` to 5.1.0
- Update property `gorm.version` to 7.2.0

In your `build.gradle` file

- Update Gradle `hibernate5` plugin to 7.2.0
- Under dependencies, update `hibernate-core` to 5.6.2.Final

## The Road Ahead

### Spring Security Core Plugin

Currently, the Spring Security Core Grails plugin still uses Spring Security Core 5.1.13.RELEASE which is EOL. We are planning to update the plugin to use the latest version of Spring Security. See [Spring Security Versions · spring-projects/spring-security Wiki · GitHub](https://github.com/spring-projects/spring-security/wiki/Spring-Security-Versions#released-versions) for more information.

### Leveling up the Grails CLI!

The current implementation of Grails CLI is difficult to test and unadaptable. For example, configuring automation tools to update dependencies is impossible. Also, the CLI does not work offline, and it is very difficult to customize it. We plan to work on implementing a new, super-flexible, easy-to-customize CLI that eliminates these limitations.

### Consolidating Grails Plugins

Currently, Grails plugins are distributed across multiple Github organizations, including [gpc - Grails Plugin Collective · GitHub](https://github.com/gpc), [Grails Plugins · GitHub](https://github.com/grails-plugins), and [grails3-plugins · GitHub](https://github.com/grails3-plugins). We are planning to consolidate a list of all active plugins into one place. We will soon share more information about this initiative in the Grails blog.

## Need help upgrading to Grails® framework 5.1?

We are excited about the release of Grails framework 5.1 and look forward to hearing about your experience with it. Please feel free to reach out to us if you have any questions or need help updating your applications. Community members and members of the core development team are available to answer questions on [our Slack channel](https://slack.grails.org/), and hourly commercial support can be purchased [here](https://objectcomputing.com/products/grails/consulting-support).

