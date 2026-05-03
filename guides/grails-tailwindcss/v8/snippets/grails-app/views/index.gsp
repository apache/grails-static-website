<%@ page import="grails.util.Environment"%>
<%@ page import="org.springframework.boot.SpringBootVersion"%>
<%@ page import="org.springframework.core.SpringVersion"%>
<g:set var="numControllers" value="${grailsApplication.controllerClasses.size()}"/>
<g:set var="numDomains" value="${grailsApplication.domainClasses.size()}"/>
<g:set var="numPlugins" value="${pluginManager.allPlugins.size()}"/>
<!doctype html>
<html>
<head>
    <title>Welcome to Grails + Tailwind</title>
    <meta name="layout" content="main"/>
</head>
<body>

<main id="content" role="main" class="space-y-8">

    <section class="card">
        <h1 class="text-3xl font-semibold tracking-tight">
            Welcome to Grails + Tailwind CSS
        </h1>
        <p class="mt-2 max-w-2xl text-base text-gray-600 dark:text-gray-400">
            This page is rendered by a Grails 8 GSP layout and styled entirely with
            Tailwind CSS 4 utilities. The dark-mode toggle in the navbar persists
            across reloads via <code class="rounded bg-gray-100 px-1 py-0.5 text-sm dark:bg-gray-700">localStorage</code>.
        </p>
        <div class="mt-6 flex flex-wrap gap-3">
            <a href="https://grails.apache.org/guides/grails-tailwindcss/8/guide/index.html"
               class="btn-primary"
               target="_blank" rel="noopener">
                Read the guide
            </a>
            <a href="https://github.com/grails-guides/grails-tailwindcss"
               class="inline-flex items-center justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-200 dark:hover:bg-gray-700"
               target="_blank" rel="noopener">
                View source
            </a>
        </div>
    </section>

    <section class="grid grid-cols-1 gap-6 md:grid-cols-3">
        <div class="card">
            <h2 class="text-sm font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
                Runtime
            </h2>
            <dl class="mt-4 space-y-2 text-sm">
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">Grails</dt>
                    <dd class="font-medium tabular-nums"><g:meta name="info.app.grailsVersion"/></dd>
                </div>
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">Spring Boot</dt>
                    <dd class="font-medium tabular-nums">${SpringBootVersion.getVersion()}</dd>
                </div>
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">Spring</dt>
                    <dd class="font-medium tabular-nums">${SpringVersion.getVersion()}</dd>
                </div>
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">Groovy</dt>
                    <dd class="font-medium tabular-nums">${GroovySystem.getVersion()}</dd>
                </div>
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">JVM</dt>
                    <dd class="font-medium tabular-nums">${System.getProperty('java.version')}</dd>
                </div>
            </dl>
        </div>

        <div class="card">
            <h2 class="text-sm font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
                Environment
            </h2>
            <dl class="mt-4 space-y-2 text-sm">
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">App name</dt>
                    <dd class="font-medium"><g:meta name="info.app.name"/></dd>
                </div>
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">Mode</dt>
                    <dd class="font-medium">${Environment.current.name}</dd>
                </div>
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">Reloading</dt>
                    <dd class="font-medium">
                        <g:if test="${Environment.reloadingAgentEnabled}">
                            <span class="inline-flex items-center gap-1 rounded-full bg-green-100 px-2 py-0.5 text-xs font-medium text-green-800 dark:bg-green-900/40 dark:text-green-300">
                                Active
                            </span>
                        </g:if>
                        <g:else>
                            <span class="inline-flex items-center gap-1 rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-700 dark:bg-gray-700 dark:text-gray-300">
                                Inactive
                            </span>
                        </g:else>
                    </dd>
                </div>
            </dl>
        </div>

        <div class="card">
            <h2 class="text-sm font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
                Artefacts
            </h2>
            <dl class="mt-4 space-y-2 text-sm">
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">Controllers</dt>
                    <dd class="font-medium tabular-nums">${numControllers}</dd>
                </div>
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">Domains</dt>
                    <dd class="font-medium tabular-nums">${numDomains}</dd>
                </div>
                <div class="flex justify-between">
                    <dt class="text-gray-600 dark:text-gray-400">Plugins</dt>
                    <dd class="font-medium tabular-nums">${numPlugins}</dd>
                </div>
            </dl>
        </div>
    </section>

</main>
</body>
</html>
