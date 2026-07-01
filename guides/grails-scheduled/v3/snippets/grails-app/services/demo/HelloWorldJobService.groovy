package demo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.scheduling.annotation.Scheduled

import java.text.SimpleDateFormat

@Slf4j //<1>
@CompileStatic //<2>
class HelloWorldJobService {

    static lazyInit = false //<3>

    @Scheduled(fixedDelay = 10000L) //<4>
    void executeEveryTen() {
        log.info "Simple Job every 10 seconds :{}", new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(new Date())
    }

    @Scheduled(fixedDelay = 45000L, initialDelay = 5000L) //<5>
    void executeEveryFourtyFive() {
        log.info "Simple Job every 45 seconds :{}", new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(new Date())
    }
}
