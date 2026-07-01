<div class="mb-3 ${invalid ? 'has-error' : ''}">
    <label for="${prefix}${property}" class="form-label">
        ${label}<g:if test="${required}"> <span class="text-danger" aria-hidden="true">*</span></g:if>
    </label>

    <f:widget property="${property}"/>

    <small class="form-text text-muted">
        <span data-counter-for="${prefix}${property}">0</span> / 2000 characters
    </small>

    <g:if test="${invalid}">
        <div class="invalid-feedback d-block">
            <g:each in="${errors}" var="error">
                <div><g:message error="${error}"/></div>
            </g:each>
        </div>
    </g:if>
</div>

<script>
(function () {
    var input = document.getElementById('${prefix}${property}');
    var counter = document.querySelector('[data-counter-for="${prefix}${property}"]');
    if (!input || !counter) return;
    var update = function () { counter.textContent = (input.value || '').length; };
    input.addEventListener('input', update);
    update();
})();
</script>
