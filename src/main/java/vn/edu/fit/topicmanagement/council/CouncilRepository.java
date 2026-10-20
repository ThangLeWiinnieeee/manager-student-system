package vn.edu.fit.topicmanagement.council;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouncilRepository extends JpaRepository<Council, Long> {

    List<Council> findAllByOrderByCreatedAtDesc();

    List<Council> findByRoundIdOrderByNameAsc(Long roundId);

    boolean existsByRoundIdAndNameIgnoreCase(Long roundId, String name);

    boolean existsByRoundIdAndNameIgnoreCaseAndIdNot(Long roundId, String name, Long id);

    @Query("SELECT c FROM Council c JOIN c.members m WHERE m.lecturer.id = :lecturerId ORDER BY c.createdAt DESC")
    List<Council> findByLecturerId(@Param("lecturerId") Long lecturerId);
}
