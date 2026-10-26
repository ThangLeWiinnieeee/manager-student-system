package vn.edu.fit.topicmanagement.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.registration-notices.enabled", havingValue = "true", matchIfMissing = true)
public class RegistrationNoticeScheduler {
    private final RegistrationNoticeService notices;

    public RegistrationNoticeScheduler(RegistrationNoticeService notices) {
        this.notices = notices;
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void publish() {
        notices.publishOpenPeriods();
    }
}
