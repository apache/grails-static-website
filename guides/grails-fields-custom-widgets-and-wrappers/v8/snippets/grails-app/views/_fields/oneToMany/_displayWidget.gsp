<g:set var="childClass" value="${persistentProperty.associatedEntity.javaClass}"/>
<g:set var="childController" value="${childClass.simpleName.toLowerCase()}"/>
<g:set var="children" value="${value ?: []}"/>

<g:if test="${children}">
    <ul class="list-inline mb-0">
        <g:each in="${children}" var="child" status="i">
            <li class="list-inline-item">
                <g:link controller="${childController}" action="show" id="${child.id}">
                    ${child.toString().encodeAsHTML()}</g:link><g:if test="${i < children.size() - 1}">,</g:if>
            </li>
        </g:each>
    </ul>
</g:if>
<g:else><span class="text-muted">&mdash;</span></g:else>
