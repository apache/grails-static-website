package demo

import com.agileorbit.schwartz.SchwartzJob
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

import java.text.SimpleDateFormat

@Slf4j
@CompileStatic
class FollowupEmailJobService implements SchwartzJob {

    void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobDataMap = context.mergedJobDataMap
		String email = jobDataMap.getString('email') // <1>
		log.info 'Sending followup email to: {} at {}', email, new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(new Date())
	}

	@Override
	void buildTriggers() {

	}
}