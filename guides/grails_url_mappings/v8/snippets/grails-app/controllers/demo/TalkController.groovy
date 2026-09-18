package demo

class TalkController {

    def index() {
        render "Retrieving all Talks..."
    }

    def save() {
        render "Saving new Talk..."
    }

    def show(Long id) {
        render "Retrieving Talk ${id}..."
    }

    def update(Long id) {
        render "Updating Talk ${id}..."
    }

    def delete(Long id) {
        render "Deleting Talk ${id}..."
    }
}
