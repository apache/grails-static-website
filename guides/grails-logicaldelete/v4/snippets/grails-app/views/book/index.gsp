<html>
<head>
    <title>Groovy & Grails Books</title>
    <meta name="layout" content="main" />
    <style type="text/css">
        .book {
            width: 150px;
            height: 300px;
            float: left;
            margin: 10px;
        }
        .book form {
            text-align: center;
            margin-bottom: 10px;
        }
        .message {
            overflow: auto;
        }
        #undo {
            margin: 10px;
            float: right;
        }
    </style>
</head>
<body>
<div id="content" role="main">
    <g:if test="${flash.message}">
        <g:if test="${undoId}">
            <g:form method="post" controller="book" action="undoDelete" id="${undoId}" class="message">
                <g:submitButton name="submit" value="${g.message(code: 'book.undoDelete', default: 'Undo')}"/>
                ${flash.message}
            </g:form>
        </g:if>
        <g:else>
            <div class="message">${flash.message}</div>
        </g:else>
    </g:if>

    <b><g:message code="books.total" default="Total number of Books"/><span id="totalNumberOfBooks">${total}</span></b>
    <section class="row">
        <g:each in="${bookList}" var="${book}">
            <div class="book">
                <g:form method="post" controller="book" action="delete" id="${book.id}">
                    <g:submitButton name="submit" value="${g.message(code: 'book.delete', default: 'Delete')}"/>
                </g:form>

                <g:link controller="book" id="${book.id}" action="show">
                    <asset:image src="${book.image}" width="150" />

                </g:link>
            </div>

        </g:each>
    </section>
</div>
</body>
</html>