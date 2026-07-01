package demo

class TrixTagLib {
    static namespace = 'trix' // <1>
    static defaultEncodeAs = [taglib: 'text'] // <2>

    def editor = { attrs, body ->
        def id = attrs.id ?: attrs.name

        out << "<input id=\"${id}\" type=\"hidden\" name=\"${attrs.name}\""
        if ( attrs.value ) {
            out << " value=\"${attrs.value.encodeAsHTML()}\""  // <2>
        }
        out << ' />'
        out << "<trix-editor input=\"${id}\"></trix-editor>"
    }
}
