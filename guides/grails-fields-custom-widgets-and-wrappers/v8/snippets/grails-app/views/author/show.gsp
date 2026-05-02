<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${author?.name}</title>
</head>
<body>
<div class="container my-4">
    <h1>${author?.name}</h1>
    <f:display bean="author" except="dateCreated,lastUpdated,books"/>
    <div class="mt-3">
        <g:link resource="${author}" action="edit" class="btn btn-warning">Edit</g:link>
        <g:link action="index" class="btn btn-link">Back</g:link>
    </div>
</div>
</body>
</html>
