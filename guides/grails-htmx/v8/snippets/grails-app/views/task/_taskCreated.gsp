<g:render template="taskForm" model="[task: formTask]"/>

<ul hx-swap-oob="afterbegin:#taskList">
    <g:render template="task" model="[task: task]"/>
</ul>
