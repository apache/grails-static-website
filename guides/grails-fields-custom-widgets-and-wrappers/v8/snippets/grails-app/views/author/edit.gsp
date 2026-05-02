<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Edit Author</title>
</head>
<body>
<div class="container my-4">
    <h1>Edit ${author?.name}</h1>
    <g:hasErrors bean="${author}">
        <div class="alert alert-danger" role="alert">
            <g:eachError bean="${author}" var="error">
                <div><g:message error="${error}"/></div>
            </g:eachError>
        </div>
    </g:hasErrors>
    <g:form resource="${author}" method="PUT">
        <g:hiddenField name="version" value="${author?.version}"/>
        <f:all bean="author" except="dateCreated,lastUpdated,books"/>
        <button type="submit" class="btn btn-primary">Update</button>
        <g:link resource="${author}" action="show" class="btn btn-link">Cancel</g:link>
    </g:form>
</div>
</body>
</html>
