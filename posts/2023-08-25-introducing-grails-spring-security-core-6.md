---
title: Introducing Grails Spring Security Core Plugin 6.0.0 - Elevated Security, Grails 6 Compatibility, Enhanced Commands, and Effortless Documentation
date: August 25, 2023
description: Exciting News from the Grails Foundation - Unveiling Grails Spring Security Core Plugin 6.0.0
author: Puneet Behl
image: grails-blog-index-3.png
---

# [%title]

[%author]

[%date]

**Exciting News from the Grails Foundation: Unveiling Grails Spring Security Core Plugin 6.0.0**

Greetings, fellow developers! Prepare to be amazed by the latest release – Grails Spring Security Core Plugin 6.0.0! This isn't just an upgrade in security; it's a journey towards greater heights for your applications. Packed with updates and improvements, this release doesn't just enhance security; it also streamlines your development experience. Let's explore the exciting changes this release brings.

## Seamless Grails 6 Compatibility with Upgraded Commands

We are intentional about making the transition to Grails 6 seamless. Grails Spring Security Core Plugin 6.0.0 now boasts upgraded commands fully compatible with Grails 6. This is crucial because the old scripts you might have been using would have caused compatibility issues with Grails 6. But worry not! We've revamped these commands, ensuring a smooth transition and allowing you to continue using them seamlessly.

Do you remember running a script like `s2-quickstart` using the command:

```bash
grails s2-quickstart com.yourapp User Role
```

This command was used to create user and role classes (and optionally a requestmap class) in your specified package.

Fast forward to Grails 6, things have evolved. Now, you can trigger these commands using Gradle tasks, like so:

```bash
./gradlew runCommand "-Pargs=s2-quickstart com.yourapp User Role"
```

The `runCommand` Gradle task allows you to execute various Grails ApplicationCommands, just like the good old `s2-quickstart`. The difference? It's sleeker, perfectly aligned with Grails 6, and propels your app into modern development.

## Spring Security Leveled Up to 5.8.6

Security is paramount, and that's why we've given the underlying Spring Security framework a power boost in Grails Spring Security Core Plugin. It's now fueled by version 5.8.6 of Spring Security. This ensures that your application benefits from the latest security enhancements and bug fixes from the Spring Security team. By staying up to date with Spring Security versions, your app is fortified against emerging threats and vulnerabilities.

## Navigating Docs Made Effortless

We've heard your feedback – navigating documentation shouldn't be a chore. That's why we've spruced up the Internal Docs task. This magic generates documentation that empowers you to grasp the plugin's features and their effective usage. But there's more – we've added a slick select drop-down feature. This nifty addition enables you to switch effortlessly between documentation for different releases. Whether diving into the latest version or revisiting an older one, finding the info you need is now seamless.

![Documentation Select Option](/blog/2023-08-25-thenewselectoption.png)

## All Aboard the Grails 6 Train

Web development is a swiftly moving train, and the Grails Spring Security Core Plugin is on board. We've given it a makeover for seamless integration with Grails 6. This empowers you to tap into the coolest Grails framework features while enjoying the robust security provided by the plugin.

## Getting Cozy with Dependency Updates

Dependencies are the foundation of your app, and keeping them up-to-date is crucial. In Grails Spring Security Core Plugin 6.0.0, we've rolled out updates to key dependencies:

- **Spring Security 5.8.6:** Latest Spring Security version for cutting-edge security features.
- **Hibernate 5:** Upgraded to version 8, embracing enhancements in Hibernate technology.
- **Spring Security Core 5.3.0:** Now at version 5.3.0, aligning with latest developments in the Spring Security ecosystem.
- **Grails Gradle Plugin 6:** Elevating your build and development experience with Grails Gradle Plugin v6.
- **Webdriver Binaries 3.2:** For web testing enthusiasts, Webdriver Binaries plugin updated to v3.2, enhancing testing capabilities.
- **Spring Dependency Management 1.1.3:** Details matter – Spring Dependency Management plugin upgraded to v1.1.3, simplifying project dependency management.

## Embrace the Future

Grails Spring Security Core Plugin 6.0.0 release goes beyond security enhancement; it's a stride towards improved compatibility and integration. The transition from Grails scripts to Grails ApplicationCommand underscores the plugin's commitment to delivering a unified and consistent experience in the realm of Grails 6. As you embark on secure, robust web app development, the upgraded plugin is your ally, aligning projects with the evolving landscape of modern web development. Upgrade today for a new era of security and compatibility in your Grails applications!

## How to Upgrade

To use the

 latest Grails Spring Security Core plugin, simply edit your `build.gradle` file and change the dependency from version 5.3.0 to version 6.0.0:

From:

```groovy
implementation 'org.grails.plugins:spring-security-core:5.3.0'
```

To:

```groovy
implementation 'org.grails.plugins:spring-security-core:6.0.0'
```

Updating your project's dependencies is all it takes. Your application is now upgraded to Grails Spring Security Core Plugin 6.0.0, ready to take advantage of enhanced security, smoother documentation navigation, and Grails 6 compatibility.

## Glimpse into the Future

But wait, there's more! As you enjoy these enhancements in Grails Spring Security Core Plugin 6.0.0, we want to give you a sneak peek into the exciting future. Our team is actively working on upgrading the Grails Spring Security REST plugin, bringing you even more comprehensive security features and seamless integration.

And that's not all. Our vision extends further down the road. We're planning to support the next major release, Spring Security 6, with a focus on addressing bug fixes and introducing further improvements. This dedication to staying ahead of the curve ensures that your applications will continue to be fortified with the latest security advancements and enhancements.

So, as you relish the benefits of Grails Spring Security Core Plugin 6.0.0, rest assured that the journey ahead is just as thrilling. We're committed to providing you with the tools you need to build exceptional, secure, and future-proof web applications. Stay tuned for more updates as we continue this innovative journey together!
