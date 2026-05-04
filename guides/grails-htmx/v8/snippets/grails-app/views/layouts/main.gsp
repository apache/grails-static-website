<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title><g:layoutTitle default="Grails + HTMX"/></title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>
    <asset:stylesheet src="application.css"/>

    <g:set var="csrfToken" value="${request.getAttribute('_csrf')}"/>
    <g:if test="${csrfToken}">
        <meta name="csrf-header" content="${csrfToken.headerName}"/>
        <meta name="csrf-token" content="${csrfToken.token}"/>
        <script>
            document.addEventListener('htmx:configRequest', function(event) {
                const header = document.querySelector('meta[name="csrf-header"]')?.content;
                const token = document.querySelector('meta[name="csrf-token"]')?.content;
                if (header && token) {
                    event.detail.headers[header] = token;
                }
            });
        </script>
    </g:if>

    <g:layoutHead/>
</head>
<body>

<nav class="navbar navbar-expand-lg bg-body border-bottom shadow-sm">
    <div class="container-lg">
        <a class="navbar-brand" href="${request.contextPath}/">
            Grails + HTMX
        </a>
    </div>
</nav>

<g:layoutBody/>

<asset:javascript src="application.js"/>
</body>
</html>
