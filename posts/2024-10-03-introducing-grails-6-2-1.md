---
title: Introducing Grails Framework 6.2.1 Release
date: October 3, 2024
description: It highlights the changes and improvements made in this release, provides information about bug fixes, dependency upgrades, and acknowledges the contributions of various developers.
author: James Fredley
image: grails-blog-index-3.png
---

# [%title]

[%author]

[%date]

The Grails Foundation is excited to announce the release of Grails framework 6.2.1. This latest version delivers critical bug fixes and reintroduces key features based on community feedback.

## What's Changed

The 6.2.1 release, while being a patch release, brings back the Grails Shell, Profiles and Wrapper (as an optional feature) and addresses some bugs.

* **Grails Shell, Profiles and Wrapper**: 
    * Reintroduces the Grails Shell and Profiles in response to community feedback, addressing a key feature loss identified by users including:
      * enables the IntelliJ Grails Plugin to load in IntelliJ  
      * execution of Grails scripts that have not been migrated to Gradle such as **grails list-plugins** and **grails dbm-update**
      * execution of custom company/internal Grails scripts
      * continued use of custom company/internal profiles
    * Grails Wrapper is now an optional feature from Grails Forge via the Grails distribution or [https://start.grails.org/](https://start.grails.org/)
      * you can now run **./grailsw update-wrapper** to get the latest grails wrapper version based on the Grails version

## Bug Fixes and Improvements

Grails 6.2.1 also addresses several bug fixes and improvements, including:

* Fixes a lack of a “controller” attribute in GSP tags with a “resource” attribute
* Fixes a bug in LinkGenerator when domain classes have a static id mapping to another property, that is not “id”
* Fixes a problem finding the main class when running CLI commands
* Fixes an edge cases that causes a cast class exception when the same variable name is used as arguments to multiple actions

These updates will enhance the stability and reliability of the framework.

## Dependency Upgrades

Keeping up with the latest dependencies is crucial for the health of any framework. In this release, we have upgraded various dependencies to their latest versions, including but not limited to the following:

* `com.github.javaparser:javaparser-core` to version 3.25.10
* `ch.qos.logback:logback-classic` to version 1.2.13
* `org.springframework:spring-core` to version 5.3.39
* `org.grails:grails-testing-support` to version 3.2.2
* `org.apache.tomcat:tomcat-jdbc` to version 9.0.95
* `org.grails.plugins:fields` to version 5.1.0
* `org.grails.plugins:scaffolding` to version 5.1.2
* `org.grails:grails-shell` to version 6.2.1
* `org.grails:grails6-wrapper` to version 4.0.1
* `org.grails.plugins:gsp` to version 6.2.2
* `org.grails.profiles:base` to version 6.0.1
* `org.grails.profiles:web` to version 6.0.1
* `org.grails.profiles:profile` to version 6.0.1
* `org.grails.profiles:plugin` to version 6.0.1
* `org.grails.profiles:web-plugin` to version 6.0.1
* `org.grails.profiles:rest-api` to version 6.0.1
* `org.grails.profiles:rest-api-plugin` to version 6.0.1
* `org.grails.profiles:react` to version 6.0.1
* `org.grails.profiles:vue` to version 6.0.1
* `org.grails.profiles:angular` to version 9.0.1
* And many more transitive dependencies.

These upgrades ensure that Grails 6.2.1 remains compatible with the latest libraries and tools, providing developers with a robust and reliable environment. We recommend checking out the [GitHub release page](https://github.com/apache/grails-core/releases/tag/v6.2.1) for more information.

## Installing Grails 6.2.1

You can get started with Grails 6.2.1 today by visiting our [official website](https://start.grails.org/). Alternatively, you can quickly install Grails 6.2.1 using the [SDKMan](https://sdkman.io/).

1. If you don't have SDKMan installed, follow the instructions at [SDKMan Installation Guide](https://sdkman.io/install/) to set it up.

2. Once SDKMan is installed, open your terminal and run the following command to install Grails 6.2.1:

    ````shell
    sdk install grails 6.2.1
    ````

3. You're all set! To verify the installation, run:

    ````shell
    grails --version
    ````

## Upgrading Your Existing Applications to Grails 6.2.1

If you already have a Grails application and want to upgrade to the latest version, follow these steps:

1. Open the project in your favorite IDE (preferably JetBrains' IntelliJ IDEA).
2. Update your application's `gradle.propreties` file to specify Grails 6.2.1 as the desired version.

    ````properties
    grailsVersion=6.2.1
    grailsGradlePluginVersion=6.2.1
    ````

3. Make any necessary adjustments to your application code, configuration, and dependencies to ensure compatibility with the new version.

Normally, Grails Core dependencies are automatically updated using the Grails Bill of Materials (BOM). However, if you have specific versions defined in your build configuration, you may need to manually update them to align with Grails 6.2.1.

By following these steps, you can smoothly transition your existing Grails application to the exciting Grails 6.2.1.

### Exploring Alternative Approaches

If manual dependency updates seem daunting, or you want a more streamlined approach, consider the following alternatives:

#### 1. Use Grails Forge Website

Visit [https://start.grails.org](https://start.grails.org) and generate a new Grails application with Grails 6.2.1. Compare the versions in the newly generated application with your existing one to identify any discrepancies. This can serve as a reference point for your update.

#### 2. Automated Dependency Update Bots

Configure automated dependency update bots like [Renovate](https://docs.renovatebot.com/) or [Dependabot](https://dependabot.com/) with your source control platform (e.g., GitHub). These bots can automatically detect and update outdated dependencies in your project, including Grails dependencies, saving you time and effort in manual updates.

With these steps and alternative approaches, you should be well on your way to enjoying the exciting features and improvements in Grails 6.2.1. Happy coding!

## Contributors

We would like to extend our heartfelt thanks to all the contributors who made Grails 6.2.1 possible. Special thanks to:

* [Scott Murphy](https://github.com/codeconsole)
* [Mattias Reichel](https://github.com/matrei)
* [James Fredley](https://github.com/jamesfredley)
* [Puneet Behl](https://github.com/puneetbehl)
* [Søren Berg Glasius](https://github.com/sbglasius)
* [David Estes](https://github.com/davydotcom)
* [Paul King](https://github.com/paulk-asert)

Their dedication and hard work have significantly contributed to the success of Grails 6.2.1.

With all these exciting changes and improvements, Grails 6.2.1 is set to offer an even better experience for developers working on web applications. Whether you're an experienced Grails developer or just getting started, this release has something for everyone.

Join the [Grails Slack community](https://grails.slack.com), share your feedback, and contribute to making Grails framework even better in the future. Happy coding!
