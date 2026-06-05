package demo

@SuppressWarnings('DuplicateNumberLiteral')
class Hotel {
    String name
    String url
    String email
    String about
    BigDecimal latitude
    BigDecimal longitude

    static constraints = {
        name blank: false, maxSize: 255
        url nullable: true, url: true, maxSize: 255
        email nullable: true, email: true, unique: true
        about nullable: true
        // tag::latitudeCustomValidator[]
        latitude nullable: true, validator: { val, obj, errors ->
            if ( val == null ) {
                return true
            }
            if (val < -90.0) {
                errors.rejectValue('latitude', 'range.toosmall')
                return false
            }
            if (val > 90.0) {
                errors.rejectValue('latitude', 'range.toobig')
                return false
            }
            true
        }
        // end::latitudeCustomValidator[]
        longitude nullable: true, validator: { val, obj, errors ->
            if ( val == null ) {
                return true
            }
            if (val < -180.0) {
                errors.rejectValue('longitude', 'range.toosmall')
                return false
            }
            if (val > 180.0) {
                errors.rejectValue('longitude', 'range.toobig')
                return false
            }
            true
        }
    }

    // tag::hotelMapping[]
    static mapping = {
        about type: 'text'
    }
    // end::hotelMapping[]
}
