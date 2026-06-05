<html>
<head>
    <title>VAT Validator</title>
    <meta name="layout" content="main" />
    <style type="text/css">
        form ol li { list-style-type: none; }
    </style>
</head>
<body>
<div id="content" role="main">
    <section class="row colset-2-its">
        <g:if test="${flash.message}">
            <p class="message">${flash.message}</p>
        </g:if>
        <g:if test="${flash.error}">
            <p class="errors">${flash.error}</p>
        </g:if>
        <g:if test="${cmd}">
            <g:hasErrors bean="${cmd}">
                <div class="errors">
                    <g:eachError><p><g:message error="${it}"/></p></g:eachError>
                </div>
            </g:hasErrors>
        </g:if>
        <g:form controller="vat" method="GET">
            <ol>
                <li>
                    <label for="code"><g:message code="vat.country" default="Country"/></label>
                    <g:select id="code" name='code' value="${cmd?.code}"
                              noSelection="${['null':'Select One...']}"
                              from='${countries}'
                              optionKey="code" optionValue="name"></g:select>
                </li>
                <li>
                    <label for="code"><g:message code="vat.vatNumber" default="VAT Number"/></label>
                    <g:textField name="vatNumber" id="vatNumber" vatNumber="${cmd?.vatNumber}"/>
                </li>
                <li>
                    <g:actionSubmit id="submit" value="${message(code:'vat.check', default: 'Check')}" action="validate"/>
                </li>
            </ol>
        </g:form>
    </section>
</div>
</body>
</html>