<%-- Date widget: HTML5 date picker, ISO-8601 value. --%>
<g:set var="iso" value="${value ? value.format('yyyy-MM-dd') : ''}"/>
<g:field type="date"
         name="${prefix}${property}"
         value="${iso}"
         class="${invalid ? 'form-control is-invalid' : 'form-control'}"
         required="${required}"/>
