---
title: Apache Grails Graduates to Top-Level Project at The Apache Software Foundation
date: October 7, 2025
description: The Grails Community is pleased to announce the graduation to Top-Level Project at The Apache Software Foundation.
author: James Fredley
image: grails-blog-index-1.png
---

# [%title]

[%author]

[%date]

The Apache Grails team is excited to announce that Apache Grails has officially graduated from incubation under the Apache Groovy project to become a Top-Level Project (TLP) at The Apache Software Foundation (ASF). This achievement reflects the dedication of our community and underscores the strength of the ASF's open source ecosystems.

[Apache Software Foundation Press Release](https://news.apache.org/foundation/entry/the-apache-software-foundation-announces-new-top-level-projects-2)

Apache Grails is a powerful Apache Groovy-based web application framework for the Java Virtual Machine (JVM) built on top of Spring Boot. It enables rapid application development through convention-over-configuration and Don't Repeat Yourself (DRY) principles, making it ideal for productively building full-stack applications with simplicity. Similar to Ruby on Rails, Grails has a nearly 20-year history of evolution and refinement, built on Java Enterprise foundations like Spring Framework, Jakarta EE, and Hibernate.

"Becoming an ASF Top-Level Project signals the beginning of a new chapter for Apache Grails," said James Fredley, Apache Grails PMC Chair. "With ASF’s support and a thriving contributor community, we look forward to broadening adoption and advancing the project’s capabilities."

We invite the community to join us in this new chapter—contribute, provide feedback, and help shape the future of Apache Grails!

## Why use Grails?
- Rapid application development with high developer productivity
- Full-stack web framework with everything included
- DRY & Convention-Over-Configuration: Less boilerplate, sensible defaults
- Gentle learning curve with Groovy for productivity
- "Framework of frameworks" built on Spring Boot, Spring Framework, Jakarta EE, and Hibernate for enterprise foundations

## History and Migration Journey
Work on the Grails framework began in July 2005, with the 0.1 release on March 29, 2006, and the 1.0 release announced on February 18, 2008. Over the years, ownership transitioned from G2One (2005-2008) to SpringSource (2008-2015), Object Computing (2015-2021), and the Grails Foundation/Unity Foundation (2021-2025), before joining the ASF in 2025. For much of its history, Grails was primarily led by single organizations.

The migration to the ASF was an 18-month process starting in late Spring 2024 alongside Grails 7 development. Motivations included shifting from single-organization dependency to a volunteer-driven model for sustainability, fostering community growth and revitalization with new energy from volunteers, aligning with open-source best practices like the Apache Way (consensus and transparency), enhancing governance through a Project Management Committee (PMC), mailing lists, and voting, and promoting vendor neutrality to encourage broader collaboration.  The key steps to becoming a TLP involved forming a volunteer team, assessing project readiness, submitting an incubation proposal, ensuring the code base met ASF policy, and issuing releases under the ASF.

The development team decided to use a mono-repo approach for the Grails code base to help accelerate compliance with ASF policy. This involved merging 24+ separate GitHub repositories (that also were the result of other combination efforts) into a single code base. Each of these merges required integrating separate build systems & release processes over 100s of commits. Builds for a release went from 3+ weeks to approximately 30 minutes after this effort. The grails-core mono-repo now produces over 325 published jar files across 109 gradle projects with local build times taking anywhere between 2 and 10 minutes (depending on caching & hardware). 

With the ability to release, the project turned to meeting ASF policy. The initial goal: changing our maven coordinates. All of the Grails maven coordinates had to switch to a base group of `org.apache.grails`. This required agreement across the community on package names since conflicts arose due to overlapping coordinates. Significant feedback was given by the community to produce more consistent and predictable coordinate name. As part of this change, a Gradle Plugin (`grails-publish`) was written to ease publishing our packages. This plugin is now available for any project to use.

Next, ASF security policy requires verifiable builds for projects that use GitHub actions. This required changing the Grails build to be reproducible, upstream changes to several of our dependencies (including contributions to Apache Groovy), and the ability to verify any Grails build independently. A major benefit to the community was that Grails applications can now be made reproducible as well.  

The last major goal was licensing compliance. First, every source file had to changed or reviewed for its license headers.  Second, 327 separate artifacts had to be reviewed to ensure we were not distributing an incompatible license. Like the mono repo merge, publishing optimizations, and verifiable builds initiatives, the dev team automated license review by adopting Software Bill of Materials for any published jar file. This ensures we stay compliant and reduces future effort to review licenses.

The final major initiative was automating our release process so that we could release more frequently. This involved developing new GitHub Actions that assisted in every step of the release process. The resulting work is the GitHub Release workflow that will enable future committers to help release Grails without specialized knowledge of our build. 

Overall, the changes to Grails can be characterized as a modernization of our build system, licensing compliance, & release process that will ensure Grails survives. The changes will benefit every end Grails application and ensure that Grails is enterprise ready. Over 2,000 commits were made to the grails-core mono-repo alone. 1,247,686 lines were removed from the code base with over 2,631,372 lines changing; many of these changes were incremental to ensure a stable build as significant change was made to the code base.

Releases for grails-core included Milestone 4 (June 2025, the first ASF release), Milestone 5 (July 2025), Release Candidate 1 (August 2025), Release Candidate 2 (September 2025), with the 7.0.0 General Availability expected in October 2025. There are also numerous other releases across the Grails Spring Security plugins, Redis plugin, Quartz plugin, and other supporting projects that were moved to the ASF.

The project passed the ASF board vote on September 24, 2025, officially graduating to TLP status.

We extend our thanks to all contributors who made these advancements possible. Special mentions go to recent contributors across various Grails repositories. 

Takeaways from the migration process include establishing early relationships with the ASF Infrastructure team via Slack and JIRA, leveraging incubation mentors for success, planning the end state for codebase and repositories upfront to avoid duplicated efforts, addressing challenges in migrating automated Gradle and GitHub Actions workflows (now serving as a model for Gradle-based projects at the ASF), working with the community to communicate upcoming changes, and consolidating repositories from an initial 100 down to 18 (with 9 active).

## Thank you!
A huge thank you to our amazing community for supporting the Grails Framework over the past 20 years! We’re excited for the future and grateful for the opportunity to continue innovating and pushing Grails forward together.

## Contributors
We would like to extend our heartfelt thanks to all the contributors who made Grails 7 possible. \
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
* [zyro](https://github.com/zyro23)

Recent Contributors by Project:

* [grails-core](https://github.com/apache/grails-core/graphs/contributors[%6MonthsBackForGitHub])
* [grails-spring-security](https://github.com/apache/grails-spring-security/graphs/contributors[%6MonthsBackForGitHub])
* [grails-static-website](https://github.com/apache/grails-static-website/graphs/contributors[%6MonthsBackForGitHub])
* [grails-forge-ui](https://github.com/apache/grails-forge-ui/graphs/contributors[%6MonthsBackForGitHub])
* [grails-quartz](https://github.com/apache/grails-quartz/graphs/contributors[%6MonthsBackForGitHub])
* [grails-gradle-publish](https://github.com/apache/incubator-grails-gradle-publish/graphs/contributors[%6MonthsBackForGitHub])
* [grails-redis](https://github.com/apache/grails-redis/graphs/contributors[%6MonthsBackForGitHub])
* [grails-github-actions](https://github.com/apache/grails-github-actions/graphs/contributors[%6MonthsBackForGitHub])

[Combined Commit List](https://github.com/search?q=repo%3Aapache%2Fgrails-core+repo%3Aapache%2Fgrails-spring-security+repo%3Aapache%2Fgrails-static-website+repo%3Aapache%2Fgrails-forge-ui+repo%3Aapache%2Fgrails-quartz+repo%3Aapache%2Fincubator-grails-gradle-publish+repo%3Agrails-redis+repo%3Agrails-github-actions+is%3Apublic&type=commits&s=committer-date&o=desc)

Their dedication and hard work have significantly contributed to the release of Grails 7.

### Resources
- [Website](https://grails.apache.org/)
- [Documentation](https://grails.apache.org/docs/latest/)
- [GitHub](https://github.com/apache/grails-core)
- [Developer Mailing List](https://lists.apache.org/list.html?dev@grails.apache.org)
- [Users Mailing List](https://lists.apache.org/list.html?users@grails.apache.org)
- [Slack](https://grails.slack.com/) (Join: [https://slack.grails.org/](https://slack.grails.org/))

## About The Apache Software Foundation (ASF)
The Apache Software Foundation (ASF) is the global home for open source software, powering some of the world’s most ubiquitous software projects, including Apache Airflow, Apache Camel, Apache Cassandra, Apache Groovy, Apache HTTP Server, and Apache Kafka. Established in 1999, The ASF is at the forefront of open source innovation, setting industry standards to advance software for the public good. Learn more at [https://apache.org](https://apache.org).

For media inquiries, contact press@apache.org.
