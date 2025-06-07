---
title: Introducing the New Grails Forge - A Lightning-Fast, Micronaut-Inspired CLI for Grails Application Generation
date: August 3, 2023
description: The Grails Foundation™ is thrilled to unveil the next level of Grails application generation - the New Grails Forge!
author: Puneet Behl
image: grails-blog-index-3.png
---

# [%title]

[%author]

[%date]

Dear Grails Developers,

The Grails Foundation™ is thrilled to unveil the next level of Grails application generation - the New Grails Forge! Building upon the foundation of the previous Grails Forge, we have introduced a brand new implementation of the Grails Forge website, and the Grails Command Line Interface (CLI).

## Multiple Application Types for Your Needs

The New Grails Forge also introduces Grails Profiles through application-types, supporting various application types to meet your specific needs. Currently, we offer support for Web, REST API, Web Plugin, and Plugin. Stay tuned as we plan to add support for additional application types, including REST API Plugin, React, and Angular.

## The New Grails Forge CLI: Speed, Flexibility, and Efficiency

Before we delve into the Grails Forge UI, let's first highlight the power of the New Grails Forge CLI. Inspired by the efficiency and flexibility of the Micronaut framework's CLI, the New Grails Forge CLI offers a lightning-fast and easy-to-maintain approach to creating and managing Grails projects.

With the New Grails Forge CLI, you can create Grails applications offline or in areas with limited connectivity, making it a perfect choice for developers on the go. Powered by the Picocli library, the CLI delivers exceptional performance and robustness, ensuring a smooth and seamless command-line experience.

## Getting Started with the Grails Forge UI

Getting started with the Grails Forge UI is as simple as 1, 2, 3:

