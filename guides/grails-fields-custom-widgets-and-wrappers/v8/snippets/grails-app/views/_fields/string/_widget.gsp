<%-- String widget dispatcher: routes to the right input based on constraints. --%>
<g:set var="cssClass" value="${invalid ? 'form-control is-invalid' : 'form-control'}"/>

<g:if test="${constraints?.inList}">
    <g:select name="${prefix}${property}"
              from="${constraints.inList}"
              value="${value}"
              class="${invalid ? 'form-select is-invalid' : 'form-select'}"
              noSelection="${required ? null : ['': '-- choose --']}"
              required="${required}"/>
</g:if>
<g:elseif test="${constraints?.email}">
    <g:field type="email"
             name="${prefix}${property}"
             value="${value}"
             class="${cssClass}"
             required="${required}"/>
</g:elseif>
<g:elseif test="${constraints?.url}">
    <g:field type="url"
             name="${prefix}${property}"
             value="${value}"
             class="${cssClass}"
             required="${required}"/>
</g:elseif>
<g:elseif test="${constraints?.password}">
    <g:passwordField name="${prefix}${property}"
                     value="${value}"
                     class="${cssClass}"
                     required="${required}"/>
</g:elseif>
<g:elseif test="${constraints?.widget == 'textarea'}">
    <g:textArea name="${prefix}${property}"
                class="${cssClass}"
                rows="4"
                required="${required}">${value}</g:textArea>
</g:elseif>
<g:else>
    <g:textField name="${prefix}${property}"
                 value="${value}"
                 class="${cssClass}"
                 maxlength="${constraints?.maxSize ?: ''}"
                 required="${required}"/>
</g:else>
