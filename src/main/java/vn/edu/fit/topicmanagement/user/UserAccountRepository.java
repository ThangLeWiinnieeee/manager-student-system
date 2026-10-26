package vn.edu.fit.topicmanagement.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from UserAccount u where u.id = :id")
    Optional<UserAccount> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT (
                EXISTS (
                    SELECT 1 FROM registration_periods c JOIN registration_windows w ON w.id = c.period_id
                    WHERE c.lecturer_id = :id AND w.sv_end >= :now
                )
                OR EXISTS (
                    SELECT 1 FROM topic_advisors a JOIN topics t ON t.id = a.topic_id
                    JOIN registration_periods c ON c.id = t.round_id
                    JOIN registration_windows w ON w.id = c.period_id
                    WHERE a.advisor_id = :id AND w.sv_end >= :now
                )
                OR EXISTS (
                    SELECT 1 FROM student_groups g
                    JOIN registration_periods c ON c.id = g.round_id
                    LEFT JOIN group_topic_registrations r ON r.group_id = g.id
                    LEFT JOIN group_results result ON result.registration_id = r.id
                    WHERE (result.id IS NULL OR (result.president_confirmed = FALSE AND result.published_at IS NULL))
                    AND (c.lecturer_id = :id OR g.leader_id = :id
                        OR EXISTS (SELECT 1 FROM student_group_members m WHERE m.group_id = g.id AND m.student_id = :id)
                        OR EXISTS (SELECT 1 FROM topic_advisors a WHERE a.topic_id = r.topic_id AND a.advisor_id = :id))
                )
                OR EXISTS (
                    SELECT 1 FROM group_assignments a
                    LEFT JOIN group_results result ON result.registration_id = a.registration_id
                    WHERE (result.id IS NULL OR (result.president_confirmed = FALSE AND result.published_at IS NULL))
                    AND (a.reviewer_id = :id OR a.supporter_id = :id
                        OR EXISTS (SELECT 1 FROM council_members m WHERE m.council_id = a.council_id AND m.lecturer_id = :id))
                )
            )
            """, nativeQuery = true)
    boolean hasOpenResponsibilities(@org.springframework.data.repository.query.Param("id") Long id,
            @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);

    Optional<UserAccount> findByEmailIgnoreCase(String email);
    List<UserAccount> findAllByOrderByFullNameAsc();
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByDepartmentId(Long departmentId);
}
