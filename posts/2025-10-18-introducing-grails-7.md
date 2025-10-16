---
version: 7.0.0
priorVersion: 7.0.0-RC2
title: Apache Grails [%version] - Release Announcement
date: October 18, 2025
description: The Apache Grails community is excited to announce the [%version] release of the Apache Grails Framework!
author: James Fredley
image: grails-blog-index-3.png

---

# [%title]

[%author]

[%date]

[%description]

This achievement reflects the dedication of our community and underscores the strength of the ASF's open source ecosystems.

Thousands, upon thousands, of hours have gone into this release, and we are incredibly grateful to everyone who contributed their time and expertise to make it happen.

Additionally, Grails has officially graduated from incubation, under the Apache Groovy project, to become a Top-Level Project (TLP) at The Apache Software Foundation (ASF).  Read more about this milestone in our [announcement blog post](/blog/2025-10-07-apache-grails-graduation-top-level-project.html).

## Why use Grails?
- Rapid application development with high developer productivity
- Full-stack web framework with everything included
- DRY & Convention-Over-Configuration: Less boilerplate, sensible defaults
- Gentle learning curve with Apache Groovy for productivity
- "Framework of frameworks" built on Apache Groovy, Spring Boot, Spring Framework, Jakarta EE, and Hibernate for enterprise foundations

## Download Source Code and Binary Distributions

[Apache Grails Downloads](/download.html)

## What's Changed
For changes made in Grails 7 prior to [%version], check out the following blog posts: 
* [Grails 7.0.0-M1](/blog/2024-12-23-grails-7-m1.html)
* [Grails 7.0.0-M3](/blog/2025-03-05-grails-7-m3.html)
* [Grails 7.0.0-M4](/blog/2025-06-10-grails-7-m4.html)
* [Grails 7.0.0-M5](/blog/2025-07-15-grails-7-m5.html)
* [Grails 7.0.0-RC1](/blog/2025-08-10-grails-7-rc1.html)
* [Grails 7.0.0-RC2](/blog/2025-09-11-grails-7-rc2.html)

### [%version] Changes:

