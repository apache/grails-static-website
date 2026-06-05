<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>${book?.title}</title>
</head>
<body>
<div class="container my-4">
    <h1>${book?.title}</h1>
    <f:display bean="book" except="dateCreated,lastUpdated"/>
    <div class="mt-3">
        <g:link resource="${book}" action="edit" class="btn btn-warning">Edit</g:link>
        <g:link action="index" class="btn btn-link">Back</g:link>
    </div>
</div>
</body>
</html>
