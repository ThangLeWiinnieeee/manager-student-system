package vn.edu.fit.topicmanagement.council;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicAssignmentRepository extends JpaRepository<TopicAssignment, Long> {

    Optional<TopicAssignment> findByRegistration_Id(Long registrationId);

    List<TopicAssignment> findByCouncil_Id(Long councilId);

    List<TopicAssignment> findBySupporter_Id(Long supporterId);

    List<TopicAssignment> findByReviewer_Id(Long reviewerId);

    boolean existsByRegistration_Id(Long registrationId);
    List<TopicAssignment> findByTopic_Id(Long topicId);
}
