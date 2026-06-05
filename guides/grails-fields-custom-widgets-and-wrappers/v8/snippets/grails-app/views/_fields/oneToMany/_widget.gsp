<%--
    one-to-many: usually NOT editable on the parent side. Children are
    edited from their own controller (the side that owns the foreign key).
    Render the existing children as a navigable list with quick-add link.
--%>
<g:set var="childClass" value="${persistentProperty.associatedEntity.javaClass}"/>
<g:set var="childController" value="${childClass.simpleName.toLowerCase()}"/>
<g:set var="children" value="${value ?: []}"/>

<div class="border rounded p-2 bg-light">
    <g:if test="${children}">
        <ul class="list-unstyled mb-2">
            <g:each in="${children}" var="child">
                <li>
                    <g:link controller="${childController}"
                            action="show"
                            id="${child.id}">${child.toString().encodeAsHTML()}</g:link>
                </li>
            </g:each>
        </ul>
    </g:if>
    <g:else>
        <p class="text-muted small mb-2">No ${property} yet.</p>
    </g:else>
    <g:link controller="${childController}"
            action="create"
            params="['${persistentProperty.referencedPropertyName}.id': bean?.id]"
            class="btn btn-sm btn-outline-primary">Add ${childClass.simpleName}</g:link>
</div>
