package demo

class SpeakerController {

    def index() {
        render "Retrieving all Speakers..."
    }

    def save() {
        render "Saving new Speaker..."
    }

    def show(Long id) {
        render "Retrieving Speaker ${id}..."
    }

    def update(Long id) {
        render "Updating Speaker ${id}..."
    }

    def delete(Long id) {
        render "Deleting Speaker ${id}..."
    }
}
