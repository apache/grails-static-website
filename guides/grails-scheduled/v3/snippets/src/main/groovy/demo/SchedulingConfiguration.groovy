package demo

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@CompileStatic
@Configuration //<1>
class SchedulingConfiguration {

    @Bean
    ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler()
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler") //<2>
        threadPoolTaskScheduler.initialize()
        threadPoolTaskScheduler
    }
}
