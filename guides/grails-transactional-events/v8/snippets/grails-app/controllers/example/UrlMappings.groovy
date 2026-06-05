package example

class UrlMappings {

    static mappings = {
        '/'(view: '/index')

        '/orders'(controller: 'order', action: 'place', method: 'POST')
        '/assignments'(controller: 'workOrder', action: 'assign', method: 'POST')
        '/workOrderStatus'(controller: 'workOrder', action: 'show', method: 'GET')

        '/ui'(controller: 'workOrderUi', action: 'index', method: 'GET')
        '/ui'(controller: 'workOrderUi', action: 'assign', method: 'POST')

        '500'(view: '/error')
        '404'(view: '/notFound')
    }
}
