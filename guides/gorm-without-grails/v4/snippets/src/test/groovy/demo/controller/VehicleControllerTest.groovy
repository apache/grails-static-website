package demo.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VehicleControllerTest {

    @Autowired
    WebTestClient webClient

    @Test
    void fetchAudiVehicles() {
        this.webClient.get().uri("/audi/vehicles").exchange().expectBody(String).isEqualTo('["A3","A4"]')
    }
}

