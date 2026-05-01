package demo

class AgendaController {

    def index() {
        render "Retrieving all Agendas..."
    }

    def save() {
        render "Saving new Agenda..."
    }

    def show(Long id) {
        render "Retrieving Agenda ${id}..."
    }

    def update(Long id) {
        render "Updating Agenda ${id}..."
    }

    def delete(Long id) {
        render "Deleting Agenda ${id}..."
    }
}
