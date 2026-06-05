<div class="form-check">
    <g:checkBox name="${prefix}${property}"
                value="${value}"
                class="form-check-input ${invalid ? 'is-invalid' : ''}"/>
    <label class="form-check-label" for="${prefix}${property}">${label}</label>
</div>
