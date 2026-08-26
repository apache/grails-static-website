package demo

import spock.lang.Specification

class PlayerInfoSpec extends Specification {

    def 'test PlayerInfo.region can be null'() {
        expect:
        new PlayerInfo(region: null).validate(['region'])
    }

    def 'test PlayerInfo.name cannot be blank'() {
        when:
        def playerInfo = new PlayerInfo(name: '')

        then:
        !playerInfo.validate(['name'])
        playerInfo.errors['name'].code == 'blank'

        when:
        playerInfo = new PlayerInfo(name: null)

        then:
        !playerInfo.validate(['name'])
        playerInfo.errors['name'].code == 'nullable'
    }

    def 'test PlayerInfo.game cannot be blank'() {
        when:
        def playerInfo = new PlayerInfo(game: '')

        then:
        !playerInfo.validate(['game'])
        playerInfo.errors['game'].code == 'blank'

        when:
        playerInfo = new PlayerInfo(game: null)

        then:
        !playerInfo.validate(['game'])
        playerInfo.errors['game'].code == 'nullable'
    }
}
