---
title: Grails and the Recent Spring Framework RCE
date: March 31, 2022
description: Grails Framework information regarding CVE-2022-22965
author: Jason Schindler
image: grails-blog-index-5.png
---

# [%title]

[%author]

[%date]

**[%description]**

## Background

The Spring Framework Team [recently announced](https://spring.io/blog/2022/03/31/spring-framework-rce-early-announcement) a remote code execution vulnerability, published as [CVE-2022-22965](https://tanzu.vmware.com/security/cve-2022-22965), discovered within the Spring Framework.  At this time, there is a known exploit that impacts applications that:

+ Use JDK 9 or higher
+ Run on the Apache Tomcat Servlet container
+ Are packaged as a WAR, and
+ Include the `spring-webmvc` or `spring-webflux` dependency

More up-to-date information can be found on the [Spring Blog announcement](https://spring.io/blog/2022/03/31/spring-framework-rce-early-announcement).

## Are Grails Applications Vulnerable?

Because Grails applications are built on top of Spring and Spring Boot, the Grails team has taken this vulnerability very seriously. Our investigations have yielded no evidence that Grails 4.x or 5.x applications are vulnerable to this attack. The Grails framework has its own data-binding logic, which includes checks to validate that a given property a) is in a list of properties that may be bound to, and b) exists within the target metaClass. All other property candidates are ignored.

The known exploit is one mechanism that can be used for this vulnerability. We will continue to monitor this situation and alert the Grails community of any vulnerabilities discovered, along with mitigation steps.

## Next Steps

Although at this time, we have no reason to believe that Grails applications are vulnerable, as a precaution, we have released [Grails 5.1.6](https://github.com/apache/grails-core/releases/tag/v5.1.6).  This Grails Framework release updates our Spring dependency to 5.3.18, which includes the upstream patch from the Spring Framework Team.
