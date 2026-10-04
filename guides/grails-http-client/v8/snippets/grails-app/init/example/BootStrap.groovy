package example

class BootStrap {

    def init = { servletContext ->
        environments {
            development {
                RecordLabel.withTransaction {
                    if (RecordLabel.count() == 0) {
                        ['Island Records', 'Motown', 'Blue Note'].each { labelName ->
                            new RecordLabel(name: labelName).save(failOnError: true)
                        }
                    }
                }
            }
        }
    }

    def destroy = {
    }
}
