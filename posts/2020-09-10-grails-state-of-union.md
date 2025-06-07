title: Grails® State of the Union
date: September 10, 2020   
description: Grails® Product Development Lead, Puneet Behl, provides an update on the current state of the Grails framework and what you can expect going forward.
author: Puneet Behl
image: 2020-09-10.jpg   
---

# [%title]

[%author]

[%date] 

Tags: #micronaut #groovy3

As we approach the fourth quarter of 2020, I wanted to take a few minutes to update everyone in the community on the updates and enhancements coming to the Grails<sup>&reg;</sup> framework. 

In the last few months, the [2GM (Groovy, Grails framework, and Micronaut) team](https://objectcomputing.com/products/2gm-team) has put a lot of work into the upcoming Grails 4.1 release. We’re excited to share that this release will include support for Apache Groovy 3! Groovy 3 offers a number of [enhancements](https://groovy-lang.org/releasenotes/groovy-3.0.html) to the language including method references and a new, more flexible parser. Additionally, Grails 4.1 will support [Micronaut 2.0](https://micronaut.io), Gradle 6.5, and Spring 2.3. We’ve also added improvements to the Grails profiles to support the exclusion of transitive dependencies.

Apart from this, we've had some minor releases featuring bug fixes and improvements, which include (but are not limited to) the following: 

- Loading configurations from plugins
- Significant improvements to the performance of compileGroovyPages and compileJsonView Gradle tasks
- GORM for MongoDB to support MongoDB Sync driver 4.0.3
- GORM for Neo4j to use the latest Neo4j driver version 4.0.1
- Update to the latest Elasticsearch in the Grails Elasticsearch plugin
- Injecting GORM Data Services

## Developer Productivity FTW!

The Grails framework remains an excellent choice for rapidly building web applications. The framework utilizes the power of the Groovy language and offers lots of Groovy DSLs. If you prefer the dynamic nature of Groovy, the Grails framework is an excellent option for you.

Other benefits include the following:

- The Grails framework gives you the power to use any Spring library; from Grails 3 forward, the framework is built on top of Spring Boot
- In Grails 4, Micronaut is the parent application, which allows you to use many Micronaut features, including the Micronaut HTTP client, Kafka client, and others in your Grails apps. Due to Micronaut’s avoidance of reflection, you also benefit from a reduced memory footprint and faster start times!
- The Grails framework provides a variety of compelling and powerful features, such as GSP, JSON Views, and more.
- The Grails framework allows you to quickly and easily build an application because it takes advantage of the convention over configuration concept, wherein you:
    - Do not need to use a lot of annotations, such as Controller, Service, etc.
    - Do not need to specify every URL mapping
    - Benefit from automatic code generation
    - Can build REST APIs for domains

While the parent context in Grails 4 is Micronaut, it is worth noting that not all Micronaut features are supported in the Grails framework at this time.  These include:

- Anything that has to do with the server, such as tracing, Micronaut controllers, etc.
- GraalVM native image support
- Micronaut serverless, AWS Lambda, Functions, GRPC, etc.

As the Grails framework continues to evolve, so does the way that we are using Grails apps here at Object Computing. As I mentioned above, Grails 4 applications can now use many of Micronaut’s features, including the [HTTP Client](https://docs.micronaut.io/latest/guide/index.html#httpClient). If your application is using the GORM REST Client, we suggest migrating over to the Micronaut HTTP Client. 

Also, because Grails 4 now uses Micronaut as the parent context, you can develop integrations for your applications using a [Micronaut Configuration](https://docs.micronaut.io/latest/guide/index.html#configurations) where you may have written a Grails plugin in the past. Doing so will enable your Grails application to make use of the new functionality and opens up the opportunity to use the configuration with other non-Grails applications that have been built on Micronaut!

## Coming Soon

In addition to the improvements above, we are planning on a number of additional enhancements including:

- Native support for web-sockets
- Transaction support in GORM for MongoDB
- Better support for Kafka, such as improvements in the CLI for creating Kafka listeners through the plugin
- Ability to use GSP in Micronaut and Spring Boot applications by making GSP more modular and independent to the Grails framework
- More Micronaut features, including reduced memory footprints and other benefits 
- Support for the latest Groovy and JDK.

## Thank You
The continuing evolution and improvements to the Grails framework wouldn’t be possible without the work and support provided by the vibrant [Grails community](/community.html). If you are new to the Grails framework or are considering it for your next project, please stop by our [Slack community](https://slack.grails.org) and say "hello." We can’t wait to hear about the great applications you're building with Groovy and the Grails framework! 
