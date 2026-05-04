<!doctype html>
<html>
<head>
    <title>HTMX Task Tracker</title>
    <meta name="layout" content="main"/>
</head>
<body>

<main class="container-lg py-4">
    <h1 class="h3 mb-3">Tasks</h1>

    <g:render template="taskForm" model="[task: task]"/>

    <%-- Live search --%>
    <input type="text"
            name="q"
           value="${q}"
           placeholder="Search tasks..."
           class="form-control mb-3"
           hx-get="${createLink(controller: 'task', action: 'search')}"
            hx-trigger="keyup changed delay:300ms"
            hx-target="#taskList"
            hx-swap="innerHTML"/>

    <ul id="taskList" class="list-group">
        <g:render template="taskRows" model="[tasks: tasks]"/>
    </ul>
</main>

</body>
</html>
