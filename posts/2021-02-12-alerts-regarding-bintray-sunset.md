---
title: Important Information Regarding Grails and Bintray
date:  February 12, 2021
description: The Grails team shares preliminary plans for existing artifacts and plugin publication in response to JFrog's plans to sunset Bintray on May 1, 2021. 
author: Jason Schindler
image: grails-blog-index-5.png

---

# [%title]

[%author]

[%date] 

Tags: #bintray #plugins


As many of you know, last week JFrog [announced](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/) that they are sunsetting JCenter and Bintray. On March 31st, JCenter will no longer accept new package submissions, but it will continue to serve artifacts until February 1st of 2022. On May 1, 2021, Bintray will be shut down. After May 1st, artifacts hosted by Bintray and the Bintray API will not be available.

This has a large impact on the Grails community, as we have been using Bintray for quite some time to host a number of our artifacts, as well as the community plugins under the `org.grails.plugins` group ID. Additionally, the [Grails Plugins Portal](/plugins.html) utilizes the Bintray API to retrieve needed metadata to list and search plugins.

We have a plan, but it is going to require some lifting.

## Moving Existing Artifacts Out of Bintray

Our primary concern is making sure existing artifacts can still be resolved after Bintray is shut down on May 1st.  In order to achieve this, we will be moving existing artifacts including community plugins from Bintray to the Grails [Artifactory instance](https://repo.grails.org/). JFrog is not shutting down Artifactory and will continue to support our instance. This should allow existing artifacts to be served from Artifactory once Bintray is shut down, and current Grails applications should continue to have access to the artifacts and plugins that they need that are in our control.

## Updating Grails Plugins Portal

As mentioned earlier, the Grails Plugins Portal is currently backed by the Bintray API. On May 1st, that API will be shut down with the rest of Bintray, so we need an alternative. We will create a public repository on GitHub that will host the metadata needed by the Grails Plugins Portal and can be managed by the community. Our intent is that this repository will periodically scan for updated release information and modify the metadata that the Grails Plugins Portal requires. This is a work in progress. We will announce more details about this process in the near future.

## Publishing Community Plugins

Unfortunately, the loss of Bintray will require some extra effort from community plugin authors when you publish your next release.

First, you will no longer be able to publish to Bintray, and you will no longer be able to use the `org.grails.plugins` group. Community access to the `org.grails.plugins` group was a benefit of using Bintray that we cannot easily recreate. We could grant community plugin authors access to the Grails Artifactory instance, but doing so would create substantial user management overhead. It would also more tightly couple Grails to our JFrog Artifactory instance.  

We feel a better solution is to maintain the plugin list publicly in a GitHub repository, and for plugin authors to publish their artifacts to another repository. Fortunately, community plugin authors have some options on where to publish their artifacts. We strongly suggest that folks publish to Maven Central. There is some extra overhead in publishing there, but we feel it is the safest bet for future availability of our artifacts.  

The “grails-plugin-publish” Gradle plugin will no longer be able to publish Grails community plugins, as it supports publishing to Bintray with the [gradle-bintray-plugin](https://github.com/bintray/gradle-bintray-plugin). To publish to Maven Central, we recommend that plugin authors switch to the [Gradle Maven Publish](https://docs.gradle.org/current/userguide/publishing_maven.html) plugin instead. We will be exercising this process ourselves in the coming days and will provide more details and guidance in the very near future.

## Grails 5 Delay

Over the next few weeks, we will be focusing our efforts on this migration, and that is likely to cause a delay in the Grails 5 release. We want to wrap this up as quickly as possible so that we can get back to work on Grails 5!

## Moving Forward

Thank you to all of you for your continued participation in the Grails community. We know that some of the changes resulting from this will be frustrating, and we ask for your patience as we work through this migration.
