package vn.edu.fit.topicmanagement.registrationperiod;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseParticipantRepository extends JpaRepository<CourseParticipant, Long> {
    boolean existsByCourse_IdAndStudent_Id(Long courseId, Long studentId);
    boolean existsByCourse_Id(Long courseId);
    List<CourseParticipant> findByStudent_Id(Long studentId);
}
