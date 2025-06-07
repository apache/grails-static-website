---
title: Introducing Grails Framework 6.2.2 Release
date: November 20, 2024
description: It highlights the changes and improvements made in this release, provides information about bug fixes, dependency upgrades, and acknowledges the contributions of various developers.
author: James Fredley
image: grails-blog-index-3.png
---

# [%title]

[%author]

[%date]

The Grails Foundation is excited to announce the release of Grails framework 6.2.2. This latest version delivers bug fixes and reintroduces key features based on community feedback.

## What's Changed

In this release, we have made several changes to improve the framework's performance and maintainability. Some of the notable changes include:

* **Simplify Generated Build**: 
    * Consolidates Gradle Plugin configuration to buildscript{}, plugins{} and apply plugin: in build.gradle
    * Changes Gradle buildSrc/build.gradle and Settings File to non-default, optional features
    * When the Gradle Settings File feature is selected, it only contains one line defining the root project
    * When the Gradle buildSrc feature is selected buildscript{} is not generated, they are equivalent
    * These changes allow Gradle plugins to be defined in one location and for consolidation of dependency version resolution.

* **Grails Wrapper**:
    * Make grails-wrapper a default feature
    * Ensure grails-wrapper files exist in native grails-forge-cli builds
    * Put profile information in all locations required by grails-shell and grails-wrapper

## Bug Fixes and Improvements

Grails 6.2.2 also addresses several bug fixes and improvements, including:

* Set asset-pipeline skipNonDigest to true, which reduces jar/war files size
* Include development, test and production dataSource configurations in generated application.yml
* Default generated applications to Java 11, 17 is optional
* gorm-hibernate5: Create foreign key constraint when tablePerHierarchy=false
* Remove -plain from Grails plugin jar filenames
* fields: Fix for invalid class not rendering on widget
* scaffolding: remove dependency on org.grails:grails-dependencies

These updates will enhance the stability and reliability of the framework.

## Dependency Upgrades

Keeping up with the latest dependencies is crucial for the health of any framework. In this release, we have upgraded various dependencies to their latest versions, including but not limited to the following:

* `org.grails:grails-gradle-plugin` to version 6.2.3
* `org.codehaus.groovy:groovy` to version 3.0.23
* `org.grails.plugins:hibernate5` to version 8.1.1
* `com.bertramlabs.asset-pipeline` to version 4.5.1
* `com.bertramlabs.asset-pipeline:asset-pipeline-grails` to version 4.5.1
* `org.grails.plugins:fields` to version 5.1.1
* `org.grails.plugins:scaffolding` to version 5.1.3
* `org.grails.plugins:gsp` to version 6.2.4
* `org.apache.tomcat:tomcat-jdbc` to version 9.0.97
* `org.apache.tomcat.embed:tomcat-embed-core` to version 9.0.97
* `org.apache.ant:ant` to version 1.10.15
* `org.aspectj:aspectjrt` to version 1.9.22.1
* `org.mongodb:mongo-driver` to version 4.11.5
* And many more transitive dependencies.

These upgrades ensure that Grails 6.2.2 remains compatible with the latest libraries and tools, providing developers with a robust and reliable environment. We recommend checking out the [GitHub release page](https://github.com/apache/grails-core/releases/tag/v6.2.2) for more information.

## Installing Grails 6.2.2

You can get started with Grails 6.2.2 today by visiting our [official website](https://start.grails.org/). Alternatively, you can quickly install Grails 6.2.2 using the [SDKMan](https://sdkman.io/).

1. If you don't have SDKMan installed, follow the instructions at [SDKMan Installation Guide](https://sdkman.io/install/) to set it up.

2. Once SDKMan is installed, open your terminal and run the following command to install Grails 6.2.2:

    ````shell
    sdk install grails 6.2.2
    ````

3. You're all set! To verify the installation, run:

    ````shell
    grails --version
    ````

## Upgrading Your Existing Applications to Grails 6.2.2

If you already have a Grails application and want to upgrade to the latest version, follow these steps:

1. Open the project in your favorite IDE (preferably JetBrains' IntelliJ IDEA).
2. Update your application's `gradle.propreties` file to specify Grails 6.2.2 as the desired version.

    ````properties
    grailsVersion=6.2.2
    grailsGradlePluginVersion=6.2.3
    ````

3. Make any necessary adjustments to your application code, configuration, and dependencies to ensure compatibility with the new version.

Normally, Grails Core dependencies are automatically updated using the Grails Bill of Materials (BOM). However, if you have specific versions defined in your build configuration, you may need to manually update them to align with Grails 6.2.2.

By following these steps, you can smoothly transition your existing Grails application to the exciting Grails 6.2.2.

### Exploring Alternative Approaches

If manual dependency updates seem daunting, or you want a more streamlined approach, consider the following alternatives:

#### 1. Use Grails Forge Website

Visit [https://start.grails.org](https://start.grails.org) and generate a new Grails application with Grails 6.2.2. Compare the versions in the newly generated application with your existing one to identify any discrepancies. This can serve as a reference point for your update.

#### 2. Automated Dependency Update Bots

Configure automated dependency update bots like [Renovate](https://docs.renovatebot.com/) or [Dependabot](https://dependabot.com/) with your source control platform (e.g., GitHub). These bots can automatically detect and update outdated dependencies in your project, including Grails dependencies, saving you time and effort in manual updates.

With these steps and alternative approaches, you should be well on your way to enjoying the exciting features and improvements in Grails 6.2.2. Happy coding!

## Contributors

We would like to extend our heartfelt thanks to all the contributors who made Grails 6.2.2 possible. Special thanks to:

* [Scott Murphy](https://github.com/codeconsole)
* [Mattias Reichel](https://github.com/matrei)
* [James Fredley](https://github.com/jamesfredley)
* [Søren Berg Glasius](https://github.com/sbglasius)
* [David Estes](https://github.com/davydotcom)
* [Guillermo Calvo](https://github.com/guillermocalvo)
* [James Daugherty](https://github.com/jdaugherty)

Their dedication and hard work have significantly contributed to the success of Grails 6.2.2.

With all these exciting changes and improvements, Grails 6.2.2 is set to offer an even better experience for developers working on web applications. Whether you're an experienced Grails developer or just getting started, this release has something for everyone.

Join the [Grails Slack community](https://grails.slack.com), share your feedback, and contribute to making Grails framework even better in the future. Happy coding!
