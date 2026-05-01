package demo.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ManufacturerControllerTest {

    @Autowired
    WebTestClient webClient

    @Test
    void fetchManufacturer() {
        this.webClient.get().uri("/").exchange().expectBody(String).isEqualTo('["audi","ford"]')
    }
}

