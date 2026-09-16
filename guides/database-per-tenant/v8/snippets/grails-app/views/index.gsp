<!doctype html>
<html>
<head>
    <title>Select Manufacturer</title>
    <meta name="layout" content="main"/>
</head>
<body>
<main id="content" role="main" class="container py-4">
    <h1 class="display-6 fw-semibold mb-3">Welcome to Grails</h1>
    <p class="text-body-secondary mb-4">
        Select a manufacturer to switch tenants. Each manufacturer uses a separate database.
    </p>
    <!-- tag::iterateManufacturers[] -->
    <div id="controllers" role="navigation">
        <h2>Available Manufacturers:</h2>
        <ul>
            <g:each var="m" in="${manufacturers}">
                <li class="controller">
                    <g:link controller="manufacturer" action="select" id="${m.name}">${m.name}</g:link>
                </li>
            </g:each>
        </ul>
    </div>
    <!-- end::iterateManufacturers[] -->
</main>
</body>
</html>
