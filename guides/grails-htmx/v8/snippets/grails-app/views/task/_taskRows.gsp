<g:each in="${tasks}" var="task">
    <g:render template="task" model="[task: task]"/>
</g:each>
<g:if test="${!tasks}">
    <li class="list-group-item text-body-secondary">No tasks yet. Add one above.</li>
</g:if>
