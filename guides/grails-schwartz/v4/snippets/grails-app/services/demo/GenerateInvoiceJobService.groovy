package demo

import com.agileorbit.schwartz.StatefulSchwartzJob
import grails.gorm.transactions.Transactional
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import static org.quartz.DateBuilder.todayAt
import static org.quartz.DateBuilder.tomorrowAt

class GenerateInvoiceJobService implements StatefulSchwartzJob {
    final int HOUR = 7
    final int MINUTE = 0
    final int SECONDS = 0

    GenerateInvoiceService generateInvoiceService

    @Transactional // <1>
    @Override
    void execute(JobExecutionContext context) throws JobExecutionException {
        generateInvoiceService.generateInvoices()
    }

    @Override
    void buildTriggers() {
        Date startAt = dailyDate()
        triggers << factory('Daily email job at 7:00 AM')
                .startAt(startAt)
                .intervalInDays(1)
                .build()

    }


    Date dailyDate() {
        Date startAt = todayAt(HOUR, MINUTE, SECONDS)
        if (startAt.before(new Date())) {
            return tomorrowAt(HOUR, MINUTE, SECONDS)
        }
        startAt
    }

}