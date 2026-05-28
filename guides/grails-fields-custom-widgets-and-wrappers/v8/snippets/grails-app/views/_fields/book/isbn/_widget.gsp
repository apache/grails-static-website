<g:textField name="${prefix}${property}"
             value="${value}"
             class="${invalid ? 'form-control is-invalid' : 'form-control'}"
             pattern="(?:\\d{10}|\\d{13}|\\d{3}-\\d-\\d{2}-\\d{6}-\\d)"
             maxlength="20"
             placeholder="ISBN-10 or ISBN-13"
             required="${required}"/>
<small class="form-text text-muted">10 or 13 digits, with or without hyphens.</small>
