---
title: Updated Grails Wrapper for Grails 3 and 4 projects
date: June 10, 2021
description: Instructions to update grails-wrapper.jar for recent updates to Grails Framework Repository
author: Jason Schindler
image: grails-blog-index-1.png
---

# [%title]

[%author]

[%date]

Recently, the Grails Framework Artifactory instance was updated. As a result, the Grails wrapper now fails under certain conditions because the URL it uses to retrieve information is no longer supported.

We are preparing a patch release of Grails framework 4 and Grails framework 3 to make new applications work with the change, but for existing Grails framework 3 and Grails framework 4 applications, the `grailsw` command will fail if the necessary assets haven't already been cached on the machine.

The error received is:

```
You must be connected to the internet the first time you use the Grails wrapper
org.xml.sax.SAXParseException; lineNumber: 6; columnNumber: 3; The element type "hr" must be terminated by the matching end-tag "</hr>".

```

## Updating Existing Projects

To fix this, replace the `grails-wrapper.jar` file in the base of your existing project with one of the updated wrappers below:

+ [Updated grails-wrapper.jar for Grails 3 Projects](/files/wrapper-issue7/grails3/grails-wrapper.jar) [(MD5)](/files/wrapper-issue7/grails3/grails-wrapper.jar.md5)
+ [Updated grails-wrapper.jar for Grails 4 Projects](/files/wrapper-issue7/grails4/grails-wrapper.jar) [(MD5)](/files/wrapper-issue7/grails4/grails-wrapper.jar.md5)

Running the `grailsw` command from within the project should now work as expected.

## Changing Repository URL

In addition to fixing the URL, we have also added the ability to change the base URL that the wrapper uses.

There are two possible mechanisms for doing this:

+ Setting an environment variable named `GRAILS_CORE_ARTIFACTORY_BASE_URL`
+ Setting the `grails.core.artifactory.baseUrl` system property

The default value is `https://repo.grails.org/grails/core`, and we do not expect that to change.

## Wrapping Up

We apologize for any inconvenience you may have experienced by this recent change. We will post a more detailed post-mortem on these events in the next few days. Please continue to report [any issues](https://github.com/apache/grails-core/issues) you encounter.
