package example

import grails.gorm.transactions.Transactional
import org.springframework.http.HttpStatus

@Transactional(readOnly = true)
class TaskController {

    static allowedMethods = [
            create: 'POST', update: 'PATCH', delete: 'DELETE', toggle: 'POST'
    ]

    /** Full page (index.gsp). */
    def index() {
        respond Task.list(params), model: [tasks: Task.list(params), q: '']
    }

    /** Live-search endpoint - returns just the rows partial. */
    def search() {
        String q = (params.q ?: '') as String
        List<Task> rows = q ? Task.findAllByTitleIlike("%${q}%") : Task.list()
        render template: 'taskRows', model: [tasks: rows]
    }

    /** Add a new task - returns the new row prepended to the list. */
    @Transactional
    def create() {
        Task t = new Task(title: params.title)
        if (!t.save()) {
            response.status = HttpStatus.UNPROCESSABLE_ENTITY.value()
            render template: 'taskForm', model: [task: t]
            return
        }
        render template: 'task', model: [task: t]
    }

    /** Inline-edit endpoint - returns the edit form for a single row. */
    def editForm(Long id) {
        Task t = Task.get(id)
        if (!t) { response.status = 404; return }
        render template: 'taskEdit', model: [task: t]
    }

    /** Apply an inline edit - returns the read-only row partial. */
    @Transactional
    def update(Long id) {
        Task t = Task.get(id)
        if (!t) { response.status = 404; return }
        t.properties = params
        if (!t.save()) {
            response.status = HttpStatus.UNPROCESSABLE_ENTITY.value()
            render template: 'taskEdit', model: [task: t]
            return
        }
        render template: 'task', model: [task: t]
    }

    /** Toggle the `done` flag - returns the row partial. */
    @Transactional
    def toggle(Long id) {
        Task t = Task.get(id)
        if (!t) { response.status = 404; return }
        t.done = !t.done
        t.save()
        render template: 'task', model: [task: t]
    }

    /** Delete - HTMX swaps the row out via hx-target="closest tr" hx-swap="outerHTML". */
    @Transactional
    def delete(Long id) {
        Task t = Task.get(id)
        if (!t) { response.status = 404; return }
        t.delete()
        render ''
    }
}
