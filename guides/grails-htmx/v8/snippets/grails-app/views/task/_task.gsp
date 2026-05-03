<li id="task-${task.id}" class="list-group-item d-flex align-items-center gap-3${task.done ? ' text-decoration-line-through text-body-secondary' : ''}">

    <%-- Toggle the done flag with a single round trip --%>
    <button type="button"
            class="btn btn-sm ${task.done ? 'btn-success' : 'btn-outline-secondary'}"
            hx-post="${createLink(controller: 'task', action: 'toggle', id: task.id)}"
            hx-target="closest li"
            hx-swap="outerHTML"
            aria-label="Toggle done">
        ${task.done ? '\u2713' : '\u00b7'}
    </button>

    <span class="flex-grow-1"
          hx-get="${createLink(controller: 'task', action: 'editForm', id: task.id)}"
          hx-target="closest li"
          hx-swap="outerHTML"
          role="button" tabindex="0"
          title="Click to edit">
        <g:fieldValue bean="${task}" field="title"/>
    </span>

    <button type="button"
            class="btn btn-sm btn-outline-danger"
            hx-delete="${createLink(controller: 'task', action: 'delete', id: task.id)}"
            hx-target="closest li"
            hx-swap="outerHTML swap:200ms"
            hx-confirm="Delete '${task.title.encodeAsHTML()}'?"
            aria-label="Delete">
        \u2715
    </button>
</li>
