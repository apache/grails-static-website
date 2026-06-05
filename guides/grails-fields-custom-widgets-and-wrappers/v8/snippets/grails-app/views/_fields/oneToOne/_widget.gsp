<%--
    one-to-one: identical wire format to many-to-one (a single foreign-key
    select), but rendered as a list-group of read-only fields when the
    target side is owned by the parent (belongsTo).
--%>
<g:set var="referencedClass" value="${persistentProperty.associatedEntity.javaClass}"/>
<g:set var="ownedSide" value="${persistentProperty.bidirectional && persistentProperty.inverseSide?.owningSide}"/>

<g:if test="${ownedSide}">
    <%-- The associated record is owned by this bean. Render its editable
         fields inline with the parent prefix so binding flows through. --%>
    <fieldset class="border rounded p-3 mb-2">
        <legend class="float-none w-auto fs-6 px-2 text-muted">
            <g:message code="${referencedClass.simpleName.toLowerCase()}.label"
                       default="${referencedClass.simpleName}"/>
        </legend>
        <f:with bean="${value ?: referencedClass.newInstance()}" prefix="${prefix}${property}.">
            <f:all/>
        </f:with>
    </fieldset>
</g:if>
<g:else>
    <%-- Independent record: behave like many-to-one and select an existing one. --%>
    <g:select name="${prefix}${property}.id"
              from="${referencedClass.list()}"
              optionKey="id"
              optionValue="${{ it.toString() }}"
              value="${value?.id}"
              class="${invalid ? 'form-select is-invalid' : 'form-select'}"
              noSelection="${required ? null : ['null': '-- none --']}"
              required="${required}"/>
</g:else>
