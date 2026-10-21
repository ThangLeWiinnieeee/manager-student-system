package vn.edu.fit.topicmanagement.notification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByPinnedDescCreatedAtDesc();

    @Query("SELECT n FROM Notification n WHERE n.published = true AND (n.targetRole = 'ALL' OR n.targetRole = :targetRole) ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notification> findPublishedForRole(@Param("targetRole") NotificationTarget targetRole);

    @Query("SELECT n FROM Notification n WHERE n.published = true ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notification> findAllPublished();
}
