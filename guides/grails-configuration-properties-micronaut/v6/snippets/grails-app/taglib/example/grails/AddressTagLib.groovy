package example.grails

import org.springframework.beans.factory.annotation.Autowired

class AddressTagLib {

    static namespace = "app" // <1>

    @Autowired // <2>
    AddressConfiguration addressConfiguration // <3>

    def address = { attrs, body ->
        out << """\
<div class='adr'>
    <div class='street-address'>${addressConfiguration.street}</div>
    <span class='locality'>${addressConfiguration.city}</span>,
    <div class='country-name'>${addressConfiguration.country}</div>
</div>""" // <4>
    }
}
