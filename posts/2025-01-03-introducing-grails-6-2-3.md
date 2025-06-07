---
title: Introducing Grails Framework 6.2.3 Release
date: January 3, 2025
description: It highlights the critical bug fix in this release.
author: James Daugherty
image: grails-blog-index-3.png
---

# [%title]

[%author]

[%date]

The Grails Foundation is excited to announce the release of Grails framework 6.2.3. This latest version delivers a key bug fix.

## Bug Fixes and Improvements

Grails 6.2.3 addresses a critical bug fixes:

* Fixes an edge cases that causes a cast class exception when inherited commands are used as an action parameter.


## Dependency Upgrades

Keeping up with the latest dependencies is crucial for the health of any framework. In this release, we have upgraded various dependencies to their latest versions, including but not limited to the following:

* `org.grails:grails-gradle-plugin` to version 6.2.4
* `org.apache.tomcat:tomcat-jdbc` to version 9.0.98
* `org.apache.tomcat.embed:tomcat-embed-core` to version 9.0.98

These upgrades ensure that Grails 6.2.3 remains compatible with the latest libraries and tools, providing developers with a robust and reliable environment. We recommend checking out the [GitHub release page](https://github.com/apache/grails-core/releases/tag/v6.2.3) for more information.

## Installing Grails 6.2.3

You can get started with Grails 6.2.3 today by visiting our [official website](https://start.grails.org/). Alternatively, you can quickly install Grails 6.2.3 using the [SDKMan](https://sdkman.io/).

1. If you don't have SDKMan installed, follow the instructions at [SDKMan Installation Guide](https://sdkman.io/install/) to set it up.

2. Once SDKMan is installed, open your terminal and run the following command to install Grails 6.2.3:

    ````shell
    sdk install grails 6.2.3
    ````

3. You're all set! To verify the installation, run:

    ````shell
    grails --version
    ````

## Upgrading Your Existing Applications to Grails 6.2.3

If you already have a Grails application and want to upgrade to the latest version, follow these steps:

1. Open the project in your favorite IDE (preferably JetBrains' IntelliJ IDEA).
2. Update your application's `gradle.propreties` file to specify Grails 6.2.3 as the desired version.

    ````properties
    grailsVersion=6.2.3
    grailsGradlePluginVersion=6.2.4
    ````

3. Make any necessary adjustments to your application code, configuration, and dependencies to ensure compatibility with the new version.

Normally, Grails Core dependencies are automatically updated using the Grails Bill of Materials (BOM). However, if you have specific versions defined in your build configuration, you may need to manually update them to align with Grails 6.2.3.

By following these steps, you can smoothly transition your existing Grails application to the exciting Grails 6.2.3.

### Exploring Alternative Approaches

If manual dependency updates seem daunting, or you want a more streamlined approach, consider the following alternatives:

#### 1. Use Grails Forge Website

Visit [https://start.grails.org](https://start.grails.org) and generate a new Grails application with Grails 6.2.3. Compare the versions in the newly generated application with your existing one to identify any discrepancies. This can serve as a reference point for your update.

#### 2. Automated Dependency Update Bots

Configure automated dependency update bots like [Renovate](https://docs.renovatebot.com/) or [Dependabot](https://dependabot.com/) with your source control platform (e.g., GitHub). These bots can automatically detect and update outdated dependencies in your project, including Grails dependencies, saving you time and effort in manual updates.

With these steps and alternative approaches, you should be well on your way to enjoying the exciting features and improvements in Grails 6.2.3. Happy coding!

Join the [Grails Slack community](https://grails.slack.com), share your feedback, and contribute to making Grails framework even better in the future. Happy coding!
