// tag::indexShow[]
package demo

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import org.springframework.http.HttpStatus

@ReadOnly
class PlayerController {

    def index() {
        respond Player.list(params), model: [playerCount: Player.count()]
    }

    def show(Player player) {
        respond player
    }
    // end::indexShow[]

    //tag::create[]
    @SuppressWarnings(['FactoryMethodName', 'GrailsMassAssignment'])
    def create() {
        respond new Player(params)
    }
    //end::create[]

    //tag::save-full[]
    //tag::save[]
    @Transactional
    def save(Player player) {
        //end::save[]
        //tag::save-handleErrors[]
        if (player == null) {
            render status: HttpStatus.NOT_FOUND
            return
        }

        if (player.hasErrors()) {
            respond player.errors, view: 'create'
            return
        }
        //end::save-handleErrors[]

        player.save flush: true

        request.withFormat {
            form multipartForm { redirect player }
            '*' { respond player, status: HttpStatus.CREATED }
        }
        //tag::save[]
    }
    //end::save[]
    //end::save-full[]

    def edit(Player player) {
        respond player
    }

    //tag::update[]
    @Transactional
    def update(PlayerInfo info) {
        if (info.hasErrors()) {
            respond info.errors, view: 'edit'
            return
        }
        Player player = Player.get(params.id)
        if (player == null) {
            render status: HttpStatus.NOT_FOUND
            return
        }

        player.properties = info.properties
        player.save flush: true

        request.withFormat {
            form multipartForm { redirect player }
            '*' { respond player, status: HttpStatus.OK }
        }
    }
    //end::update[]

//tag::indexShow[]
}
//end::indexShow[]

//tag::playerInfo[]
class PlayerInfo {
    String name
    String game
    String region
//end::playerInfo[]
    //tag::playerInfo-constraints[]

    static constraints = {
        name blank: false
        game blank: false
        region nullable: true
    }
    //end::playerInfo-constraints[]
//tag::playerInfo[]
}
//end::playerInfo[]
