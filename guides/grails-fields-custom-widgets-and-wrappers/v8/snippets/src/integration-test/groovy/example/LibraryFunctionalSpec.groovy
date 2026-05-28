package example

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

/**
 * Browser tests for the Fields-plugin UI, driven by Geb 8 against the real
 * booted app. ContainerGebSpec starts a Selenium-Chrome container via
 * Testcontainers, so only a running Docker daemon is required. BootStrap
 * seeds the library at startup, so these read-only specs assert on it.
 *
 * The point is to prove the custom widgets and wrappers actually render -
 * f:table on the index, f:display on the show page (custom isbn widget,
 * description wrapper), and the custom association widgets on the create form.
 */
@Integration
class LibraryFunctionalSpec extends ContainerGebSpec {

    void "the book index renders the custom f:table with seeded rows"() {
        when:
        go '/book/index'

        then:
        title == 'Books'
        $('table.table-striped').size() == 1
        $('table.table-striped thead.table-dark').size() == 1
        $('table.table-striped tbody tr').size() >= 2
        $('table.table-striped tbody').text().contains('The Hobbit')
        $('table.table-striped tbody').text().contains('Pride and Prejudice')
    }

    void "the book show page renders the custom isbn widget and description wrapper"() {
        given: 'the seeded Hobbit id'
        Long id = Book.withNewTransaction { Book.findByTitle('The Hobbit').id }

        when:
        go "/book/show/${id}"

        then: 'f:display renders the value and the custom isbn display includes its formatted output'
        $('body').text().contains('The Hobbit')
        $('body').text().contains('9780547928227')
        $('body').text().contains('A reluctant hobbit')
    }

    void "the book create form renders the custom widgets"() {
        when:
        go '/book/create'

        then: 'the custom isbn widget renders with its pattern and help text'
        $('input', name: 'isbn').size() == 1
        $('input', name: 'isbn').@pattern.contains('\\d{13}')
        $('body').text().contains('10 or 13 digits')

        and: 'the manyToOne author widget renders as a select'
        $('select', name: 'author.id').size() == 1

        and: 'the manyToMany tags widget renders a checkbox per seeded tag'
        $('input', type: 'checkbox', name: 'tags').size() == 4
    }

    void "the author show page renders the one-to-one contactInfo via its custom display"() {
        given:
        Long id = Author.withNewTransaction { Author.findByName('J.R.R. Tolkien').id }

        when:
        go "/author/show/${id}"

        then: 'f:display renders the hasOne ContactInfo through its toString()'
        $('body').text().contains('J.R.R. Tolkien')
        $('body').text().contains('Contact for J.R.R. Tolkien')
    }
}
