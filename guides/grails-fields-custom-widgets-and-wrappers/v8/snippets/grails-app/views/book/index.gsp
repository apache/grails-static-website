<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Books</title>
</head>
<body>
<div class="container my-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1>Books</h1>
        <g:link action="create" class="btn btn-primary">New Book</g:link>
    </div>
    <f:table collection="${bookList}" properties="title,author,genre,priceUSD,inStock"/>
</div>
</body>
</html>
