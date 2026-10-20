package vn.edu.fit.topicmanagement.result;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicFinalResultRepository extends JpaRepository<TopicFinalResult, Long> {

    Optional<TopicFinalResult> findByTopic_Id(Long topicId);

    List<TopicFinalResult> findByCouncil_Id(Long councilId);

    @Query("SELECT r FROM TopicFinalResult r WHERE r.topic.round.id = :roundId ORDER BY r.finalScore DESC NULLS LAST")
    List<TopicFinalResult> findByRoundId(@Param("roundId") Long roundId);

    List<TopicFinalResult> findByPublishedTrue();
}
