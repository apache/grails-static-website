<%--
    many-to-many: render every candidate as a checkbox so the user can
    toggle membership without leaving the form. Spring/Grails data binding
    accepts a multi-valued <input name="<prop>" value="<id>"> form, which is
    what `<g:select multiple>` produces - we use checkboxes for clarity.
--%>
<g:set var="referencedClass" value="${persistentProperty.associatedEntity.javaClass}"/>
<g:set var="candidates" value="${referencedClass.list([sort: 'id', order: 'asc'])}"/>
<g:set var="selectedIds" value="${(value ?: []).collect { it.id } as Set}"/>

<div class="d-flex flex-wrap gap-2 ${invalid ? 'border border-danger rounded p-2' : ''}">
    <g:each in="${candidates}" var="candidate">
        <div class="form-check">
            <input type="checkbox"
                   class="form-check-input"
                   name="${prefix}${property}"
                   id="${prefix}${property}-${candidate.id}"
                   value="${candidate.id}"
                   ${selectedIds.contains(candidate.id) ? 'checked="checked"' : ''}/>
            <label class="form-check-label"
                   for="${prefix}${property}-${candidate.id}">${candidate.toString().encodeAsHTML()}</label>
        </div>
    </g:each>
</div>