- `org.apache.grails:grails-i18n` has been changed to `org.apache.grails.i18n:grails-i18n` and is provided transitively, remove `org.apache.grails:grails-i18n` from your dependency list
- Bump github/codeql-action from 3 to 4 @dependabot[bot] (#15135)
- Take snapshot prior to mongo update then mark any changes during beforeUpdate and update events as dirty. @codeconsole (#15143)
- Remove method from GradleUtils @matrei (#15126)
- Update grails-publish and add organization to pom @matrei (#15138)
- Add kapa.ai AI assistant widget to docs templates @jamesfredley (#15134)
- Update documentation links to grails.apache.org/docs @jamesfredley (#15131)
- Update release workflow to publish doc to grails-website @jamesfredley (#15128)
- Use consistent ISO-8601 formatting for rendering JSON Date, Calendar, LocalDateTime, and Instant properties @codeconsole (#15121)
- Centralizing Gradle Logic @jdaugherty (#15114)
- Instant should persist the same way (BsonType.DATE_TIME) as LocalDateTime and Date @codeconsole (#15111)
- Add additional Sitemesh -> grails-layout class and property moves/changes to docs @jamesfredley (#15112)
- Docs GSP Javascript: Removed deprecated example @dauer (#15106)
- 15100 - remove previous optimization for older JDKs & larger projects @jdaugherty (#15104)
- Update cyclonedx to 2.4.1 @matrei (#15099)
- #14993 - Remove spring loaded @jdaugherty (#15096)
- Update asset-pipeline and bom versions to 5.0.19 @jamesfredley (#15092)
- Update asset-pipeline and bom versions to 5.0.18 @jamesfredley (#15089)
- Add 'migrations' to grailsAppResourceDirs resolve duplicates @jamesfredley (#15084)
- Gorm service enhancements @codeconsole (#15070)
- Update spring-boot.version to 3.5.6 @jamesfredley (#15080)
- Issue-15061 Grails-doc bug in render example @dauer (#15072)
- Support for Gorm Entities with same name, but different packages @codeconsole (#15036)
- Merge v7.0.0-RC2 & Back to snapshot @jdaugherty (#15068)
- Remove additivity attribute from StackTrace logger @jamesfredley (#15066)
- Add upgrade notes for hibernate.cache.region.factory_class @jamesfredley (#15063)
- Issue-14172 Out Of Date Instructions For Creating A Plugin @dauer (#15062)
- Revise upgrading guide for Grails 7: grails-i18n plugin @jamesfredley (#15060)
- Add external references for dependency graph @jamesfredley (#15054)
- Update GitHub references and default project slug for documentation @jamesfredley (#15053)
- Update gradleToolingApiVersion to 8.14.3 @jamesfredley (#15047)
- Update grails-publish-plugin version to 0.0.1-SNAPSHOT @jamesfredley (#15050)

🚀 Features
- Remove Neo4j Forge Feature @jamesfredley (#15107)
- Remove LogbackGroovy logging feature temporarily for 7.0.0 @jamesfredley (#15098)
- 15048 - feature: reproducible gorm service implementations @jdaugherty (#15097)
- 15085 - set banner in constructor so commands & scripts have it defaulted @jdaugherty (#15094)
- feature: generate sboms for all published binary jar files @jdaugherty (#15087)
- 14977 - multiproject reloading support @jdaugherty (#15076)
- feature: allow disabling micronaut configuration by the grails plugin @jdaugherty (#15069)

💡 Improvements
- 15079 - grails-micronaut improvements @jdaugherty (#15105)
- fix(geb): minor improvements @matrei (#15103)

🐛 Bug Fixes
- chore: Fix 15102 Removed traces of incubation and PPMC @sbglasius (#15115)
- fix(geb): minor improvements @matrei (#15103)
- fix: 13851 - move GrailsMessageSourceUtils @matrei (#15101)
- Geb 8 @matrei (#15067)
- fix: wrapper should only consider release types that are a higher priority than the wrapper version when packaged @jdaugherty (#15052)
- fix: remove leftover grails-doc plugin declaration @matrei (#15051)
- 🔧 Maintenance
- Test Demonstrating Mongo beforeUpdate changes are not persisted @codeconsole (#15142)
- gson views date/calendar should have millisecond precision to match standard rendering @codeconsole (#15130)
- Web profile jar artifact restore @jamesfredley (#15125)
- Test publishing docs to /docs on apache/grails-website @jamesfredley (#15123)
- First Step in optimizing GSP Performance on Grails 7. Safe small optimizations to start @davydotcom (#15109)
- chore: Fix 15102 Removed traces of incubation and PPMC @sbglasius (#15115)
- 15048 - feature: reproducible gorm service implementations @jdaugherty (#15097)
- build reproducibility & verification issues after testing 7.0.0-RC2 @jdaugherty (#15055)

Full Changelog: [v[%priorVersion]...v[%version]](https://github.com/apache/grails-core/compare/v[%priorVersion]...v[%version])

Upgrade instructions are available in the [documentation](https://grails.apache.org/docs/[%version]/guide/upgrading.html#upgrading60x). 


## Additional Releases

The following plugins and tools are being released alongside Grails [%version]:

### Grails Spring Security 7.0.0

See the release page: [https://github.com/apache/grails-spring-security/releases/tag/v7.0.0](https://github.com/apache/grails-spring-security/releases/tag/v7.0.0)

### Grails Quartz 4.0.0

See the release page: [https://github.com/apache/grails-quartz/releases/tag/v4.0.0](https://github.com/apache/grails-quartz/releases/tag/v4.0.0)

### Grails Redis 5.0.0

See the release page: [https://github.com/apache/grails-redis/releases/tag/v5.0.0](https://github.com/apache/grails-redis/releases/tag/v5.0.0)

### Grails GitHub Actions 1.0.1

See the release page: [https://github.com/apache/grails-github-actions/releases/tag/v1.0.0](https://github.com/apache/grails-github-actions/releases/tag/v1.0.1)

### Grails Gradle Publish 0.0.2

See the release page: [https://github.com/apache/grails-gradle-publish/releases/tag/v0.0.2](https://github.com/apache/grails-gradle-publish/releases/tag/v0.0.2)

## Dependency Upgrades
In this release, we've upgraded several dependency versions, including but not limited to the following:

* Asset Pipeline 5.0.19 (now cloud.wondrify.asset-pipeline)
* Spring Framework 6.2.11
* Spring Boot 3.5.6
* See all in the [grails-bom](https://grails.apache.org/docs/[%version]/ref/Versions/Grails%20BOM.html).

## Generating a new Grails [%version] application with Grails Forge
Try out Grails today by visiting our online application generator [Grails Forge](https://start.grails.org). This is the quickest and the recommended way to get started with Grails.

After installing JetBrains' IntelliJ IDEA 2025.2 or later and the [Grails Plugin](https://plugins.jetbrains.com/plugin/18504-grails), the Grails Application Forge will also be available under New Project in IntelliJ IDEA. 

Within your newly generated project you can access the Grails CLIs with the Grails Wrapper.

See the [Types of CLI](https://grails.apache.org/docs/[%version]/guide/gettingStarted.html#_types_of_command_line_interface_cli) section in the documentation for details on each CLI.

grail-shell-cli

```shell
grailsw
```

grails-forge-cli

```shell
grailsw -t forge
```

## Installing Grails CLIs [%version] with SDKMan
Alternatively, you can quickly install Grails [%version] CLIs (grails-shell-cli and grails-forge-cli) using [SDKMan](https://sdkman.io).

See the [Types of CLI](https://grails.apache.org/docs/[%version]/guide/gettingStarted.html#_types_of_command_line_interface_cli) section in the documentation for details on each CLI.

1. If you don't have SDKMan installed, follow the instructions at [SDKMan Installation Guide](https://sdkman.io/install) to set it up.
2. Once SDKMan is installed, open your terminal and run the following command to install Grails [%version]:

    ```shell
    sdk install grails [%version]
    ```

3. You're all set! To verify the installation, run:

    ```shell
    grails --version
    ```

The Grails Shell CLI can be accessed as:

```shell
grails
``` 
or
```shell
grails-shell-cli
```
The Grails Forge CLI can be accessed as:
```shell
grails -t forge
```
or
```shell
grails-forge-cli
```    

## Upgrading Your Existing Applications to Grails [%version]
If you already have a Grails application and want to upgrade to the latest version, follow these steps:

1. Open the project in your favorite IDE (preferably JetBrains' IntelliJ IDEA 2025.2 or later).
2. Update your application's `gradle.properties` file to specify Grails [%version] as the desired version.

    ```properties
    grailsVersion=[%version]
    ```

3. Make any necessary adjustments to your application code, configuration, and dependencies to ensure compatibility with
   the new version. [See Upgrade Guide](https://grails.apache.org/docs/[%version]/guide/upgrading.html#upgrading60x)

   - remove `org.apache.grails:grails-i18n` from your dependency list, it has changed to `org.apache.grails.i18n:grails-i18n` and is provided transitively by default

Normally, Grails Core dependencies are automatically updated using the Grails Bill of Materials (BOM). However, if you
have specific versions defined in your build configuration, you may need to manually update them to align with
Grails [%version].

By following these steps, you should be able to transition your existing Grails application to Grails [%version].

### Exploring Alternative Approaches
If manual dependency updates seem daunting, or you want a more streamlined approach, consider the following alternatives:

#### 1. Use Grails Forge Website
Visit [Grails Forge](https://start.grails.org) and generate a new Grails application with Grails [%version]. Compare the
versions in the newly generated application with your existing one to identify any discrepancies. This can serve as a
reference point for your update.

#### 2. Automated Dependency Update Bots
Configure automated dependency update bots like [Renovate](https://docs.renovatebot.com) or
[Dependabot](https://dependabot.com) with your source control platform (e.g., GitHub). These bots can automatically
detect and update outdated dependencies in your project, including Grails dependencies, saving you time and effort in
manual updates.

With these steps and alternative approaches, you should be well on your way to enjoying the exciting features and
improvements in Grails [%version].

## Why should you try out Grails [%version]?
* **Leverage cutting-edge foundations**: Built on the latest Groovy 4.0.28, Spring Framework 6.2.11, Spring Boot 3.5.6, Jakarta EE, and an optimized Asset Pipeline for seamless, modern web development.
* **Boost productivity with fresh enhancements**: Packed with bug fixes, performance tweaks, and innovative features like containerized browser testing via Geb, external configuration integration streamlining your workflow.
* **Celebrate a historic milestone**: As the inaugural release under Grails' new status as an Apache Top-Level Project, it underscores two decades of innovation and the vibrant open-source community's unwavering commitment.
* **Invest in the future**: This version drives ongoing innovation, backed by an active Grails community ensuring support, updates, and evolving capabilities tailored to your needs.

## Grails Release Schedule

* **Grails [%version] Release**: Officially released on [%date], as the first stable version under the Apache Software Foundation (ASF).
* **Grails 7.0.x Patch Releases**: Scheduled at least monthly to align with Spring Boot's monthly cadence; additional releases may occur as needed for urgent fixes.
* **Grails 7.0.x Support Period**: Full updates and maintenance available until the Spring Boot 3.5.x end-of-life on June 30, 2026.
* **Grails 8.0.x Development**: Set to commence in late November 2025, immediately following the Spring Boot 4.0.0 general availability release.
* **Grails 6.x End-of-Life**: Version 6.2.3 (released January 3, 2025) marks the final 6.2.x update and the last pre-ASF release, driven by the Spring Boot 2.7.x end-of-life.

## Apache Grails Mailing Lists
### Users Mailing List
The users mailing list will be a General purpose list for questions and discussion about Grails.\
**Web Archive:** [https://lists.apache.org/list.html?users@grails.apache.org](https://lists.apache.org/list.html?users@grails.apache.org) \
**Subscribe:** Send a blank email to [users-subscribe@grails.apache.org](mailto:users-subscribe@grails.apache.org)

### Dev Mailing List
The dev mailing list is focused on the framework implementation and its evolution. \
**Web Archive:** [https://lists.apache.org/list.html?dev@grails.apache.org](https://lists.apache.org/list.html?dev@grails.apache.org) \
**Subscribe:** Send a blank email to [dev-subscribe@grails.apache.org](mailto:dev-subscribe@grails.apache.org)

When participating in mailing lists, you should never include any personally identifiable information (PII) like
your address, phone number or email address, as all messages sent to these lists are publicly accessible and archived,
meaning anyone can view your information. Make sure your email client does not add your signature with these items.

## Thank you!
A huge thank you to our amazing community for supporting the Grails Framework over the past 20 years! We’re excited for
the future and grateful for the opportunity to continue innovating and pushing Grails forward together.

## Contributors
We would like to extend our heartfelt thanks to all the contributors who made Grails [%version] possible. \
Special thanks to:

* [James Daugherty](https://github.com/jdaugherty)
* [James Fredley](https://github.com/jamesfredley)
* [Mattias Reichel](https://github.com/matrei)
* [Scott Murphy](https://github.com/codeconsole)
* [Brian Koehmstedt](https://github.com/bkoehm)
* [Søren Berg Glasius](https://github.com/sbglasius)
* [Paul King](https://github.com/paulk-asert)
* [Puneet Behl](https://github.com/puneetbehl)
* [Jonas Pammer](https://github.com/JonasPammer)
* [Gianluca Sartori](https://github.com/gsartori)
* [David Estes](https://github.com/davydotcom)
* [Michael Yan](https://github.com/rainboyan)
* [Jude Vargas](https://github.com/JudeRV)
* [Thomas Rasmussen](https://github.com/dauer)
* [Laura Estremera](https://github.com/irllyliketoast)
* [Hallie Uczen](https://github.com/shadowchaser000)
* [Jérôme Prinet](https://github.com/jprinet)
* [Stephen Lynch](https://github.com/lynchie14)
* [Andrew Herring](https://github.com/dreewh)
* [Yasuharu Nakano](https://github.com/nobeans)
* [Aaron Mondelblatt](https://github.com/amondel2)
* [Arjang Chinichian](https://github.com/arjangch)
* [Felix Scheinost](https://github.com/felixscheinost)
* [Carl Marcum](https://github.com/cbmarcum)
* [Eugene Kamenev](https://github.com/eugene-kamenev)
* [yucai](https://github.com/huangyucaigit)
* [gandharvas](https://github.com/gandharvas)
* [ihuangyucai](https://github.com/ihuangyucai)
* [zyro](https://github.com/zyro23)

Recent Contributors by Project:

* [grails-core](https://github.com/apache/grails-core/graphs/contributors[%6MonthsBackForGitHub])
* [grails-spring-security](https://github.com/apache/grails-spring-security/graphs/contributors[%6MonthsBackForGitHub])
* [grails-static-website](https://github.com/apache/grails-static-website/graphs/contributors[%6MonthsBackForGitHub])
* [grails-forge-ui](https://github.com/apache/grails-forge-ui/graphs/contributors[%6MonthsBackForGitHub])
* [grails-quartz](https://github.com/apache/grails-quartz/graphs/contributors[%6MonthsBackForGitHub])
* [grails-gradle-publish](https://github.com/apache/grails-gradle-publish/graphs/contributors[%6MonthsBackForGitHub])
* [grails-redis](https://github.com/apache/grails-redis/graphs/contributors[%6MonthsBackForGitHub])
* [grails-github-actions](https://github.com/apache/grails-github-actions/graphs/contributors[%6MonthsBackForGitHub])

[Combined Commit List](https://github.com/search?q=repo%3Aapache%2Fgrails-core+repo%3Aapache%2Fgrails-spring-security+repo%3Aapache%2Fgrails-static-website+repo%3Aapache%2Fgrails-forge-ui+repo%3Aapache%2Fgrails-quartz+repo%3Aapache%2Fgrails-gradle-publish+repo%3Agrails-redis+repo%3Agrails-github-actions+is%3Apublic&type=commits&s=committer-date&o=desc)

Their dedication and hard work have significantly contributed to the release of Grails [%version].

Join the [Grails Slack Community](https://grails.slack.com), share your feedback, and contribute to making Grails Framework even better in
the future. Happy coding!

