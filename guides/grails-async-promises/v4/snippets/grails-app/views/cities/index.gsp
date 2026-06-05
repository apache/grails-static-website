<html>
<head>
    <title>Largest US City Weather</title>
    <meta name="layout" content="main" />
</head>
<body>
<div id="content" role="main">
    <section class="row colset-2-its">
        <g:each in="${currentWeatherList}" var="${currentWeather}">
            <g:if test="${currentWeather}">
                <g:render template="/openweather/currentWeather"
                          model="[currentWeather: currentWeather, unit: unit]" />
            </g:if>
        </g:each>
    </section>
</div>
</body>
</html>