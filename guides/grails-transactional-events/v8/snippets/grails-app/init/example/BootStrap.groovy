package example

class BootStrap {

    def init = { servletContext ->
        if (Customer.count() == 0) {
            new Customer(name: 'Alice Wong', email: 'alice@example.com').save(failOnError: true)
            new Customer(name: 'Bob Chen',   email: 'bob@example.com').save(failOnError: true)
        }
    }

    def destroy = { }
}
