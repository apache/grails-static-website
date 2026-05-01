package demo

import grails.gorm.transactions.Transactional

class GenerateInvoiceJobService {
    final int HOUR = 7
    final int MINUTE = 0
    final int SECONDS = 0

    GenerateInvoiceService generateInvoiceService

    @Transactional // <1>
    void execute() {
        println "I AM THE POST CONSTRUCT 2"
    }
//    void execute(JobExecutionContext context) throws JobExecutionException {
//        generateInvoiceService.generateInvoices()
//    }
//
//    @Override
//    void buildTriggers() {
//        Date startAt = dailyDate()
//        triggers << factory('Daily email job at 7:00 AM')
//                .startAt(startAt)
//                .intervalInDays(1)
//                .build()
//
//    }


    Date dailyDate() {
        Date startAt = new Date(hours: HOUR, minutes: MINUTE, seconds: SECONDS)
        if(startAt.before(new Date())) {
            return startAt + 1
        }
        startAt
    }

}
