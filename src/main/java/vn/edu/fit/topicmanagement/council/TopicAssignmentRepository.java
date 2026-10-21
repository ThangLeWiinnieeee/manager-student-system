package vn.edu.fit.topicmanagement.council;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicAssignmentRepository extends JpaRepository<TopicAssignment, Long> {

    Optional<TopicAssignment> findByTopic_Id(Long topicId);

    List<TopicAssignment> findByCouncil_Id(Long councilId);

    List<TopicAssignment> findByReviewer_Id(Long reviewerId);

    boolean existsByTopic_Id(Long topicId);
}
