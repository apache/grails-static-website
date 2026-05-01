package demo

import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.text.SimpleDateFormat

@Slf4j
@CompileStatic
class RegisterService {

	ThreadPoolTaskScheduler threadPoolTaskScheduler // <1>
	EmailService emailService // <2>

	void register(String email, String message) {
		log.info 'saving {} at {}', email, new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(new Date())
		scheduleFollowupEmail(email, message)
	}

	@CompileDynamic
	Date startAtDate() { // <3>
		Date startAt = new Date()
		use(TimeCategory) {
			startAt = startAt + 1.minute
		}
		startAt
	}

	void scheduleFollowupEmail(String email, String message) {
		threadPoolTaskScheduler.schedule(new EmailTask(emailService, email, message), startAtDate()) // <4>
	}
}