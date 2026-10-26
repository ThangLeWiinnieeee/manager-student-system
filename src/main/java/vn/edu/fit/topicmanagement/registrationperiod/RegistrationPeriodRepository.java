package vn.edu.fit.topicmanagement.registrationperiod;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RegistrationPeriodRepository extends JpaRepository<RegistrationPeriod, Long> {
    List<RegistrationPeriod> findAllByOrderByGvStartDesc();
    @Query("select p.id from RegistrationPeriod p where "
            + "(p.gvNoticeSent = false and p.gvStart <= :now and p.gvEnd > :now) or "
            + "(p.svNoticeSent = false and p.svStart <= :now and p.gvEnd <= :now and p.svEnd >= :now) order by p.id")
    List<Long> findIdsAwaitingNotice(@Param("now") java.time.LocalDateTime now);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from RegistrationPeriod p where p.id = :id")
    Optional<RegistrationPeriod> findLockedById(@Param("id") Long id);
}
