<g:set var="childClass" value="${persistentProperty.associatedEntity.javaClass}"/>
<g:set var="childController" value="${childClass.simpleName.toLowerCase()}"/>
<g:set var="children" value="${(value ?: []) as List}"/>

<g:if test="${children}">
    <g:each in="${children}" var="child">
        <span class="badge text-bg-secondary me-1">
            <g:link controller="${childController}" action="show" id="${child.id}" class="text-white text-decoration-none">
                ${child.toString().encodeAsHTML()}
            </g:link>
        </span>
    </g:each>
</g:if>
<g:else><span class="text-muted">&mdash;</span></g:else>
