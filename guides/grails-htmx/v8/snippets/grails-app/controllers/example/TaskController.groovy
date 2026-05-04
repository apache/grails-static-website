package example

import grails.gorm.transactions.Transactional
import org.springframework.http.HttpStatus

@Transactional(readOnly = true)
class TaskController {

    static allowedMethods = [
            create: 'POST', update: 'PATCH', delete: 'DELETE', toggle: 'POST'
    ]

    def index() {
        List<Task> tasks = Task.list(params)
        render view: 'index', model: [tasks: tasks, task: new Task(), q: '']
    }

    def show(Long id) {
        Task task = Task.get(id)
        if (!task) {
            response.status = HttpStatus.NOT_FOUND.value()
            return
        }
        render template: 'task', model: [task: task]
    }

    def search() {
        String q = (params.q ?: '').trim()
        List<Task> rows = q ? Task.findAllByTitleIlike("%${q}%") : Task.list()
        render template: 'taskRows', model: [tasks: rows]
    }

    @Transactional
    def create() {
        Task task = new Task(title: params.title)
        if (!task.validate()) {
            response.status = HttpStatus.UNPROCESSABLE_ENTITY.value()
            render template: 'taskForm', model: [task: task]
            return
        }
        task.save(flush: true)
        render template: 'taskCreated', model: [task: task, formTask: new Task()]
    }

    def editForm(Long id) {
        Task task = Task.get(id)
        if (!task) {
            response.status = HttpStatus.NOT_FOUND.value()
            return
        }
        render template: 'taskEdit', model: [task: task]
    }

    @Transactional
    def update(Long id) {
        Task task = Task.get(id)
        if (!task) {
            response.status = HttpStatus.NOT_FOUND.value()
            return
        }
        task.title = params.title
        if (!task.save(flush: true)) {
            response.status = HttpStatus.UNPROCESSABLE_ENTITY.value()
            render template: 'taskEdit', model: [task: task]
            return
        }
        render template: 'task', model: [task: task]
    }

    @Transactional
    def toggle(Long id) {
        Task task = Task.get(id)
        if (!task) {
            response.status = HttpStatus.NOT_FOUND.value()
            return
        }
        task.done = !task.done
        task.save(flush: true)
        render template: 'task', model: [task: task]
    }

    @Transactional
    def delete(Long id) {
        Task task = Task.get(id)
        if (!task) {
            response.status = HttpStatus.NOT_FOUND.value()
            return
        }
        task.delete(flush: true)
        render ''
    }
}
