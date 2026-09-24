---
version: 8.0.0
priorVersion: 8.0.0-RC1
title: Apache Grails [%version] - Release Announcement
date: October 14, 2026
description: The Apache Grails community is excited to announce the [%version] release of the Apache Grails Framework!
author: James Daugherty
image: grails-blog-index-3.png

---

<!-- TODO before publishing: set the real GA date (the site skips posts dated in the future), confirm the author line, and fill in the Release Schedule section. -->

# [%title]

[%author]

[%date]

[%description]

Grails 8 is the first major release planned and delivered entirely as an Apache Top-Level Project. Since the 8.0.x branch opened in November 2025, more than 3,500 commits across nearly 300 pull requests have landed through six milestones and a release candidate. The result moves Grails onto Java 21, Apache Groovy 5, Spring Boot 4.1 and Spring Framework 7, adds a first-class GORM implementation for Hibernate 7, brings GORM for Neo4j back as a published artifact, and reworks the plugin lifecycle, the command-line tooling, and the way GSP pages and tag libraries are compiled.

This post walks through what changed, grouped by area. The [What's New in Grails 8](https://grails.apache.org/docs/[%version]/guide/introduction.html#whatsNew) section of the guide covers the headline features, and the [Grails 8 upgrade guide](https://grails.apache.org/docs/[%version]/guide/upgrading.html#upgrading80x) documents every behavior change in detail.

Thousands of volunteer hours went into this release. Thank you to everyone who contributed code, reviews, documentation, issue reports, and testing.

## Why use Grails?
- Rapid application development with high developer productivity
- Full-stack web framework with everything included
- DRY & Convention-Over-Configuration: Less boilerplate, sensible defaults
- Gentle learning curve with Apache Groovy for productivity
- "Framework of frameworks" built on Apache Groovy, Spring Boot, Spring Framework, Jakarta EE, and Hibernate for enterprise foundations

## Download Source Code and Binary Distributions

[Apache Grails Downloads](/download.html)

## Grails 8 at a Glance

- **Modern platform**: Java 21, Gradle 9.7, Apache Groovy 5.1, Spock 2.4, Spring Boot 4.1, Spring Framework 7.0, Spring Security 7.1, Jackson 3, Tomcat 11, Jakarta Servlet 6.1
- **GORM for Hibernate 7**, alongside Hibernate 5 (still the default), with database migration support and a dedicated BOM
- **GORM for Neo4j is back**, rebuilt for Spring Boot 4 and published again
- **Plugin beans register before Spring Boot auto-configuration**, a new `beanRegistrar()` hook, and a compile-time `@GrailsBeans` DSL that produces real `@AutoConfiguration` classes
- **Tag libraries and GSP pages compile statically**: compiled tag resolution, application-wide GSP static compilation, and a one-line opt-in to compile every controller, service and tag library with `@GrailsCompileStatic`
- **Ahead-of-time support**: Spring AOT processing, GraalVM native image metadata, and JDK AOT cache training that cuts startup time by more than half
- **Command-line tooling moves to companion `-cli` artifacts**, keeping CLI dependencies out of your packaged application
- **Embedded MongoDB, MongoDB multi-document transactions, and Spring Data MongoDB interoperability**
- **Security hardening** throughout: deny-by-default data binding, compile-time GORM query safety checks, XML DOCTYPE rejection, canonicalized interceptor matching, and a published threat model
- **Undertow support returns**, alongside Tomcat and Jetty
- **Observability**: Micrometer spans for controllers, interceptors, data binding, and GSP rendering, with correct `uri` tags for Grails-mapped requests

## What's New in Grails 8

### Platform Baseline and Foundations

Grails 8 raises the build and runtime baseline to **Java 21** and requires **Gradle 9.7** or later; a Gradle wrapper carried over from a Grails 7 application does not work, because the Grails Gradle plugin relies on `groovyOptions.configurationScriptFile`, which only exists from Gradle 9.7 on. The standard BOM ships **Apache Groovy 5.1.x** and **Spock 2.4-groovy-5.0**.

The framework is built on **Spring Boot 4.1.x** and **Spring Framework 7.0.x**. That crosses the Spring Boot 4 boundary and brings the modular auto-configuration layout, Spring Framework 7 API removals, **Jackson 3** as the default JSON library, **Tomcat 11**, **Jakarta Servlet 6.1**, and Spring Boot 4.1 dependency management for Spring Security 7.1, Spring Data 2026.0 and Micrometer 1.17.

The **Spring Dependency Management plugin is gone**. The Grails Gradle plugin now adds `grails-bom` as a native Gradle `platform()` on every configuration, and a bundled `org.apache.grails.gradle.bom-property-overrides` plugin keeps the `gradle.properties` / `ext['slf4j.version']` override workflow working exactly as before. `grails { bom = null }` opts out entirely, and `enforcedPlatform("org.apache.grails:grails-bom:$grailsVersion")` restores "BOM always wins" semantics for anyone who needs them. `grails-bom` now inherits the versions Spring Boot already manages instead of re-pinning them, and the BOM family grows two members: `grails-hibernate7-bom` and `grails-neo4j-bom`.

Grails Micronaut is now maintained and released from the [apache/grails-micronaut](https://github.com/apache/grails-micronaut) repository against published Grails Core artifacts.

Finally, Grails 8 removes a large body of long-deprecated API: `grails.web.JSONBuilder`, the JSON/XML `EnumMarshaller` classes, the plugin filter family, the legacy events API and `grails-events-compat`, `Mixin`, `NamedCriteriaProxy`, `AetherGrapeEngine`, and more. The upgrade guide includes API compatibility notes derived from a japicmp pass over 47 modules, plus a section on known Grails 7 plugin incompatibilities and how to work around them.

### GSP, Tag Libraries and Static Compilation

**Compiled tag resolution.** Tag libraries are now described when they are compiled, and that description resolves tag calls in pages, tag libraries and controllers compiled afterwards. A call whose namespace and tag are known compiles into a direct invocation instead of a metaclass dispatch, and no tag methods are installed onto tag library, page or dispatcher metaclasses any more. The tag is still selected by name at runtime, so overrides and registration order behave exactly as before. In the framework's own measurements this removed about two-thirds of the per-call cost in a statically compiled page and a third in a tag library. Applications whose tag libraries are all described can ask for build errors instead of runtime failures:

```groovy
grails {
    compileStatic {
        strictTags = true
        dynamicTagNamespaces = ['legacy']   // registered while the application runs
    }
}
```

Defining a tag as a `Closure` field still works but is deprecated and now warns at compile time. Method-based tags gained more predictable named-attribute binding, exclude inherited framework and `Object` methods from dispatch, and preserve real namespace property getters; the Gradle extension defaults `preserveParameterNames` to `true` so parameter names are available to typed tag methods.

**Application-wide GSP static compilation.** Grails 7.2 could compile a page statically with a page directive. Grails 8 makes that something an application can switch on everywhere:

```groovy
grails {
    compileStatic {
        gsp = true
        strictGsp = true   // optional: report any undeclared name
    }
}
```

Framework-bound names such as `params`, `flash`, `request`, `response`, `session`, `servletContext`, `webRequest`, `controllerName` and `actionName` are typed and checked without declaring them; a page that declares no `model` reads its model dynamically instead of failing; names introduced by `var`/`status` attributes need no declaration; and `<g:set type="int" var="total" .../>` gives a variable a type so operators can be applied. Any page can still opt out with `<%@ page compileStatic="false" %>`. In the framework's own in-process measurements a statically compiled page rendered roughly 1.4–1.7 times faster.

**Opt every controller, service and tag library into `@GrailsCompileStatic`** from the build, without annotating each class, and controllers annotated with `@GrailsCompileStatic` can now call tag library methods (`link(...)`, `my.customTag(...)`) without compile-time errors:

```groovy
grails {
    compileStatic {
        controllers = true
        services = true
        tagLibs = true   // or: all = true
    }
}
```

**GSP in a plain Spring Boot application.** `org.apache.grails.views:grails-gsp-spring-boot` is now published and documented. A Spring Boot application with no `grails-app` directory, no plugins and Spring MVC routing can render GSP views from `src/main/resources/templates`, precompile them with `compileGroovyPages`, and ship without the `.gsp` sources.

**SiteMesh 3 is the default layout engine.** New applications use `org.apache.grails:grails-sitemesh3` on the SiteMesh 3.3 line for Spring Boot 4. Decoration is applied at the bean-definition level, which fixes layouts silently disappearing when Boot's content-negotiating view resolver wraps the view resolver first, and both SiteMesh 3 tag libraries are method-based. The SiteMesh 2 based `grails-layout` remains available as an opt-in (`--features=grails-layout` in Forge).

**Reproducible precompiled GSPs.** Generated page classes record a source checksum instead of a file modification time, so identical sources compile to identical bytes on every checkout and Gradle's build cache actually hits. Reloading is also more accurate: an edit is caught however close two saves fall, and touching a file no longer recompiles it. `compileGroovyPages` now forks the JVM the project's Java toolchain asks for, rather than whatever JVM ran Gradle.

**Smaller GSP improvements:** `\${...}` renders a literal `${...}` (handy for JavaScript template literals), links, forms, pagination, redirects and includes resolve the target controller's namespace automatically when it is unambiguous, and scaffolded views are now expanded and compiled at build time so packaged applications and native images do not generate them on the first request.

### Controllers, Requests and Content Negotiation

**Every controller as a REST resource.** `"/$controller"(resources: '*')` applies a `resources` mapping to every controller at once, resolving the controller from the request rather than from the mapping; `includes`, `excludes` and `group` work as they do for a named resource.

**Modern content negotiation.** The framework now supplies built-in MIME defaults (`all`, `atom`, `css`, `csv`, `form`, `html`, `js`, `json`, `multipartForm`, `pdf`, `rss`, `text`, `hal`, `xml`), so new applications no longer carry a `grails.mime.types` block, and `grails.mime.mergeDefaults: true` layers a declared block over them. The `Accept` header is honored for every client: the Firefox 2-era user-agent exclusion list defaults to unset, so a browser `fetch()` asking for `application/json` gets JSON.

**`@EnableWebMvc` is no longer added to your Application class.** Spring Boot's `WebMvcAutoConfiguration` is now active, so `spring.mvc.*` and `spring.web.*` properties work, `PUT`/`PATCH`/`DELETE` form bodies are parsed into `params`, and locale resolution is configurable through `grails.i18n.localeResolver` (`session`, `cookie`, `acceptHeader` or `fixed`). Grails removes Boot's catch-all view resolver, static-resource handler and welcome-page mapping automatically so `UrlMappings` still owns `/`.

**Typed reads of request, session, flash and servlet-context attributes.** All four gain the same null-safe converters `params` has had for years — `request.int('page', 1)`, `session.string('timeZone', 'UTC')`, `flash.string('notice')`, `servletContext.int('maxUploads', 10)` — and `session`, `request` and `servletContext` also get a typed, non-coercing `getAttribute(name, Class)` that needs no cast under `@GrailsCompileStatic`.

**Observability.** Grails dispatches through URL mappings rather than MVC handler methods, so Micrometer and OpenTelemetry used to record every request as `uri=UNKNOWN`. Grails 8 tags `http.server.requests` with `/<controller>/<action>` and emits inline observations for `grails.controller`, `grails.interceptor`, `grails.databinding`, `grails.convert` and `grails.render`, plus `gsp.view`, `gsp.template`, `gsp.layout` and `gsp.compile` spans. Everything is a no-op when observations are disabled.

**A leaner request path.** Per-context collaborators are cached, request parameters are built once per request rather than once per candidate mapping, the `LocaleContext` is restored rather than cleared, and Spring's API-versioning `Deprecation`/`Sunset`/`Link` headers are now emitted for Grails-mapped requests. URL mapping tokens such as `$action` are now filled from the URI only, never from a request parameter.

**The hidden HTTP method filter is off by default.** The filter that rewrote `POST` into `PUT`/`PATCH`/`DELETE` from `_method` (or the `X-HTTP-Method-Override` header) ran ahead of Spring Security and forced multipart bodies to be parsed before routing. `_method` is now resolved inside the dispatcher for `PUT`, `PATCH` and `DELETE` only, the header is no longer honored, and a `resources` mapping additionally accepts `POST /books/$id` as `update`. Set `grails.web.hiddenmethod.filter.enabled: true` to restore the previous behavior.

**File uploads use Spring Boot's multipart configuration.** `grails.controllers.upload.*` is removed in favor of `spring.servlet.multipart.*` (Boot's defaults are 1 MB per file and 10 MB per request), `request` is always the outermost servlet request rather than a `MultipartHttpServletRequest` (`request.getFile(...)` and friends still work), and an oversized upload now reaches your application's error handling as a `413` — `"413"(controller: 'errors', action: 'tooLarge')` — instead of a container error page.

### Data Binding and Security Hardening

**Opt-in deny-by-default data binding.** Binding stays permissive by default. Setting `grails.databinding.denyByDefault: true` switches to an allowlist: only properties marked `bindable: true`, an explicit `include:` list, or a `@BindAllowed(['firstName', 'lastName'])` command-object parameter are bound, enforced through nested associations, collections and maps. In either mode an explicit `bindable: false` is never mass-assigned any more. `bindData(book, params, [include: ['title', 'description'], clearMissing: true])` clears included properties that are absent from the binding source.

**Compile-time GORM query safety check.** A `String` flattened from an interpolated `GString` and passed to `find`, `findAll`, `executeQuery`, `executeUpdate`, `findAllWithSql` or Neo4j `cypherStatic` is the one case GORM's runtime parameter binding cannot see, and it now fails the build. Passing the `GString` directly remains safe and is never flagged. `@SuppressWarnings("GormUnsafeQueryString")` suppresses a single site; the `protectSqlInjectionAttacks=false` system property disables the check build-wide.

**XML request bodies.** The SAX hardening features Grails sets had been registered under unrecognized `https://` identifiers since an HTTPS link sweep in 2024, silently leaving parser defaults in place. Grails 8 uses the registered identifiers, disables XInclude, external entities and DTD loading, and refuses any XML request body that declares a `DOCTYPE`.

**Interceptor and security matcher paths are canonicalized like dispatch.** `match(uri:)`, `excludes(uri:)`, the Spring Security compatibility request matcher and the IP address filter previously matched the raw request URI, so `/%61dmin/users` or `/admin;x=1/users` reached `AdminController` without triggering an interceptor for `/admin/**`. All three now match what the dispatcher has always matched.

**Validated query arguments.** `sort`, `order` and (on Hibernate 7) `fetch` arguments are validated before a query is built rather than interpolated into HQL, on `list()`, dynamic finders, criteria, where queries and `listOrderBy*`. Invalid values throw `IllegalArgumentException` without echoing the value; `order: 'descending'` no longer silently sorts ascending.

**Also hardened:** `withForm` tokens are consumed atomically and bounded per session; Grails Wrapper and Forge repository overrides must be HTTPS at every hop; FuseSource Jansi (CVE-2026-8484) is gone from the classpath entirely; and the repository now publishes a [THREAT_MODEL.md](https://github.com/apache/grails-core/blob/8.0.x/THREAT_MODEL.md) following the ASF security threat-model rubric.

### Internationalization and the Generated UI

**Message bundles are resolved by Spring Boot.** Grails' custom message source, and the `classpath*:*.properties` scan it performed at startup, are gone. Base names are recorded at build time per application and per plugin, so the whole `spring.messages.*` surface (`cache-duration`, `use-code-as-default-message`, ...) works as it does in any Boot application, and GraalVM resource hints for bundles are registered automatically. Plugin bundles must now be namespaced on the plugin name (`spring-security-core.properties` rather than `messages.properties`).

**Available-locale discovery and a real language selector.** Grails now knows which locales an application is actually translated into, and `<g:localeSelect available="true" pinDefault="true" type="dropdown"/>` renders a complete Bootstrap navbar language menu from them — or `type="links"`, or a body form exposing `loc.autonym`, `loc.menuName` and friends — instead of listing ~150 JVM locales.

**A new welcome page and layout.** The generated application's welcome page and layout are internationalized in 18 bundled locales, gain a light/dark/auto theme selector, and replace the static controller list with switchable artefact cards (controllers, domains, services, tag libraries) and a runtime-internals card showing the effective servlet filter pipeline, Spring Security filter chains, MIME types and actuators. The jQuery webjar moves to 4.0.0 and Bootstrap to 5.3.8.

### Async, Virtual Threads and Performance

**Promises run on Spring Boot's task executor.** The default `PromiseFactory` is now `CompletableFuturePromiseFactory`, and in an application promises and the default event bus execute on Boot's `applicationTaskExecutor`, so `spring.task.execution.*`, graceful shutdown, `TaskDecorator` beans and `spring.threads.virtual.enabled=true` all apply without code changes. Grails propagates the `GrailsWebRequest` into `task { }` blocks, every promise is a JDK `CompletionStage`, and async controller responses honor `spring.mvc.async.request-timeout` (rendering a `503` you can map). A `grails.async.promiseFactory=virtual-thread` opt-in provides a dedicated virtual-thread factory.

**Virtual-thread friendliness on the hot path.** `GrailsHttpSession` replaces `synchronized` with a `ReentrantLock` to avoid carrier pinning, the codec extension helpers are statically compiled, codec registration no longer re-adds identical `encodeAs*` methods to the `String` metaclass on every reload, and thread-locals are cleared instead of retained across pooled threads.

**GORM startup now scales as O(entities + connections).** `GormEnhancer` used to eagerly build static, instance and validation APIs for every entity × connection/tenant pair; a new `GormRegistry` builds them lazily on first use, which matters most for applications with many domain classes or schema/database multi-tenancy.

A JMH benchmark suite now runs on every pull request labeled `performance`, comparing URL mappings, data binding, GSP rendering, interceptors and views against the base branch.

### Plugins and Spring Integration

**Plugin beans register before Spring Boot auto-configuration.** A bean a plugin contributes now takes precedence over a Spring Boot default guarded by `@ConditionalOnMissingBean` — Boot backs off, with no need to override or remove it afterwards. Plugins register beans through the new `beanRegistrar()` hook, which returns a Spring Framework `BeanRegistrar`; the `doWithSpring` bean builder DSL is deprecated but continues to work, and a statically compilable `doWithSpring(BeanBuilder)` method form is available as a stepping stone.

```groovy
class MyGrailsPlugin extends Plugin {
    @Override
    BeanRegistrar beanRegistrar() {
        { BeanRegistry registry, Environment environment ->
            registry.registerBean('myService', MyServiceImpl)
        } as BeanRegistrar
    }
}
```

**`@GrailsBeans`: a bean-wiring DSL compiled into real auto-configuration.** A `beans = { ... }` block on the `Application` class or on a `*GrailsPlugin` descriptor is rewritten at compile time into `@Bean` factory methods on a genuine `@AutoConfiguration` class — nothing DSL-shaped survives in bytecode, the methods are statically compiled, and Boot's own conditional pipeline applies:

```groovy
class Application extends GrailsAutoConfiguration {
    def beans = {
        bean(MyService)

        bean('mailSender', JavaMailSenderImpl).conditionalOnMissingBean(JavaMailSender) {
            new JavaMailSenderImpl(host: 'localhost')
        }
    }
}
```

Qualifiers such as `.conditionalOnMissingBean(...)`, `.conditionalOnProperty('app.offline', havingValue: 'false')`, `.conditionalOnClass(...)`, `.conditionalOnGrailsEnv('development')`, `.primary()`, `.lazy()` and `.scope(...)` chain onto a declaration, `@AutoConfiguration(before=/after=)` ordering and nested conditional `group(...)` blocks are supported, the `AutoConfiguration.imports` entry is written for you, and eight of the framework's own auto-configurations are now authored this way.

**More framework beans are cleanly overridable** (`localeResolver`, `messageSource`, `grailsCorsFilter`, `exceptionHandler`, `grailsCacheManager` and others are `@ConditionalOnMissingBean`), `spring.main.allow-bean-definition-overriding` and `allow-circular-references` are configurable instead of hardcoded, `grails.spring.bean.packages` scanning works from any working directory, the Grails plugin lifecycle runs only for a Grails application, and unresolved plugin dependencies are reported with the exact cause instead of a bare failure.

**Configuration metadata.** Every Grails module jar now ships a standard `META-INF/spring-configuration-metadata.json`, generated at build time from Groovy `@ConfigurationProperties` classes, Java classes and Groovy configuration DSL scripts. IDEs get completion, types and defaults for `grails.*` keys in `application.yml`, and the guide gains a generated Application Properties reference.

### Grails Data (GORM)

**GORM for Hibernate 7.** Grails 8 ships a complete GORM implementation for Hibernate ORM 7.4 alongside Hibernate 5, which remains the default. The version-agnostic GORM test suite runs natively against both, so every contract is verified on both lines. Switching an application is a BOM and a plugin:

```groovy
dependencies {
    implementation enforcedPlatform("org.apache.grails:grails-hibernate7-bom:[%version]")
    implementation "org.apache.grails:grails-hibernate7"
}
```

Database migration ships for Hibernate 7 as `grails-data-hibernate7-dbmigration` (with its `dbm-*` commands in a companion `-cli` artifact), and Grails Forge generates Hibernate 7 applications directly with `--data=hibernate7`. Because Spring Framework 7 removed `org.springframework.orm.hibernate5` entirely, Grails vendors that support code for both Hibernate lines. On Hibernate 7, GORM now tracks changes from persist rather than insert (a new entity with a pre-insert identifier starts at `version` 0 with a single `INSERT`), persistence listeners receive `Persist` and `Merge` events, and `id generator: 'sequence'` derives its sequence name from the table. Hibernate 5 gains an in-tree ByteBuddy proxy factory that replaces a third-party dependency, and reading a proxy's identifier never initializes it on either line.

**Locking.** `book.refresh(lock: true)` reloads an entity's state under a pessimistic write lock, `Book.lock(id, refresh: true)` reloads an already-managed instance under the lock, and both accept `type:`/`lock:` with any `jakarta.persistence.LockModeType` on Hibernate 5 and 7. `entity.mutex { }` now reloads under an exclusive lock, so it waits for a competing writer instead of failing with an optimistic locking exception.

**Domain properties are nullable by default**, aligning GORM with JPA, Spring Data and Bean Validation; declare `nullable: false` for required properties, or restore the old default with `grails.gorm.default.nullable: false`. **`count()` returns `Long`** instead of silently narrowing to `Integer`.

**Datastore-native identity types.** `grails { gorm { defaultIdType = 'native' } }` lets a domain class that declares no `id` take the identity type of the GORM implementation it is mapped with — `String` for MongoDB, `Long` for Hibernate — resolved at compile time from `mapWith`, so the same source works against either datastore.

**Also new:** collection properties stay dirty-checked after reassignment and iterator-based removals are tracked on interception-based stores; a static `Book.deleteAll()` removes every instance; calls on a class inside `Book.secondary.withTransaction { }` route to that connection across Hibernate, MongoDB and Neo4j; auto-timestamp suppression is thread-safe; a new Multi-Tenancy chapter documents `Tenants.withId`, `withTenant` and `eachTenant`; and GORM entities stay on the DevTools restart class loader, ending the `Not an entity` errors after a restart.

### GORM for MongoDB

- **Multi-document transactions.** `grails.mongodb.transactional = true` makes `withTransaction { }` and `@Transactional` drive a real server-side transaction over a `ClientSession`, so every read and write commits or rolls back atomically (replica set or sharded cluster required).
- **Spring Data MongoDB interoperability.** The optional `grails-data-mongodb-spring-data` module auto-configures a `MongoTemplate`, `MongoDatabaseFactory` and transaction manager over GORM's existing `MongoClient`, so GORM and Spring Data repositories share one connection and one transaction inside a single `@Transactional` method.
- **Embedded MongoDB.** `org.apache.grails:grails-data-mongodb-embedded` starts a server when the connection URL names the host `embedded` (`mongodb://embedded/bookstore`), with an in-memory backend for millisecond startup or a real `mongod` via Flapdoodle, single-node replica sets for transaction tests, and DevTools restart reuse. Forge wires it in by default for MongoDB applications, so a generated application runs without MongoDB or Docker installed.
- **TTL, text and reconciled indexes.** `indexAttributes: [expireAfterSeconds: 3600]` declares a TTL index, `indexAttributes: [type: 'text']` a text index, changed TTLs are applied in place with `collMod`, and `grails.mongodb.buildIndexes: false` / `buildIndexesAsync: true` take index creation off the startup path.
- **`String` ids are stored as `ObjectId` by default.** Code still sees the 24-character hex string; applications with existing BSON-string `_id` values should pin `grails.mongodb.stringIds.defaultStoredAs: string`. Association and `IN` criteria coerce correctly, `updateAll` is supported, and the pre-GORM-6 `mapping` engine is deprecated.
- **Read-only transactions no longer flush**, matching every other GORM datastore; a `MongoClientSettingsBuilderCustomizer` bean customizes the driver; an externally supplied `MongoClient` is no longer closed by GORM, and `grails.mongodb.*` settings are honored when one is supplied.
- **CRaC checkpoint/restore readiness.** `MongoDatastore` closes every client before a checkpoint and rebuilds them on restore — 54 ms to restore versus 3.4 s to start cold in the framework's measurement on Azul Zulu 25 with CRaC.

### GORM for Neo4j

GORM for Neo4j was never built or published for the Grails 7 line. Grails 8 brings it back, migrated to Groovy 5, Jakarta and Spring Boot 4, on the new `GormRegistry`, and published as `org.apache.grails:grails-data-neo4j`, `org.apache.grails:grails-data-neo4j-spring-boot`, `org.apache.grails.data:grails-data-neo4j-core` and `org.apache.grails:grails-neo4j-bom`. Grails Forge generates a Neo4j application with `--data=neo4j`, named connections start and route correctly, and Cypher query strings participate in the compile-time query safety check.

### Build, CLI and Developer Tooling

**Commands move to companion `-cli` artifacts.** `ApplicationCommand` implementations — the `dbm-*` migration commands, the scaffolding `generate-*` commands, `url-mappings-report`, `schema-export`, the Spring Security `s2-*` commands — no longer ship inside runtime plugin jars. Each command-bearing module publishes a companion artifact with a `-cli` suffix, applications consume them through a new `grailsCli` Gradle configuration that never reaches `runtimeClasspath`, `bootJar` or `bootWar`, and the Grails Gradle plugin discovers every companion advertised by your dependency graph automatically. Plugin authors get the whole publishing side from the new `org.apache.grails.gradle.grails-plugin-cli` plugin, and `grails { legacyCommandSupport = true }` keeps unchanged Grails 7 command plugins working through a deprecated bridge.

**Developer tooling leaves the application classpath.** Jansi, `grails-console`, the Groovy console and shell, and JLine no longer reach `runtimeClasspath`; ANSI output is controlled by Spring Boot's `spring.output.ansi.enabled`, and a `validateProductionClasspath` check keeps it that way.

**A faster, project-aware CLI.** A release CLI never re-checks remote `maven-metadata.xml`, dependency resolution runs in parallel (`-Dgrails.dependency.resolution.threads=N`), `grails --help` inside a project lists the application's own commands, Grails tasks appear in the `Grails` Gradle task group, and the start scripts grant native access so JLine produces no JEP 472 warnings on JDK 24+.

**No generated `logback-spring.xml`.** New applications rely on Spring Boot's Logback defaults (`logging.level.*`, `logging.pattern.console`, `logging.file.name`); `--features=logback-config` generates an editable starter file for those who want one. `grails.logging.stackTraceFiltererClass` and `grails.exceptionresolver.logFullStackTraceOnFilter` now govern every stack-trace sanitizer, not only the exception resolver.

**Gradle plugin changes.** The vestigial `gspCompile` configuration is removed (GSPs compile against `compileClasspath`), the combined Groovy compiler configuration script is produced by a proper `generateCompileGroovyGrailsCompilerConfig` task, and the `grails { }` extension gains `bom`, `cliAutoProvision`, `legacyCommandSupport`, `preserveParameterNames`, `compileStatic { }`, `gorm { }`, `i18n { }`, `aotCache { }` and `nativeMetadata { }` blocks.

**DevTools.** On macOS, Grails' directory watcher no longer silently falls back to one-second polling whenever JNA is on the classpath (add `io.methvin:directory-watcher` for native FSEvents); Spring Boot 4 turns LiveReload off by default (Forge enables it in `application-development.yml`); and the restart class loader is resolved through Tomcat's web-app loader so GORM entities are found after a restart.

**Test fixtures.** `JsonViewTest`, `GraphQLSchemaSpec`, `GrailsWebMockUtil` and the `Mock*` resource-loader helpers are no longer on production classpaths; request them with `testImplementation testFixtures('org.apache.grails:grails-core')` and friends.

### Deployment and Runtime

**Ahead-of-time processing.** Applying Spring Boot's AOT plugin adds a `processAot` task that generates bean definitions as source at build time, so a start with `-Dspring.aot.enabled=true` skips classpath scanning and annotation processing. The Grails Gradle plugin sets `grails.env` to `production` on `processAot` automatically, and the guide documents what AOT changes at runtime and what it does not yet cover (beans contributed through a plugin's `beanRegistrar()` are re-registered at runtime).

**GraalVM native image metadata.** `generateNativeMetadata` writes reachability metadata for your artefacts and compiled pages from the build output, and `traceNativeMetadata` runs the application under GraalVM's tracing agent along the paths and forms you list (`grails { nativeMetadata { paths = ['/', '/book']; forms = ['/login?username=admin&password=secret'] } }`), carrying the login session so pages behind authentication are traced.

**JDK AOT cache training (Project Leyden).** On JDK 25 or later, `grails.aotCache` trains a JDK AOT cache by running the packaged application once over the paths you list and recording the classes it loaded and linked and the methods it ran:

```groovy
grails {
    aotCache {
        enabled = true
        paths = ['/', '/login', '/book/index']
    }
}
```

Start the application with `java -XX:AOTCache=build/aot-cache/myapp.aot -Dspring.aot.enabled=true -jar build/aot-cache/application/myapp.jar`. In the framework's measurement of a GORM/Hibernate application with Spring Security and the asset pipeline, startup fell from 2.594 s to 0.933 s, and a generated `aot-cache.properties` records the JDK build, archive checksum and training arguments so a deployment can tell whether the cache still applies.

**Undertow is back.** Spring Boot 4 dropped its Undertow starter before Undertow had Jakarta Servlet 6.1 support; Grails 8 restores it as `org.apache.grails:grails-undertow` (Undertow 2.4 with the `io.undertow.ee` servlet and websocket modules). Tomcat 11, Jetty 12 and Undertow are all supported, Forge's `--servlet=undertow` works again, and the startup banner names the container in use.

**The startup banner** is colored, reports the servlet container and Spring Security version by default, and shows how the application was started — `NATIVE`, `AOT CACHE` or `AOT` — where that is worth saying. For WAR deployment to an external container, `providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat-runtime'` replaces the old Tomcat starter.

### Testing

- **Latency testing.** `grails-testing-support-latency` injects random artificial latency into the application under test (`grails.testing.latency.enabled`, `min-delay`, `max-delay`, `probability`, `url-patterns`, `seed`), turning intermittently flaky functional tests into deterministic failures.
- **`httpPostForm(...)`** on `HttpClientSupport` sends `application/x-www-form-urlencoded` bodies with UTF-8 encoding and repeated keys for collection values.
- **Tag library tests clean up automatically.** `purgeTagLibMetaClass` is gone; mocked tag libraries are cleared and rebuilt between feature methods.
- **Continuous test mode is documented.** `grails> test-app -continuous` reruns the selected tests whenever a source file changes, without leaving the interactive CLI.
- **Embedded MongoDB** and single-node replica sets make MongoDB and transaction specs run without Docker.
- Spring Boot 4's test changes apply: `@MockBean`/`@SpyBean` become `@MockitoBean`/`@MockitoSpyBean`, and `@SpringBootTest` no longer auto-configures `MockMvc` or `TestRestTemplate` without the corresponding `@AutoConfigure*` annotation. `GrailsUnitTest`, `GrailsWebUnitTest` and `@Integration` users are unaffected.

### Grails Forge

Grails Forge is now a Micronaut 4 application built with Gradle 9.7, and it generates Grails 8 applications with:

- A new `-d, --data` option (`hibernate5`, `hibernate7`, `mongodb`, `neo4j`; `-g/--gorm` and `hibernate` remain as legacy aliases) that applies the matching BOM consistently across application dependencies, the buildscript classpath and `buildSrc`.
- New features: `gorm-hibernate7`, `gorm-neo4j`, `gorm-async`, `grails-undertow`, `grails-layout`, `logback-config`, and three security options — `grails-spring-security` (User/Role/UserRole domain classes, a scaffolded user controller, static rules and a seeded admin), `grails-spring-security-ui` (adds user/role management, registration and forgot-password flows), and `spring-boot-starter-security` (plain Spring Security wired through the new beans DSL on the `Application` class).
- Embedded MongoDB wired in by default for MongoDB applications, Testcontainers 2.x artifact names, the internationalized welcome page with a theme selector, no generated Logback file, and a `/favicon.ico` mapping so the browser's icon fetch can no longer bounce through `/login`.

### Spring Security, Quartz and Other Plugins

**Grails Spring Security 8.0.0** runs on Spring Security 7.1. A new `grails-spring-security-compat` module reimplements the access-decision types Spring Security 7 removed (`ConfigAttribute`, `AccessDecisionManager`, `RoleVoter`, `AbstractSecurityInterceptor`, ...), so the plugin's voter and interceptor model — and application code built on it — keeps compiling and running. The **UI plugin is rewritten on Bootstrap 5 and jQuery 4**: all 44 screens render through the host application's layout (`grails.plugin.springsecurity.ui.gsp.parentLayout`), inheriting theme, locale selector and branding, with jQuery UI, DataTables, jGrowl and the bundled CSS gone. The core plugin's login and denied pages are plain Bootstrap 5, eight locales were added to its message bundle, and CAS single sign-out is now opt-in (`useSingleSignout`, default `false`) and actually works, verified against a real Apereo CAS Testcontainer.

**Grails Quartz 8.0.0** no longer lets one broken schedule take an application with it: a trigger that can never fire is logged and left unscheduled (`quartz.failOnNeverFiringTriggers: true` restores the old behavior), jobs are stamped with the registering application so two applications can share a JDBC job store, the scheduling methods report what is wrong instead of throwing `NullPointerException`, and a new Long-Running Jobs chapter covers concurrency, misfire handling and interrupting a job.

The **Cache**, **Mail** and **Redis** plugins are wired through `beanRegistrar()` and the beans DSL, and `grails.cache.enabled=yes|on|1` no longer fails startup.

### Documentation and AI-Assisted Development

The guide gains chapters on Ahead-of-Time Processing, Ahead-of-Time Caching, GSP Static Compilation, GSP in a Spring Boot Application, Compiled Tag Resolution, Multi-Tenancy and Long-Running Quartz Jobs, plus sections on Spring Boot structured logging, Spring HTTP interface clients, continuous testing, the `PATCH` mapping generated by `resources`, and a complete Hibernate 7 manual.

The grails-core repository also ships nine agent skills under `.agents/skills/` for AI coding assistants — including a **`grails-8-upgrade`** skill built from the upgrade guide, plus `grails-developer`, `groovy-developer`, `java-developer`, `gradle-developer`, `hibernate-developer`, `test-fixer`, `violation-fixer` and `mono-repo-integration` — so an assistant working on a Grails 8 project has the same conventions the maintainers use.

## Behavior Changes to Review Before Upgrading

The [upgrade guide](https://grails.apache.org/docs/[%version]/guide/upgrading.html#upgrading80x) covers each of these in depth. The ones most likely to touch an existing application:

- Java 21 and Gradle 9.7 minimums; the Spring Boot 4 starter renames (`spring-boot-starter-web` → `spring-boot-starter-webmvc`) and auto-configuration package relocations
- Jackson 3 is the default JSON library; enum serialization defaults changed
- GORM properties are nullable by default; `count()` returns `Long`
- The hidden HTTP method filter is off by default; `grails.controllers.upload.*` is replaced by `spring.servlet.multipart.*`
- The `Accept` header is honored for browsers; framework MIME defaults replace the generated `grails.mime.types` block
- Plugin message bundles must be namespaced; `spring.messages.*` is the configuration surface
- `doWithSpring` is deprecated in favor of `beanRegistrar()`; `@Configuration` classes declared in `resources.groovy` are no longer processed
- Commands live in `-cli` artifacts; Jansi is removed; no `logback-spring.xml` is generated
- MongoDB `String` ids are stored as `ObjectId` by default; read-only transactions on MongoDB no longer flush
- Hibernate 7 applications: `id generator: 'sequence'` derives its name from the table, and new entities start at version 0
- `org.apache.grails.data:grails-datamapping-async` moved to `org.apache.grails:grails-datamapping-async`

Add `runtimeOnly 'org.springframework.boot:spring-boot-properties-migrator'` for the duration of the migration to have deprecated and relocated configuration properties reported at startup.

## Dependency Upgrades

Grails [%version] ships with the following foundational dependency versions:

| Component | Grails 7.2 | Grails 8.0 |
|---|---|---|
| Java (minimum) | 17 | 21 |
| Gradle | 8.14 | 9.7 |
| Apache Groovy | 4.0.x | 5.1.x |
| Spock | 2.4-groovy-4.0 | 2.4-groovy-5.0 |
| Spring Boot | 3.5.x | 4.1.x |
| Spring Framework | 6.2.x | 7.0.x |
| Spring Security | 6.5.x | 7.1.x |
| Jackson | 2.x | 3.1.x |
| Apache Tomcat | 10.1 | 11.0 |
| Jakarta Servlet | 6.0 | 6.1 |
| Hibernate ORM | 5.6 | 5.6 (default) or 7.4 |
| SiteMesh 3 | 3.2 | 3.3 |
| Undertow | - | 2.4 |
| MongoDB driver | 5.9 | 5.12 |
| jQuery webjar | 3.7 | 4.0 |
| Bootstrap webjar | 5.3.3 | 5.3.8 |

See all managed versions in the [grails-bom](https://grails.apache.org/docs/[%version]/ref/Versions/Grails%20BOM.html), including the Hibernate 7 and Neo4j BOM variants.

## Pre-Release Notes

For the changes as they landed in each pre-release, see the GitHub release notes:
* [Grails 8.0.0-M1](https://github.com/apache/grails-core/releases/tag/v8.0.0-M1)
* [Grails 8.0.0-M2](https://github.com/apache/grails-core/releases/tag/v8.0.0-M2)
* [Grails 8.0.0-M3](https://github.com/apache/grails-core/releases/tag/v8.0.0-M3)
* [Grails 8.0.0-M4](https://github.com/apache/grails-core/releases/tag/v8.0.0-M4)
* [Grails 8.0.0-M5](https://github.com/apache/grails-core/releases/tag/v8.0.0-M5)
* [Grails 8.0.0-M6](https://github.com/apache/grails-core/releases/tag/v8.0.0-M6)
* [Grails 8.0.0-RC1](https://github.com/apache/grails-core/releases/tag/v8.0.0-RC1)

Full Changelog: [v[%priorVersion]...v[%version]](https://github.com/apache/grails-core/compare/v[%priorVersion]...v[%version])

## Generating a new Grails [%version] application with Grails Forge
Try out Grails today by visiting our online application generator [Grails Forge](https://start.grails.org). This is the quickest and the recommended way to get started with Grails.

After installing JetBrains' IntelliJ IDEA and the [Grails Plugin](https://plugins.jetbrains.com/plugin/18504-grails), the Grails Application Forge will also be available under New Project in IntelliJ IDEA.

From the command line, the Forge CLI generates a Grails 8 application with the GORM implementation of your choice:

```shell
grails -t forge create-app --data=hibernate7 com.example.demo
```

Within your newly generated project you can access the Grails CLIs with the Grails Wrapper.

See the [Types of CLI](https://grails.apache.org/docs/[%version]/guide/gettingStarted.html#_types_of_command_line_interface_cli) section in the documentation for details on each CLI.

grails-shell-cli

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
If you already have a Grails 7 application and want to upgrade to Grails [%version], follow these steps:

1. Install JDK 21 or later and update your toolchain, CI pipelines and deployment environments.
2. Update the Gradle wrapper; a Grails 7 wrapper does not work with the Grails 8 Gradle plugins:

    ```shell
    ./gradlew wrapper --gradle-version 9.7.1
    ```

3. Update your application's `gradle.properties` file to specify Grails [%version] as the desired version.

    ```properties
    grailsVersion=[%version]
    ```

4. Add the Spring Boot properties migrator for the duration of the migration so deprecated and relocated configuration properties are reported at startup:

    ```groovy
    runtimeOnly 'org.springframework.boot:spring-boot-properties-migrator'
    ```

5. Make any necessary adjustments to your application code, configuration, and dependencies to ensure compatibility with the new version. [See Upgrade Guide](https://grails.apache.org/docs/[%version]/guide/upgrading.html#upgrading80x). If you use an AI coding assistant, point it at the `grails-8-upgrade` skill in the [grails-core repository](https://github.com/apache/grails-core/tree/8.0.x/.agents/skills/grails-8-upgrade).

Normally, Grails Core dependencies are automatically updated using the Grails Bill of Materials (BOM). However, if you have specific versions defined in your build configuration, you may need to manually update them to align with Grails [%version]. Grails 8 no longer applies the Spring Dependency Management plugin, so a `dependencyManagement { }` block in your build must be replaced with plain dependency declarations or `resolutionStrategy.force`; property-based overrides in `gradle.properties` continue to work unchanged.

### Exploring Alternative Approaches
If manual dependency updates seem daunting, or you want a more streamlined approach, consider the following alternatives:

#### 1. Use Grails Forge Website
Visit [Grails Forge](https://start.grails.org) and generate a new Grails application with Grails [%version]. Compare the versions in the newly generated application with your existing one to identify any discrepancies. This can serve as a reference point for your update.

#### 2. Automated Dependency Update Bots
Configure automated dependency update bots like [Renovate](https://docs.renovatebot.com) or [Dependabot](https://dependabot.com) with your source control platform (e.g., GitHub). These bots can automatically detect and update outdated dependencies in your project, including Grails dependencies, saving you time and effort in manual updates.

With these steps and alternative approaches, you should be well on your way to enjoying the exciting features and improvements in Grails [%version].

## Why should you try out Grails [%version]?
* **Leverage cutting-edge foundations**: Built on Java 21, Apache Groovy 5.1, Spring Framework 7.0, Spring Boot 4.1, Jakarta EE 10 and your choice of Hibernate 5 or Hibernate 7.
* **Start faster and run leaner**: Spring AOT processing, JDK AOT cache training, native image metadata, a leaner request path, virtual-thread-friendly internals, and a GORM startup that scales with the size of your domain model.
* **Compile more of your application statically**: compiled tag resolution, application-wide GSP static compilation, and a single build setting to opt every controller, service and tag library into `@GrailsCompileStatic`.
* **Ship a smaller, safer application**: CLI dependencies out of your jar, deny-by-default binding, compile-time query safety, hardened XML and URI handling, and a published threat model.
* **Invest in the future**: This version drives ongoing innovation, backed by an active Grails community ensuring support, updates, and evolving capabilities tailored to your needs.

## Grails Release Schedule

<!-- TODO: confirm the schedule and support windows before publishing. -->
* **Grails [%version] Release**: Officially released on [%date].
* **Grails 8.0.x Patch Releases**: Scheduled at least monthly to align with Spring Boot's cadence; additional releases may occur as needed for urgent fixes.
* **Grails 7.x Support Period**: Grails 7.0.x, 7.1.x and 7.2.x continue to receive updates on the Spring Boot 3.5.x line. <!-- TODO: state the end-of-support date for the 7.x line -->
* **Grails 8.1.x Development**: <!-- TODO: planned start and Spring Boot alignment -->

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
A huge thank you to our amazing community for supporting the Grails Framework over the past 20 years! We're excited for
the future and grateful for the opportunity to continue innovating and pushing Grails forward together.

## Contributors
We would like to extend our heartfelt thanks to all the contributors who made Grails [%version] possible. \
Special thanks to:

<!-- Derived from the authors of commits and pull requests on the 8.0.x branch since it diverged from 7.2.x; add anyone whose 7.x-cycle work was merged forward. -->
* [James Fredley](https://github.com/jamesfredley)
* [Scott Murphy](https://github.com/codeconsole)
* [Mattias Reichel](https://github.com/matrei)
* [James Daugherty](https://github.com/jdaugherty)
* [Walter B Duque de Estrada](https://github.com/borinquenkid)
* [Søren Berg Glasius](https://github.com/sbglasius)
* [Paul King](https://github.com/paulk-asert)
* [Gianluca Sartori](https://github.com/gsartori)
* [David Estes](https://github.com/davydotcom)
* [Rahul Shishodia](https://github.com/rahulshishodia)
* [Thomas Rasmussen](https://github.com/dauer)
* [Szava Maczika](https://github.com/maczikasz)
* [Jarek Potiuk](https://github.com/potiuk)

Recent Contributors by Project:

* [grails-core](https://github.com/apache/grails-core/graphs/contributors[%6MonthsBackForGitHub])
* [grails-micronaut](https://github.com/apache/grails-micronaut/graphs/contributors[%6MonthsBackForGitHub])
* [grails-static-website](https://github.com/apache/grails-static-website/graphs/contributors[%6MonthsBackForGitHub])
* [grails-forge-ui](https://github.com/apache/grails-forge-ui/graphs/contributors[%6MonthsBackForGitHub])
* [grails-gradle-publish](https://github.com/apache/grails-gradle-publish/graphs/contributors[%6MonthsBackForGitHub])
* [grails-github-actions](https://github.com/apache/grails-github-actions/graphs/contributors[%6MonthsBackForGitHub])

[Combined Commit List](https://github.com/search?q=repo%3Aapache%2Fgrails-core+repo%3Aapache%2Fgrails-micronaut+repo%3Aapache%2Fgrails-static-website+repo%3Aapache%2Fgrails-forge-ui+repo%3Aapache%2Fgrails-gradle-publish+repo%3Aapache%2Fgrails-github-actions+is%3Apublic&type=commits&s=committer-date&o=desc)

Their dedication and hard work have significantly contributed to the release of Grails [%version].

Join the [Grails Slack Community](https://grails.slack.com), share your feedback, and contribute to making Grails Framework even better in
the future. Happy coding!
