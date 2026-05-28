<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Book List</title>
</head>
<body>
<h1>Book List</h1>
<table>
    <thead>
        <tr><th>Title</th><th>ISBN</th><th>Pages</th></tr>
    </thead>
    <tbody>
        <g:each in="${bookList}" var="book">
            <tr>
                <td>${book.title}</td>
                <td>${book.isbn}</td>
                <td>${book.pageCount}</td>
            </tr>
        </g:each>
    </tbody>
</table>
</body>
</html>
