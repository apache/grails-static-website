<li id="task-${task.id}" class="list-group-item d-flex align-items-center gap-3${task.done ? ' text-decoration-line-through text-body-secondary' : ''}">

    <button type="button"
            class="btn btn-sm ${task.done ? 'btn-success' : 'btn-outline-secondary'}"
            hx-post="${createLink(uri: '/tasks/' + task.id + '/toggle')}"
            hx-target="closest li"
            hx-swap="outerHTML"
            aria-pressed="${task.done}"
            aria-label="Toggle done">
        ${task.done ? '\u2713' : '\u00b7'}
    </button>

    <button type="button"
            class="btn btn-link p-0 text-start text-decoration-none flex-grow-1 ${task.done ? 'text-body-secondary' : 'text-body'}"
            hx-get="${createLink(uri: '/tasks/' + task.id + '/edit')}"
            hx-target="closest li"
            hx-swap="outerHTML"
            title="Click to edit">
        <g:fieldValue bean="${task}" field="title"/>
    </button>

    <button type="button"
            class="btn btn-sm btn-outline-danger"
            hx-delete="${createLink(uri: '/tasks/' + task.id)}"
            hx-target="closest li"
            hx-swap="outerHTML swap:200ms"
            hx-confirm="Delete this task?"
            aria-label="Delete">
        \u2715
    </button>
</li>
