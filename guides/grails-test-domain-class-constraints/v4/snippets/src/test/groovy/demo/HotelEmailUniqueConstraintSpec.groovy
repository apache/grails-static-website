package demo

import grails.test.hibernate.HibernateSpec

@SuppressWarnings('MethodName')
class HotelEmailUniqueConstraintSpec extends HibernateSpec {

    List<Class> getDomainClasses() { [Hotel] }

    def "hotel's email unique constraint"() {

        when: 'You instantiate a hotel with name and an email address which has been never used before'
        def hotel = new Hotel(name: 'Hotel Transilvania', email: 'info@hoteltransilvania.com')

        then: 'hotel is valid instance'
        hotel.validate()

        and: 'we can save it, and we get back a not null GORM Entity'
        hotel.save()

        and: 'there is one additional Hotel'
        Hotel.count() == old(Hotel.count()) + 1

        when: 'instanting a different hotel with the same email address'
        def hilton = new Hotel(name: 'Hilton Hotel', email: 'info@hoteltransilvania.com')

        then: 'the hotel instance is not valid'
        !hilton.validate(['email'])

        and: 'unique error code is populated'
        hilton.errors['email']?.code == 'unique'

        and: 'trying to save fails too'
        !hilton.save()

        and: 'no hotel has been added'
        Hotel.count() == old(Hotel.count())
    }
}
