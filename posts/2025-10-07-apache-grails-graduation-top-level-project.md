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

Apache Grails is a powerful Groovy-based web application framework for the Java Virtual Machine (JVM) built on top of Spring Boot. It enables rapid application development through convention-over-configuration and Don't Repeat Yourself (DRY) principles, making it ideal for productively building full-stack applications with simplicity. Similar to Ruby on Rails, Grails has a nearly 20-year history of evolution and refinement, built on Java Enterprise foundations like Spring Framework, Jakarta EE, and Hibernate.

"Becoming an ASF Top-Level Project signals the beginning of a new chapter for Apache Grails," said James Fredley, Apache Grails PMC Chair. "With ASF’s support and a thriving contributor community, we look forward to broadening adoption and advancing the project’s capabilities."

### Why use Grails?
- Rapid application development with high developer productivity
- Full-stack web framework with everything included
- DRY & Convention-Over-Configuration: Less boilerplate, sensible defaults
- Gentle learning curve with Groovy for productivity
- "Framework of frameworks" built on Spring Boot, Spring Framework, Jakarta EE, and Hibernate for enterprise foundations

### History and Migration Journey
Work on the Grails framework began in July 2005, with the 0.1 release on March 29, 2006, and the 1.0 release announced on February 18, 2008. Over the years, ownership transitioned from G2One (2005-2008) to SpringSource (2008-2015), Object Computing (2015-2021), and the Grails Foundation/Unity Foundation (2021-2025), before joining the ASF in 2025. For much of its history, Grails was primarily led by single organizations.

The migration to the ASF was an 18-month process starting in late Spring 2024. Motivations included shifting from single-organization dependency to a volunteer-driven model for sustainability, fostering community growth and revitalization with new energy from volunteers, aligning with open-source best practices like the Apache Way (consensus and transparency), enhancing governance through a Project Management Committee (PMC), mailing lists, and voting, and promoting vendor neutrality to encourage broader collaboration.

Key steps involved forming a volunteer team, assessing project readiness, submitting an incubation proposal, modernizing the codebase (including mono-repo merge, build system updates, dependency upgrades, and Maven coordinates), and issuing releases under the ASF. Releases included Milestone 4 (June 2025, the first ASF release), Milestone 5 (July 2025), Release Candidate 1 (August 2025), Release Candidate 2 (September 2025), with the 7.0.0 General Availability expected in early October 2025.

The project passed the ASF board vote on September 24, 2025, officially graduating to TLP status.

### Recent Updates and Contributions
As part of the migration and recent development, Grails 7.0.0-RC2 (released September 2025) included key updates such as dependency upgrades (e.g., Spring Boot 3.5.5, Spring Framework 6.2.10, Asset Pipeline 5.0.16), refactoring for Micronaut support via an optional plugin, bug fixes for Geb and Forge issues, and maintenance improvements like code style enforcement and CI enhancements. Full changelog available [here](https://github.com/apache/grails-core/compare/v7.0.0-RC1...v7.0.0-RC2).

Alongside the core release, companion projects were updated: Grails Spring Security 7.0.0-RC2, Grails Quartz 4.0.0-RC2, Grails Redis 5.0.0-RC2, Grails GitHub Actions 1.0.0, and Grails Gradle Publish 0.0.1.

We extend our thanks to all contributors who made these advancements possible. Special mentions go to recent contributors across various Grails repositories. For a combined list of commits, see [this GitHub search](https://github.com/search?q=repo%3Aapache%2Fgrails-core+repo%3Aapache%2Fgrails-spring-security+repo%3Aapache%2Fgrails-static-website+repo%3Aapache%2Fgrails-forge-ui+repo%3Aapache%2Fgrails-quartz+repo%3Aapache%2Fincubator-grails-gradle-publish+repo%3Agrails-redis+repo%3Agrails-github-actions+is%3Apublic&type=commits&s=committer-date&o=desc).

Takeaways from the migration process include establishing early relationships with the ASF Infrastructure team via Slack and JIRA, leveraging incubation mentors for success, planning the end state for codebase and repositories upfront to avoid duplicated efforts, addressing challenges in migrating automated Gradle and GitHub Actions workflows (now serving as a model for Gradle-based projects), and consolidating repositories from an initial 100 down to 18 (with 9 active).

### About The Apache Software Foundation (ASF)
The Apache Software Foundation (ASF) is the global home for open source software, powering some of the world’s most ubiquitous software projects, including Apache Airflow, Apache Camel, Apache Cassandra, Apache Groovy, Apache HTTP Server, and Apache Kafka. Established in 1999, The ASF is at the forefront of open source innovation, setting industry standards to advance software for the public good. Learn more at https://apache.org.

The ASF’s annual Community Over Code event is where open source technologists convene to share best practices and use cases, forge critical relationships, and learn about advancements in their field. https://communityovercode.org/

### Resources
- Website: https://grails.apache.org/
- Documentation: https://docs.grails.org/latest/
- GitHub: https://github.com/apache/grails-core
- Developer Mailing List: https://lists.apache.org/list.html?dev@grails.apache.org
- Users Mailing List: https://lists.apache.org/list.html?users@grails.apache.org
- Slack: https://grails.slack.com/ (Join: https://slack.grails.org/)

We invite the community to join us in this new chapter—contribute, provide feedback, and help shape the future of Apache Grails!

For media inquiries, contact press@apache.org.