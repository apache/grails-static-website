<li id="task-${task.id}" class="list-group-item">
    <form hx-patch="${createLink(controller: 'task', action: 'update', id: task.id)}"
          hx-target="closest li"
          hx-swap="outerHTML"
          class="d-flex gap-2">
        <input type="text"
               name="title"
               value="${task.title.encodeAsHTML()}"
               class="form-control"
               autofocus/>
        <button type="submit" class="btn btn-primary btn-sm">Save</button>
        <button type="button"
                class="btn btn-outline-secondary btn-sm"
                hx-get="${createLink(controller: 'task', action: 'show', id: task.id)}"
                hx-target="closest li"
                hx-swap="outerHTML">Cancel</button>
    </form>
    <g:if test="${task.errors?.hasErrors()}">
        <ul class="text-danger small mt-2 mb-0">
            <g:eachError bean="${task}"><li>${message(error: it).encodeAsHTML()}</li></g:eachError>
        </ul>
    </g:if>
</li>
