package demo

import com.agileorbit.schwartz.SchwartzJob
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

//tag::dailyEmailJobService[]
@CompileStatic
@Slf4j
class DailyEmailJobService implements SchwartzJob {

    EmailService emailService // <1>

    void execute(JobExecutionContext context) throws JobExecutionException {
        emailService.send('john.doe@example.com') // <2>
    }

    void buildTriggers() {
        triggers << factory('Daily email job')
            .cronSchedule("0 30 4 1/1 * ? *") // <3>
            .build()
    }
}
//end::dailyEmailJobService[]