<div id="taskFormContainer">
    <g:form controller="task"
            action="create"
            class="d-flex gap-2 mb-3 align-items-start"
            hx-post="${createLink(controller: 'task', action: 'create')}"
            hx-target="#taskFormContainer"
            hx-swap="outerHTML">
        <div class="flex-grow-1">
            <g:textField name="title"
                         value="${task?.title}"
                         required="required"
                         maxlength="255"
                         placeholder="What needs doing?"
                         class="form-control"/>
            <g:hasErrors bean="${task}" field="title">
                <ul class="text-danger small mt-2 mb-0 list-unstyled">
                    <g:eachError bean="${task}" field="title">
                        <li>${message(error: it).encodeAsHTML()}</li>
                    </g:eachError>
                </ul>
            </g:hasErrors>
        </div>
        <button type="submit" class="btn btn-primary">Add</button>
    </g:form>
</div>
