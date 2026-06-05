package example

class BootStrap {

    def init = { servletContext ->
        // Seed a few books so the /books page (and the Geb functional test)
        // has rows to render. ISBNs are distinct from those the specs use.
        if (Book.count() == 0) {
            new Book(title: 'The Hobbit',  isbn: '9780261103344', pageCount: 310).save(failOnError: true)
            new Book(title: 'Dune',        isbn: '9780441013593', pageCount: 412).save(failOnError: true)
            new Book(title: 'Neuromancer', isbn: '9780441569595', pageCount: 271).save(failOnError: true)
        }
    }

    def destroy = {
    }

}