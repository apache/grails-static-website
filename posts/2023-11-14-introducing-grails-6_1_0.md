---
title: Introducing Grails Framework 6.1.0 Release
date: November 14, 2023
description: It highlights the changes and improvements made in this release, provides information about bug fixes, dependency upgrades, and acknowledges the contributions of various developers.
author: Puneet Behl
image: grails-blog-index-3.png
---

# [%title]

[%author]

[%date]

The Grails Foundation is excited to announce the release of Grails Framework 6.1.0! This release comes with a host of changes, bug-fixes, and improvements that will further enhance your experience with the Grails framework . Grails continues to be a powerful and efficient framework for building web applications in Groovy, and this latest version takes it to the next level. Let's dive into what's new in Grails 6.1.0.

## What's Changed

In this release, we have made several changes to improve the framework's performance and maintainability. Some of the notable changes include:

* **Update to SnakeYaml 2.2**: This update fixes the [CVE-2022-1471](https://www.cve.org/CVERecord?id=CVE-2022-1471) reported in December of last year. This unsafe deserialization problem could easily lead to arbitrary code execution under the right circumstances. For more details on this vulnerability, you can read about it on [Snyk's blog](https://snyk.io/blog/snakeyaml-unsafe-deserialization-vulnerability/).

* **Decoupling the Sitemesh Plugin**: We have introduced significant changes to decouple the Sitemesh plugin for more flexibility. Here are the details:
    1. Created a new `org.grails.plugins:sitemesh2` and moved Sitemesh specific tag libraries that require the Sitemesh2 dependency into a separate Sitemesh2 specific plugin. This plugin can now be optionally used, providing more flexibility in choosing Sitemesh versions.

    2. Introducing the [Sitemesh3 Grails Plugin](https://github.com/codeconsole/grails-sitemesh3), a new plugin that can be independently used with Grails 6. This plugin offers compatibility with the latest Sitemesh version, allowing developers to leverage the benefits of Sitemesh3 in their Grails applications.

    3. To migrate from Sitemesh 2 to Sitemesh 3, not only completely decouples Sitemesh from GSP, but it also introduces a powerful new feature that GSP has been lacking: Decorator Chaining. With SiteMesh 3, you can apply multiple decorators to a view. For instance, something like this:

       ````html
       <meta name="layout" content="my-decorator,my-other-decorator" />
       ````

       This enhancement provides more flexibility in structuring views and layouts, making it easier to create complex and dynamic page layouts.

    4. Going Further (JDK 17) - Jakarta compatible SiteMesh 3.2 is already available. Sitemesh 3 supports JDK 17, making it an easier transition to future Grails releases, such as Grails 7.0.

    5. To learn more about updates around Grails and Sitemesh, please [review the discussion](https://github.com/apache/grails-core/issues/13058). This discussion provides insights into the decisions and considerations made during the decoupling process.

    These changes aim to provide developers with more choices and the ability to use the Sitemesh version that best suits their application requirements.

* Updated Micronaut Groovy to version 3.4.0 for better compatibility.

* Escaped Javadoc special characters for improved documentation.

* Update Spring Core to 5.3.30, and Spring Boot 2.7.16.

* Updated various dependencies and plugins to their latest versions.

These changes will not only make Grails more efficient but also ensure that it remains current and compatible with the latest technologies.

## Bug Fixes and Improvements

Grails 6.1.0 also addresses several bug fixes and improvements, including:

* An update to GSP version 6.1.0.
* An update to the grails-testing-support monorepo to version 3.1.0.

These updates will enhance the stability and reliability of the framework.

## Dependency Upgrades

Keeping up with the latest dependencies is crucial for the health of any framework. In this release, we have upgraded various dependencies to their latest versions, including but not limited to the following:

* `commons-io:commons-io` to version 2.15.0.
* `org.jsoup:jsoup` to version 1.16.2.
* `org.grails:grails-datastore-gorm-hibernate5` to 8.0.1.
* `Micronaut` to version 3.10.2.
* `Gradle` to version 7.6.3.
* `Apache Tomcat` to 9.0.81.
* `JUnit Jupiter` to 5.10.0, and `JUnit Platform` to 1.10.0.
* And many more.

These upgrades ensure that Grails 6.1.0 remains compatible with the latest libraries and tools, providing developers with a robust and reliable environment. We recommend checking out the [GitHub release page](https://github.com/apache/grails-core/releases/tag/v6.1.0) for more information.

## Installing Grails 6.1.0

You can get started with Grails 6.1.0 today by visiting our [official website](https://start.grails.org/). Alternatively, you can quickly install Grails 6.1.0 using the [SDKMan](https://sdkman.io/).

1. If you don't have SDKMan installed, follow the instructions at [SDKMan Installation Guide](https://sdkman.io/install/) to set it up.

2. Once SDKMan is installed, open your terminal and run the following command to install Grails 6.1.0:

    ````shell
    sdk install grails 6.1.0
    ````

3. You're all set! To verify the installation, run:

    ````shell
    grails --version
    ````

## Upgrading Your Existing Applications to Grails 6.1.0

If you already have a Grails application and want to upgrade to the latest version, follow these steps:

1. Open the project in your favorite IDE (preferably JetBrains' IntelliJ IDEA).
2. Update your application's `gradle.propreties` file to specify Grails 6.1.0 as the desired version.

    ````properties
    grailsVersion=6.1.0
    grailsGradlePluginVersion=6.1.0
    ````

3. Make any necessary adjustments to your application code, configuration, and dependencies to ensure compatibility with the new version.

Normally, Grails Core dependencies are automatically updated using the Grails Bill of Materials (BOM). However, if you have specific versions defined in your build configuration, you may need to manually update them to align with Grails 6.1.0.

By following these steps, you can smoothly transition your existing Grails application to the exciting Grails 6.1.0.

### Exploring Alternative Approaches

If manual dependency updates seem daunting or you want a more streamlined approach, consider the following alternatives:

#### 1. Use Grails Forge Website

Visit [https://start.grails.org](https://start.grails.org) and generate a new Grails application with Grails 6.1.0. Compare the versions in the newly generated application with your existing one to identify any discrepancies. This can serve as a reference point for your update.

#### 2. Automated Dependency Update Bots

Configure automated dependency update bots like [Renovate](https://docs.renovatebot.com/) or [Dependabot](https://dependabot.com/) with your source control platform (e.g., GitHub). These bots can automatically detect and update outdated dependencies in your project, including Grails dependencies, saving you time and effort in manual updates.

With these steps and alternative approaches, you should be well on your way to enjoying the exciting features and improvements in Grails 6.1.0. Happy coding!

## Contributors

We would like to extend our heartfelt thanks to all the contributors who made Grails 6.1.0 possible. Special thanks to:

* [Scott Murphy](https://github.com/codeconsole)
* [Guillermo Calvo](https://github.com/guillermocalvo)
* [Puneet Behl](https://github.com/puneetbehl)
* [Weiqi Gao](https://github.com/weiqigao)
* [Matthew Moss](https://github.com/mattmoss)
* [Emma Richardson](https://github.com/Emrichardsone)
* [Mattias Reichel](https://github.com/matrei)

Their dedication and hard work have significantly contributed to the success of Grails 6.1.0.

With all these exciting changes and improvements, Grails 6.1.0 is set to offer an even better experience for developers working on web applications. Whether you're an experienced Grails developer or just getting started, this release has something for everyone.

Join the [Grails Slack community](https://grails.slack.com), share your feedback, and contribute to making Grails framework even better in the future. Happy coding!
