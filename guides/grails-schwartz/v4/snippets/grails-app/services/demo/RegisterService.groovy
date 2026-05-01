package demo

import com.agileorbit.schwartz.QuartzService
import com.agileorbit.schwartz.builder.BuilderFactory
import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.TriggerBuilder

import java.text.SimpleDateFormat

@Slf4j
@CompileStatic
class RegisterService {

    QuartzService quartzService

	void register(String email) {
		log.info 'saving {} at {}', email, new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(new Date())
		scheduleFollowupEmail(email)
	}

	@CompileDynamic
	Date startAtDate() { // <1>
		Date startAt = new Date()
		use(TimeCategory) {
			startAt = startAt + 1.minute
		}
		startAt
	}

	void scheduleFollowupEmail(String email) {
		JobDataMap jobDataMap = new JobDataMap()
		jobDataMap.put('email', email)
		Trigger trigger = TriggerBuilder.newTrigger()
				.forJob(JobKey.jobKey(FollowupEmailJobService.simpleName)) // <2>
				.startAt(startAtDate())
				.usingJobData(jobDataMap) // <3>
				.build()
		quartzService.scheduleTrigger(trigger) // <4>
	}
}