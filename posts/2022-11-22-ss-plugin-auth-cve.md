---
title: Grails Spring Security Core Plugin Improper Privilege Management Vulnerability
date: November 22, 2022
description: Information regarding CVE-2022-41923
author: Matthew Moss
image: grails-blog-index-4.png
---

# [%title]

[%author]

[%date]

**[%description]**


## Overview

The Grails team has confirmed a security vulnerability found in the Grails Spring Security Core plugin, initially identified by Adrien Peter and Benjamin Sepe from [Synacktiv](https://www.synacktiv.com/), and investigated and reported by Arek Bazylewicz of [ID5](https://id5.io). This vulnerability has been assigned identifier [CVE-2022-41923](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2022-41923).

The vulnerability allows an attacker access to one endpoint (i.e. the targeted endpoint) using the authorization requirements of a different endpoint (i.e. the donor endpoint). In some Grails framework applications, access to the targeted endpoint will be granted based on meeting the authorization requirements of the donor endpoint, which can result in a privilege escalation attack.

**Update**: Synacktiv has made available their [security advisory (PDF)](https://www.synacktiv.com/sites/default/files/2023-03/Synacktiv-Grails-Spring-Security-CVE-2022-41923.pdf) on this vulnerability.

## Impacted Applications

Grails Spring Security Core plugin versions:
  * 1.x
  * 2.x
  * &gt;=3.0.0 &lt;3.3.2
  * &gt;=4.0.0 &lt;4.0.5
  * &gt;=5.0.0 &lt;5.1.1

We strongly suggest that all Grails framework applications using the Grails Spring Security Core plugin be updated to a patched release of the plugin.

## Protecting Your Applications

The following Grails Spring Security Core plugin versions have been patched for this vulnerability:
  * [3.3.2](https://github.com/apache/grails-spring-security/releases/tag/v3.3.2)
  * [4.0.5](https://github.com/apache/grails-spring-security/releases/tag/v4.0.5)
  * [5.1.1](https://github.com/apache/grails-spring-security/releases/tag/v5.1.1)

The best way to protect your Grails framework application is to upgrade to a patched release of this plugin.

If you are unable to upgrade to a patched version of the plugin, you can work around the issue with a small code and configuration change described in [this GitHub repository](https://github.com/grails/GSSC-CVE-2022-41923).  We have provided workaround examples for Grails framework 2 through 5 applications. A demonstration of the vulnerability will be provided in time, after users have an opportunity to patch their applications.

## Looking Forward

The Grails Foundation and the Grails development team take application security very seriously. We are continuing to research and monitor this vulnerability and will update this post with new information as it is discovered.

If you have questions about this vulnerability or need assistance on upgrades or workarounds, please see the [discussion on GitHub](https://github.com/apache/grails-spring-security/issues/844) or contact us at [security@apache.org](mailto:security@apache.org).
