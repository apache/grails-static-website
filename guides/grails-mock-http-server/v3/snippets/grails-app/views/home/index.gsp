<html>
<head>
    <title>Current Weather</title>
    <meta name="layout" content="main" />
</head>
<body>
    <div id="content" role="main">
        <section class="row colset-2-its">
            <g:if test="${currentWeather}">
                <g:render template="/openweather/currentWeather"
                          model="[currentWeather: currentWeather, unit: unit]"/>
            </g:if>
        </section>
    </div>
</body>
</html>