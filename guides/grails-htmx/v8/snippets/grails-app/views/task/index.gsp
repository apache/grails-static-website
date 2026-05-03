<!doctype html>
<html>
<head>
    <title>HTMX Task Tracker</title>
    <meta name="layout" content="main"/>
</head>
<body>

<main class="container-lg py-4">
    <h1 class="h3 mb-3">Tasks</h1>

    <%-- Add a task --%>
    <form id="taskForm"
          hx-post="${createLink(controller: 'task', action: 'create')}"
          hx-target="#taskList"
          hx-swap="afterbegin"
          hx-on::after-request="this.reset()"
          class="d-flex gap-2 mb-3">
        <input type="text" name="title" placeholder="What needs doing?" required class="form-control"/>
        <button type="submit" class="btn btn-primary">Add</button>
    </form>

    <%-- Live search --%>
    <input type="text"
           name="q"
           placeholder="Search tasks..."
           class="form-control mb-3"
           hx-get="${createLink(controller: 'task', action: 'search')}"
           hx-trigger="keyup changed delay:300ms"
           hx-target="#taskList"
           hx-swap="innerHTML"/>

    <%-- The list. HTMX swaps individual rows in/out of this <ul>. --%>
    <ul id="taskList" class="list-group">
        <g:render template="taskRows" model="[tasks: tasks]"/>
    </ul>
</main>

</body>
</html>
