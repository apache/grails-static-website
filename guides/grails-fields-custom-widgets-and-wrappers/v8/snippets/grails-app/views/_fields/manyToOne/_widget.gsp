<%-- many-to-one: render the owning side as a select of all candidates. --%>
<g:set var="referencedClass" value="${persistentProperty.associatedEntity.javaClass}"/>
<g:set var="candidates" value="${referencedClass.list([sort: 'id', order: 'asc'])}"/>
<g:set var="cssClass" value="${invalid ? 'form-select is-invalid' : 'form-select'}"/>

<g:select name="${prefix}${property}.id"
          from="${candidates}"
          optionKey="id"
          optionValue="${{ it.toString() }}"
          value="${value?.id}"
          class="${cssClass}"
          noSelection="${required ? null : ['null': '-- none --']}"
          required="${required}"/>
