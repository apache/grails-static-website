<!doctype html>
<html lang="en" class="h-full">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title><g:layoutTitle default="Grails"/></title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>
    <asset:stylesheet src="application.css"/>

    <%-- Pre-paint dark-mode resolution: read the user's saved choice (or
         system preference) and apply the `dark` class on <html> BEFORE
         the body renders. This avoids the light-to-dark flash on reload. --%>
    <script>
        (function () {
            var saved = localStorage.getItem('theme');
            var prefers = window.matchMedia('(prefers-color-scheme: dark)').matches;
            if (saved === 'dark' || (!saved && prefers)) {
                document.documentElement.classList.add('dark');
            }
        })();
    </script>
    <g:layoutHead/>
</head>

<body class="h-full bg-gray-50 text-gray-900 antialiased dark:bg-gray-900 dark:text-gray-100">

<nav class="border-b border-gray-200 bg-white shadow-sm dark:border-gray-700 dark:bg-gray-800">
    <div class="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
        <a class="flex items-center gap-2" href="${request.contextPath}/">
            <asset:image class="h-8 w-auto" src="grails.svg" alt="Grails Logo"/>
            <span class="text-base font-semibold">Grails + Tailwind CSS</span>
        </a>
        <div class="flex items-center gap-6">
            <a href="https://grails.apache.org/docs/" class="nav-link" target="_blank" rel="noopener">Docs</a>
            <a href="https://grails.apache.org/community.html" class="nav-link" target="_blank" rel="noopener">Community</a>
            <button id="theme-toggle"
                    type="button"
                    aria-label="Toggle dark mode"
                    class="inline-flex h-9 w-9 items-center justify-center rounded-md border border-gray-300 bg-white text-gray-700 shadow-sm hover:bg-gray-50 dark:border-gray-600 dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600">
                <span aria-hidden="true" class="dark:hidden">&#9788;</span>
                <span aria-hidden="true" class="hidden dark:inline">&#9789;</span>
            </button>
        </div>
    </div>
</nav>

<div class="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
    <g:flashMessages />
    <g:layoutBody/>
</div>

<footer class="mt-12 border-t border-gray-200 bg-white py-8 dark:border-gray-700 dark:bg-gray-800">
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <p class="text-sm text-gray-500 dark:text-gray-400">
            Built with Apache Grails 8 and Tailwind CSS 4. See the
            <a class="font-medium text-blue-600 hover:underline dark:text-blue-400"
               href="https://grails.apache.org/guides/grails-tailwindcss/8/guide/index.html">
                guide
            </a> for the full source.
        </p>
    </div>
</footer>

<script>
    document.getElementById('theme-toggle').addEventListener('click', function () {
        var html = document.documentElement;
        html.classList.toggle('dark');
        localStorage.setItem('theme', html.classList.contains('dark') ? 'dark' : 'light');
    });
</script>
</body>
</html>
