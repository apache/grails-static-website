package demo

import org.springframework.boot.ApplicationArguments
import demo.init.BootStrap
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration

@CompileStatic
@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class) // <1>
class Application implements ApplicationRunner {

    @Autowired
    BootStrap bootStrap

    static void main(String[] args) {
        SpringApplication.run Application, args
    }

    void run(ApplicationArguments args) throws Exception {
        bootStrap.init()
    }
}
