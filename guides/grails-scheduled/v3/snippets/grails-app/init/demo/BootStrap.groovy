package demo

import demo.todayat.DailyMailJobService
import groovy.transform.CompileStatic

@CompileStatic
class BootStrap {

	RegisterService registerService
    DailyMailJobService dailyMailJobService

    def init = { servletContext ->
        final String followUp = 'Follow up - How was your experience'
    	registerService.register('harry@howards.com', followUp)
    	sleep(20_000)
    	registerService.register('ron@howards.com', followUp)
        dailyMailJobService.register('john.daily@example.com', 'Daily Reminder')
    }
    def destroy = {
    }
}
