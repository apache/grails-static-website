<html>
<head>
    <meta name="layout" content="${gspLayout ?: 'main'}"/>
    <title><g:message code='springSecurity.login.title'/></title>
</head>

<body>
<div id="login">
    <div class="inner centered" >
        <div class="fheader"><g:message code='springSecurity.login.header'/></div>
        <g:if test='${flash.message}'>
            <div class="login_message">${flash.message}</div>
        </g:if>
    </div>
</div>
</body>
</html>
