<html>
<head>
    <title>Sign Up</title>
    <meta name="layout" content="main" />
    <style type="text/css">
        ol li {
            list-style-type: none;
            margin: 10px auto;
        }
        ol li label {
            width: 120px;
        }
    </style>
</head>
<body>
<div id="content" role="main">
    <g:hasErrors bean="${signUpInstance}">
    <ul class="errors">
        <g:eachError bean="${signUpInstance}">
            <li><g:message error="${it}"/></li>
        </g:eachError>
    </ul>
    </g:hasErrors>
    <p class="message">${flash.message}</p>

    <h1><g:message code="register.title" default="Register Form"/></h1>
    <g:form controller="register" action="save" method="POST">
        <ol>
            <li>
                <label for="firstName">First Name</label>
                <g:textField name="firstName" id="firstName" value="${signUpInstance.firstName}" />
            </li>
            <li>
                <label for="lastName">Last Name</label>
                <g:textField name="lastName" id="lastName" value="${signUpInstance.lastName}" />
            </li>
            <li>
                <label for="email">Email</label>
                <g:textField name="email" id="email" value="${signUpInstance.email}" />
            </li>
            <li>
                <g:submitButton name="Submit" id="submit"/>
            </li>

        </ol>

    </g:form>
</div>
</body>
</html>