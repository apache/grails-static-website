package example

class BootStrap {

    def init = { servletContext ->
      // Wrap the seed in a transaction so the writes - including the
      // many-to-many Book<->Tag join rows - flush and commit together.
      // A bare save() in BootStrap runs outside any transaction, and the
      // join table is not reliably persisted; save(flush: true) without a
      // transaction throws TransactionRequiredException.
      Book.withTransaction {
        Tag fantasy = new Tag(name: 'Fantasy').save(failOnError: true)
        Tag classic = new Tag(name: 'Classic').save(failOnError: true)
        Tag romance = new Tag(name: 'Romance').save(failOnError: true)
        Tag adventure = new Tag(name: 'Adventure').save(failOnError: true)

        Author tolkien = new Author(
                name: 'J.R.R. Tolkien',
                email: 'jrr@example.com',
                bio: 'English writer and philologist, best known for The Hobbit and The Lord of the Rings.',
                website: 'https://www.tolkienestate.com/'
        )
        tolkien.contactInfo = new ContactInfo(
                phone: '+44 20 7946 0958',
                mailingAddress: '1 Oxford Way, Oxford, England'
        )
        tolkien.save(failOnError: true)

        Author austen = new Author(
                name: 'Jane Austen',
                email: 'jane@example.com',
                bio: 'English novelist known primarily for her six major novels of the early 19th century.',
                website: null
        )
        austen.contactInfo = new ContactInfo(
                phone: '+44 1256 462100',
                mailingAddress: 'Steventon Rectory, Hampshire, England'
        )
        austen.save(failOnError: true)

        Book hobbit = new Book(
                title: 'The Hobbit',
                isbn: '9780547928227',
                genre: 'Fiction',
                description: 'A reluctant hobbit, Bilbo Baggins, sets out to the Lonely Mountain with a spirited group of dwarves to reclaim their mountain home.',
                publishedDate: Date.parse('yyyy-MM-dd', '1937-09-21'),
                priceUSD: 14.99G,
                inStock: true,
                author: tolkien
        )
        hobbit.addToTags(fantasy).addToTags(adventure).addToTags(classic).save(failOnError: true)

        Book pride = new Book(
                title: 'Pride and Prejudice',
                isbn: '9780141439518',
                genre: 'Fiction',
                description: 'The story of Mr Bennet of Longbourn estate and his five daughters on the lookout for marriage.',
                publishedDate: Date.parse('yyyy-MM-dd', '1813-01-28'),
                priceUSD: 9.99G,
                inStock: true,
                author: austen
        )
        pride.addToTags(romance).addToTags(classic).save(failOnError: true)
      }
    }

    def destroy = {}
}
