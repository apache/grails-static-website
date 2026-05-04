<g:render template="task" collection="${tasks}" var="task"/>
<g:if test="${!tasks}">
    <li class="list-group-item text-body-secondary">No tasks yet. Add one above.</li>
</g:if>
