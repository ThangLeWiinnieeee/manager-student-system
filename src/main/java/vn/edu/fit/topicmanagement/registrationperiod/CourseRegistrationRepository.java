package vn.edu.fit.topicmanagement.registrationperiod;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRegistrationRepository extends JpaRepository<CourseRegistration, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from CourseRegistration r where r.id = :id")
    java.util.Optional<CourseRegistration> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);
    List<CourseRegistration> findAllByOrderByPeriod_GvStartDesc();

    @org.springframework.data.jpa.repository.Query("select r.id from CourseRegistration r where r.joinCode = :code")
    java.util.Optional<Long> findIdByJoinCode(@org.springframework.data.repository.query.Param("code") String code);

    @org.springframework.data.jpa.repository.Query("select r.period.id from CourseRegistration r where r.id = :id")
    java.util.Optional<Long> findPeriodIdById(@org.springframework.data.repository.query.Param("id") Long id);

    boolean existsByPeriod_Id(Long periodId);
    List<CourseRegistration> findByPeriod_IdOrderByCourseNameAsc(Long periodId);
    List<CourseRegistration> findByPeriod_IdAndCourseNameIgnoreCaseAndClassCodeIgnoreCase(Long periodId, String courseName, String classCode);

    @org.springframework.data.jpa.repository.Query("""
            select count(r) > 0 from CourseRegistration r where r.id = :id and (
                exists (select t.id from Topic t where t.round.id = r.id)
                or exists (select g.id from StudentGroup g where g.roundId = r.id)
                or exists (select c.id from Council c where c.round.id = r.id))
            """)
    boolean hasRelatedData(@org.springframework.data.repository.query.Param("id") Long id);
}
