<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Create Book</title>
</head>
<body>
<div class="container my-4">
    <h1>Create Book</h1>
    <g:hasErrors bean="${book}">
        <div class="alert alert-danger" role="alert">
            <g:eachError bean="${book}" var="error">
                <div><g:message error="${error}"/></div>
            </g:eachError>
        </div>
    </g:hasErrors>
    <g:form resource="${book}" method="POST">
        <f:all bean="book" except="dateCreated,lastUpdated"/>
        <button type="submit" class="btn btn-primary">Create</button>
        <g:link action="index" class="btn btn-link">Cancel</g:link>
    </g:form>
</div>
</body>
</html>
