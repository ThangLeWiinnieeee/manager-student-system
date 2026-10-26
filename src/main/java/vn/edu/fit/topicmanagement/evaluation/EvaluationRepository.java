package vn.edu.fit.topicmanagement.evaluation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    Optional<Evaluation> findByRegistration_IdAndEvaluator_Id(Long registrationId, Long evaluatorId);

    List<Evaluation> findByRegistration_Id(Long registrationId);

    List<Evaluation> findByEvaluator_Id(Long evaluatorId);

    long countByRegistration_IdAndStatus(Long registrationId, EvaluationStatus status);
}
