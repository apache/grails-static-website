<div class="mb-3 ${invalid ? 'has-error' : ''}">
    <label for="${prefix}${property}" class="form-label">
        ${label}<g:if test="${required}"> <span class="text-danger" aria-hidden="true">*</span></g:if>
    </label>

    ${widget}

    <g:if test="${invalid}">
        <div class="invalid-feedback d-block">
            <g:each in="${errors}" var="error">
                <div><g:message error="${error}"/></div>
            </g:each>
        </div>
    </g:if>
</div>
