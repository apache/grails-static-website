package example

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration

/**
 * Browser tests for the Fields-plugin UI, driven by Geb 8 against the real
 * booted app. ContainerGebSpec starts a Selenium-Chrome container via
 * Testcontainers, so only a running Docker daemon is required. BootStrap
 * seeds the library at startup, so these read-only specs assert on it.
 *
 * The point is to prove the custom widgets and wrappers actually render on
 * EVERY scaffolded page of EVERY domain - not just Book. The app-wide
 * f:table template and the per-type widgets compile cleanly and only fail
 * when a real request renders them, so each controller action gets a spec.
 */
@Integration
class LibraryFunctionalSpec extends ContainerGebSpec {

    // ---- Book pages -------------------------------------------------------

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

    void "the book edit form is pre-filled and keeps the optimistic-lock version field"() {
        given:
        Long id = Book.withNewTransaction { Book.findByTitle('The Hobbit').id }

        when:
        go "/book/edit/${id}"

        then: 'the custom isbn widget is pre-filled with the persisted value and still carries its pattern'
        $('input', name: 'isbn').value() == '9780547928227'
        $('input', name: 'isbn').@pattern.contains('\\d{13}')

        and: 'the hidden version field is present so stale updates are rejected'
        $('input', name: 'version').size() == 1
    }

    // ---- Author pages -----------------------------------------------------

    void "the author index renders the custom f:table without crashing on a title-less domain"() {
        when: 'rendering the app-wide f:table for a domain class that has no title property'
        go '/author/index'

        then: 'the shared template must be domain-agnostic - this is the regression guard for the book.title bug'
        title == 'Authors'
        $('table.table-striped').size() == 1
        $('table.table-striped thead.table-dark').size() == 1
        $('table.table-striped tbody tr').size() >= 2
        $('table.table-striped tbody').text().contains('J.R.R. Tolkien')
        $('table.table-striped tbody').text().contains('Jane Austen')
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

    void "the author create form renders the email, url and textarea widgets from constraints"() {
        when:
        go '/author/create'

        then: 'the String widget dispatcher routes email/url/textarea by constraint'
        $('input', name: 'email', type: 'email').size() == 1
        $('input', name: 'website', type: 'url').size() == 1
        $('textarea', name: 'bio').size() == 1
    }

    void "the author edit form is pre-filled from the persisted author"() {
        given:
        Long id = Author.withNewTransaction { Author.findByName('Jane Austen').id }

        when:
        go "/author/edit/${id}"

        then:
        $('input', name: 'name').value() == 'Jane Austen'
        $('input', name: 'email', type: 'email').value() == 'jane@example.com'
        $('input', name: 'version').size() == 1
    }

    // ---- Mutating actions (save / delete) through the UI ------------------

    void "creating an author through the form persists it and redirects to show"() {
        given:
        int before = Author.withNewTransaction { Author.count() }

        when: 'filling and submitting the create form'
        go '/author/create'
        $('input', name: 'name').value('Mary Shelley')
        $('input', name: 'email', type: 'email').value('mary@example.com')
        $('textarea', name: 'bio') << 'English novelist who wrote Frankenstein.'
        $('button', type: 'submit').click()

        then: 'the save action persisted the row and the show page renders it'
        waitFor { $('body').text().contains('Mary Shelley') }
        Author.withNewTransaction { Author.count() } == before + 1
    }

    void "editing an author through the form updates the persisted row"() {
        given: 'a throwaway author to edit'
        Long id = Author.withNewTransaction {
            new Author(name: 'Temp Author', email: 'temp@example.com', bio: 'temp bio')
                    .save(flush: true, failOnError: true).id
        }

        when: 'changing the name on the edit form and submitting (update action)'
        go "/author/edit/${id}"
        $('input', name: 'name').value('Renamed Author')
        $('button', type: 'submit').click()

        then:
        waitFor { Author.withNewTransaction { Author.get(id).name == 'Renamed Author' } }
    }

    void "deleting a book through the custom f:table delete form removes the row (delete action)"() {
        given: 'a throwaway book that sorts last (title asc) so we delete it, not the seed'
        Long id = Book.withNewTransaction {
            Author a = Author.findByName('Jane Austen')
            new Book(title: 'Throwaway', isbn: '9780000000001', genre: 'Fiction',
                    description: 'temp', publishedDate: new Date(), priceUSD: 1.00G,
                    inStock: true, author: a).save(flush: true, failOnError: true).id
        }

        when: 'submitting the last row\'s DELETE form (g:form method=DELETE renders as POST + _method), bypassing the JS confirm guard'
        go '/book/index'
        js.exec('var b = document.querySelectorAll("button.btn-outline-danger"); b[b.length - 1].closest("form").submit();')

        then:
        waitFor { Book.withNewTransaction { Book.get(id) == null } }
    }
}
