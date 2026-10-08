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
        <%-- Grails Fields 8 passes only bean, property, and prefix into f:all,
             so required on f:all is ignored. Set it on each child field.
             An optional association must not HTML-require a blank child. --%>
        <g:set var="childBean" value="${value ?: referencedClass.newInstance()}"/>
        <g:set var="childEntity" value="${grailsApplication.mappingContext.getPersistentEntity(referencedClass.name)}"/>
        <f:with bean="${childBean}" prefix="${prefix}${property}.">
            <g:each in="${childEntity.persistentProperties}" var="childProp">
                <g:if test="${!(childProp instanceof org.grails.datastore.mapping.model.types.Association) && !(childProp.name in ['id', 'version', 'dateCreated', 'lastUpdated'])}">
                    <g:if test="${required}">
                        <f:field property="${childProp.name}"/>
                    </g:if>
                    <g:else>
                        <f:field property="${childProp.name}" required="false"/>
                    </g:else>
                </g:if>
            </g:each>
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
