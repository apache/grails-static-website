package example

class UrlMappings {

    static mappings = {

        "/tasks"(controller: 'task', action: 'index', method: 'GET')
        "/tasks"(controller: 'task', action: 'create', method: 'POST')
        "/tasks/search"(controller: 'task', action: 'search', method: 'GET')
        "/tasks/$id"(controller: 'task', action: 'show', method: 'GET')
        "/tasks/$id/edit"(controller: 'task', action: 'editForm', method: 'GET')
        "/tasks/$id"(controller: 'task', action: 'update', method: 'PATCH')
        "/tasks/$id"(controller: 'task', action: 'delete', method: 'DELETE')
        "/tasks/$id/toggle"(controller: 'task', action: 'toggle', method: 'POST')

        "/"(redirect: '/tasks')

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
