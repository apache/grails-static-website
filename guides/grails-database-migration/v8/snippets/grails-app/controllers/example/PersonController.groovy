package example

class PersonController {

    static responseFormats = ['json']
    static allowedMethods = [index: 'GET']

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Person.list(params), model: [personCount: Person.count()]
    }
}
