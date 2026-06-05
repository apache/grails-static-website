package demo

class ConferenceController {

    def talks(Long id) {
        if(id) {
            render "Returning conference talk ${id}..."
        } else {
            render "Returning conference talks..."
        }

    }

    def speakers(Long id) {
        if(id) {
            render "Returning conference speaker ${id}..."
        } else {
            render "Returning conference speakers..."
        }
    }

    def agenda() {
        render "Returning conference agenda..."
    }
}
