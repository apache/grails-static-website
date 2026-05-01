package demo.todayat

//tag::dailyEmailJobService[]
import com.agileorbit.schwartz.SchwartzJob
import demo.EmailService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import static org.quartz.DateBuilder.todayAt
import static org.quartz.DateBuilder.tomorrowAt

@CompileStatic
@Slf4j
class DailyEmailJobService implements SchwartzJob {
    final int HOUR = 4
    final int MINUTE = 30
    final int SECONDS = 0

    EmailService emailService // <1>

    void execute(JobExecutionContext context) throws JobExecutionException {
        emailService.send('john.doe@example.com') // <2>
    }

    Date dailyDate() {
        Date startAt = todayAt(HOUR, MINUTE, SECONDS)
        if (startAt.before(new Date())) {
            return tomorrowAt(HOUR, MINUTE, SECONDS)
        }
        startAt
    }

    void buildTriggers() {
        Date startAt = dailyDate()
        triggers << factory('Daily email job')
                .startAt(startAt)
                .intervalInDays(1)
                .build()

    }
}
//end::dailyEmailJobService[]