1. Visit the Website: Head over to the official Grails Forge UI website at [https://start.grails.org](https://start.grails.org)

2. Explore and Customize: Take a tour of the website and explore its features. Customize your project settings to match your development requirements.

3. Generate Your Project: Once you're satisfied with your choices, hit the "Generate Project" button. Your Grails project will be created instantly, and you can download the project files to get started immediately.

## Global CLI Compatibility: The Best of Both Worlds

While many commands transition smoothly to Gradle tasks, we understand that some actions require the familiarity of the globally-installed Grails CLI. Fear not, as the New Grails Forge thoughtfully addresses this concern. If you have the Grails CLI installed on your system, you can still use it within your project folder for commands involving file manipulations. This harmonious integration ensures a seamless transition for existing projects while taking advantage of the new features of the New Grails Forge.

## Extending Functionality: A World of Possibilities

The New Grails Forge empowers you to extend the functionality of your projects effortlessly. Although some plugin-specific commands may require slight adjustments during the migration, we've got you covered with our comprehensive Grails Documentation, providing valuable insights into creating custom commands. This newfound capability allows you to tailor commands to your unique needs, automate tasks, integrate with third-party tools, and streamline repetitive processes.

## Lightning-Fast and Offline Ready

With the new CLI, you can create Grails applications even offline or in areas with limited connectivity, thanks to its lightning-fast performance. Say goodbye to frustrating delays and embrace a seamless and efficient application generation process.

## Powered by Picocli and Rocker Template Engine

To achieve such speed and efficiency, we harnessed the power of the Picocli library to build a robust and flexible command-line application. This integration ensures that you have a user-friendly and highly maintainable CLI at your fingertips.

Moreover, we adopted the Rocker template engine, which produces statically typed plain Java objects, guaranteeing precision and performance in every command you execute.

## Saying Farewell to the Grails CLI Wrapper

With Grails 6, we're embracing a new era where the Grails CLI wrapper will no longer be included in new projects. Instead, we encourage you to leverage the Gradle build system for greater flexibility and a more cohesive development environment. Common commands, such as "./grailsw run-app," can now be executed directly using the straightforward "./gradlew bootRun."

## Enhanced Plugin Compatibility with Grails ApplicationCommand

While many commands transition seamlessly to Gradle tasks, we acknowledge that certain commands, supported via plugins, may require special attention during migration. To address this, we introduced Grails ApplicationCommand tasks, enabling compatibility with plugin-specific commands. Refer to our comprehensive Grails Documentation for valuable insights on creating custom commands and making the most of this new capability.

## Turbocharge Your Grails Development: Swift Installation of the New Grails Forge

Installing the New Grails Forge is a breeze! Follow these step-by-step instructions based on your preferred method:

### Option 1: Installing with SDKMan

The easiest way to install the New Grails CLI is with SDKMan. Simply run the following command in your terminal:

```shell
sdk install grails 6.0.0
```

### Option 2: Download & Install Binary

1. Download the latest binary from the following link, based on your Operating System: [New Grails CLI Binary](https://github.com/apache/grails-forge/releases)

2. Extract the downloaded zip file to an appropriate directory on your system.

3. Create an environment variable named "GRAILS_HOME" and set its value to the installation directory, for example:
   - On Windows: `set GRAILS_HOME=c:/grails`
   - On macOS/Linux: `export GRAILS_HOME=/usr/local/grails`

4. Update the PATH environment variable to include the Grails binary directory. For example:
   - On Windows: `set PATH=%PATH%;%GRAILS_HOME%\bin`
   - On macOS/Linux: `export PATH=$PATH:$GRAILS_HOME/bin`

### Option 3: Use SDKMan to Build & Install From Source

1. Clone the repository with the following command:

```shell
git clone https://github.com/apache/grails-forge.git
```

2. Build the distribution using the following command:

```shell
cd grails-forge
./gradlew :grails-cli:assembleDist
```

3. Copy and extract the archive from `grails-forge/grails-cli/build/distributions/grails-cli.*.zip` to your user home directory.

4. Use SDKMan to install the development version using the following commands:

```shell
sdk install grails dev ~/grails-cli/
sdk use grails dev
```

## Explore the Source Code

Curious about the inner workings of the New Grails Forge? Delve into the source code on GitHub: [Grails Forge Core Source Code](https://github.com/apache/grails-forge)

## Preview Feature: Explore Your Grails Application Source Files

We are excited to introduce the Preview Feature on [https://start.grails.org](https://start.grails.org)! This powerful addition allows you to take a sneak peek into your Grails application source files before actually generating the project. With the Preview Feature, you can explore the selected options and features to see exactly what will be generated, giving you full control and confidence in your project setup.

How does it work? It's simple:

1. Customize Your Options: Start by selecting the application type and setting your project preferences, just like you would with the regular project generation process.

2. Click "Preview": Instead of hitting the "Generate Project" button right away, click on "Preview" to see the magic happen.

3. Visualize Your Project: The Preview Feature will instantly generate a visualization of your Grails application source files, showcasing the directory structure and all the code files based on your chosen options.

4. Navigate and Inspect: Dive into the source files, navigate through the directories, and inspect the actual code snippets that will be included in your project.

5. Refine and Adjust: If you spot anything you'd like to modify, simply go back, adjust your options, and preview again until you're fully satisfied with your project setup.

This Preview Feature is a game-changer, as it allows you to make informed decisions and tailor your Grails application precisely to your needs. Gone are the days of uncertainty - with the Preview Feature, you can confidently create and manage your Grails projects with ease.

So, what are you waiting for? Try out the Preview Feature on [https://start.grails.org](https://start.grails.org) and unleash the full potential of the New Grails Forge CLI and UI.

## Ready to Forge Ahead

With the New Grails Forge installed on your system, you're all set to embark on a new era of Grails application generation. Explore the lightning-fast performance, enhanced flexibility, and seamless compatibility with Gradle and plugins. Together, let's redefine Grails development and build remarkable applications for a rapidly evolving world.

## Introducing the Grails Forge UI: A User-Friendly Gateway to Project Generation

Now, let's move on to the star of the show - the React-based Grails Forge UI! This cutting-edge website serves as an interactive and user-friendly alternative to project generation, eliminating the need to install the CLI locally.

## Seamless Replacement for the Old Grails Forge Website

With the Grails Forge UI, we are embracing the future of project creation and bidding farewell to the old Grails Forge website [https://start.grails.org](https://start.grails.org). This exciting transition ensures a more advanced and efficient platform for generating Grails projects.

## A Leap Towards the Future

The New Grails Forge marks a significant leap towards the future of Grails application generation. With its Micronaut-inspired design, lightning-fast performance, and versatile application-types, you can confidently embark on your development journey, knowing you have a powerful tool at your disposal.

## Join the New Grails Forge Revolution

We invite you to embrace the New Grails Forge and be part of this revolutionary movement in Grails development. As we continue to refine and improve, your feedback and insights are invaluable. Together, let's redefine Grails application generation and shape a future of remarkable applications powered by the New Grails Forge.

Stay connected with us on the official Grails website and community forums for the latest updates, resources, and discussions on harnessing the full potential of this cutting-edge CLI. Let's embark on this journey together, unlocking the limitless possibilities of the New Grails Forge.

Happy coding!

The Grails Team

