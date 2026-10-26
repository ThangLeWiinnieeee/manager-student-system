package vn.edu.fit.topicmanagement.handover;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;

@Repository
public class HandoverLog {
    private final JdbcTemplate jdbc;
    public HandoverLog(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void record(Long courseId, String task, Long targetId, Long previousId, Long replacementId, String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 500)
            throw new BusinessRuleException("Cần lý do bàn giao, tối đa 500 ký tự");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        jdbc.update("""
                INSERT INTO academic_handovers(course_id, task, target_id, previous_user_id, replacement_user_id, actor_id, reason)
                VALUES (?, ?, ?, ?, ?, (SELECT id FROM users WHERE lower(email)=lower(?)), ?)
                """, courseId, task, targetId, previousId, replacementId, email, reason.trim());
    }

    public List<Map<String, Object>> findByCourse(Long courseId) {
        return jdbc.queryForList("""
                SELECT h.task, h.target_id, h.reason, h.created_at,
                       p.full_name AS previous_name, n.full_name AS replacement_name, a.full_name AS actor_name
                FROM academic_handovers h JOIN users p ON p.id=h.previous_user_id
                JOIN users n ON n.id=h.replacement_user_id JOIN users a ON a.id=h.actor_id
                WHERE h.course_id=? ORDER BY h.id DESC
                """, courseId);
    }
}
