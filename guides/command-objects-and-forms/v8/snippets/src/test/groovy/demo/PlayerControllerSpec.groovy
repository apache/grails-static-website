// tag::updateSpec[]
package demo

import grails.testing.gorm.DomainUnitTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class PlayerControllerSpec extends Specification implements ControllerUnitTest<PlayerController>, DomainUnitTest<Player> {

    def 'test update'() {
        when:
        def player = new Player(name: 'Sergio', game: 'XCOM: Enemy Unkown', region: 'Spain', wins: 3, losses: 2)
        player.save()
        params.id = player.id
        params.name = 'Sergio del Amo'
        params.game = 'XCOM 2'
        params.region = 'USA'
        params.wins = 4
        controller.update() // <1>

        then: 'respond model has no errors'
        !model.player.hasErrors()

        and: 'player properties have been updated correctly'
        model.player.name == 'Sergio del Amo'
        model.player.game == 'XCOM 2'
        model.player.region == 'USA'

        and: 'non existing properties in the command object have not been modified'
        model.player.wins == 3
        model.player.losses == 2
    }
    // end::updateSpec[]

    // tag::uniqueSpec[]
    def 'test update reports a duplicate name'() {
        given:
        new Player(name: 'Taken', game: 'Chess', region: 'EAST', wins: 1, losses: 0).save(flush: true)
        def player = new Player(name: 'Free', game: 'Go', region: 'WEST', wins: 2, losses: 1).save(flush: true)

        when:
        params.id = player.id
        params.name = 'Taken'
        params.game = 'Go'
        params.region = 'WEST'
        controller.update()

        then:
        view == 'edit'
        model.player.hasErrors()
        model.player.errors['name']?.code == 'unique'
    }
    // end::uniqueSpec[]

// tag::updateSpec[]
}
// end::updateSpec[]
