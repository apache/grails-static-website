package demo.todayat

//tag::dailyEmailJobService[]
import demo.EmailService
import demo.EmailTask
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.lang.time.DateUtils
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.time.Duration

@Slf4j
@CompileStatic
class DailyMailJobService {
    private final int HOUR = 4
    private final int MINUTE = 30
    private final int SECONDS = 0
    private static final long MILLISECONDS_IN_DAY = Duration.ofDays(1).getSeconds() * 1000 //<4>

    ThreadPoolTaskScheduler threadPoolTaskScheduler // <1>
    EmailService emailService // <2>

    void register(String email, String message) {
        scheduleDailyEmail(email, message)
    }

    void scheduleDailyEmail(String email, String message) { //<5>
        threadPoolTaskScheduler.scheduleAtFixedRate(new EmailTask(emailService, email, message), dailyDate(), MILLISECONDS_IN_DAY)
    }

    Date dailyDate() { //<3>
        Date startAt = new Date(hours: HOUR, minutes: MINUTE, seconds: SECONDS)
        if(startAt.before(new Date())) {
            return (DateUtils.addDays(startAt, 1))
        }
        startAt
    }
}
//end::dailyEmailJobService[]