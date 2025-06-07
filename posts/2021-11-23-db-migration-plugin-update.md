---
title: Grails Database Migration 4.0.0-RC1 Released
date: Nov 23, 2021
description: Grails Database Migration 4.0.0-RC1 Released
author: Puneet Behl
image: grails-blog-index-2.png
---

# [%title]

[%author]

[%date]

The Grails Database Migration plugin is one of the most popular plugins in the Grails ecosystem. It helps manage database changes in a structural manner and prevents inconsistencies, communication issues, and other problems.

In today's technology landscape, where application and database schemas are continuously evolving, this plugin makes it easy for the development team to keep track of database changes, quickly rollback, and modify the schema.

The plugin internally uses the Liquibase library to manage the database. When using the plugin, database migrations are represented in the form of text, either using Groovy DSL or native Liquibase XML, in one or more changelog files. There are several popular approaches to maintaining and organizing  the changelog files. We suggest you check [Grails Database Migration plugin documentation](https://grails-plugins.github.io/grails-database-migration/4.0.0-RC1/index.html) for details.

In this release, we have updated to Liquibase 4.6, Grails framework 5, and Apache Groovy 3. Refer to the release notes for 4.0.0-RC1 for more details.


## Upgrading from Liquibase 3.10 to 4.6

The update from Liquibase 3.10 to 4.6 has been a major effort, and it's allowed us to offer users  a bunch of new features, significant improvements, and bug fixes. Following are some highlights:

1. The extension classes are now loaded with the java.util.ServiceLoader system instead of the legacy custom class-finding logic. This might not impact the main Liquibase functionality, but it will affect anyone using extension classes.

2. Logging is now based on java.util.Logging, which is unlikely to directly impact applications.

3. Support has been added to enable users to connect to Liquibase Hub ([hub.liquibase.com](https://hub.liquibase.com)). This allows you to organize, monitor, and visualize Liquibase database change activity in real time.

4. Support has been added for additional databases, such as CockroachDB and Maria DB.

5. We've made significant changes to some important configuration and command APIs and added a new CLI library.

6. Newly added support for Liquibase Quality Checks allow you to run checks against changelogs and SQL scripts.

For more information, we recommend checking out [Liquibase Extension Upgrade Guide](https://docs.liquibase.com/tools-integrations/extensions/extension-upgrade-guides/home.html) and the [release notes](https://github.com/liquibase/liquibase/releases/) for each major version starting with 4.x (4.1, 4.2, and so on up to 4.6).

## Still Not Updated to Grails Framework 5?

We are excited about the recent release of Grails framework 5 and continually updating documentation and popular libraries. Now is a good time to update to Grails framework 5 and switch to Liquibase 4.

We would love to hear about your experience with the update to Grails Database Migration 4.0.0-RC1, and if you need any help with your upgrade, we're here to [support](/support.html) you.
