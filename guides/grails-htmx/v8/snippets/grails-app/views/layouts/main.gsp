<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title><g:layoutTitle default="Grails + HTMX"/></title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>
    <asset:stylesheet src="application.css"/>

    <%-- HTMX 2.x via CDN. For production, vendor the file under
         grails-app/assets/javascripts/ and load via asset:javascript. --%>
    <script src="https://unpkg.com/htmx.org@2.0.4"
            integrity="sha384-HGfztofotfshcF7+8n44JQL2oJmowVChPTg48S+jvZoztPfvwD79OC/LTtG6dMp+"
            crossorigin="anonymous"></script>

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
