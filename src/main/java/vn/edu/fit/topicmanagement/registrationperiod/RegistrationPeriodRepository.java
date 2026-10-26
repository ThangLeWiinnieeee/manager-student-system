package vn.edu.fit.topicmanagement.registrationperiod;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
    List<RegistrationPeriod> findAllByOrderByGvStartDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from RegistrationPeriod p where p.id = :id")
    Optional<RegistrationPeriod> findLockedById(@Param("id") Long id);
}
