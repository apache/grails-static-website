<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Authors</title>
</head>
<body>
<div class="container my-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1>Authors</h1>
        <g:link action="create" class="btn btn-primary">New Author</g:link>
    </div>
    <f:table collection="${authorList}" properties="name,email,website"/>
</div>
</body>
</html>
