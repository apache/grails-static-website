<g:if test="${value}">
    <g:link controller="${persistentProperty.associatedEntity.javaClass.simpleName.toLowerCase()}"
            action="show"
            id="${value.id}">${value.toString().encodeAsHTML()}</g:link>
</g:if>
<g:else><span class="text-muted">&mdash;</span></g:else>
