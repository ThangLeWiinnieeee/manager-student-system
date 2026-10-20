package vn.edu.fit.topicmanagement.evaluation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    Optional<Evaluation> findByTopic_IdAndEvaluator_Id(Long topicId, Long evaluatorId);

    List<Evaluation> findByTopic_Id(Long topicId);

    List<Evaluation> findByEvaluator_Id(Long evaluatorId);

    long countByTopic_IdAndStatus(Long topicId, EvaluationStatus status);
}
