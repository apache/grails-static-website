<html>
<head>
    <title>Groovy & Grails Books</title>
    <meta name="layout" content="main" />
</head>
<body>
<div id="content" role="main">
    <section class="row colset-2-its">
        <g:each in="${bookList}" var="${book}">
            <g:link controller="book" id="${book.id}" action="show">
                <asset:image src="${book.image}" width="200" />
            </g:link>
        </g:each>
    </section>
</div>
</body>
</html>