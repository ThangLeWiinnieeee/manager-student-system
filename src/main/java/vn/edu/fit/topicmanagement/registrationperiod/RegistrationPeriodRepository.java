package vn.edu.fit.topicmanagement.registrationperiod;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
    List<RegistrationPeriod> findAllByOrderByGvStartDesc();
}