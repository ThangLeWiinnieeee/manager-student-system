package vn.edu.fit.topicmanagement.topic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository đề tài.
 * search(): lọc theo đợt / chuyên ngành / trạng thái (bắt buộc của ngày 10 - kế hoạch TV2),
 * kèm phân trang. Với giảng viên/sinh viên (viewerEmail khác null), chỉ thấy
 * đề tài do mình tạo hoặc đề tài đã công bố.
 */
public interface TopicRepository extends JpaRepository<Topic, Long> {

    @org.springframework.data.jpa.repository.Query("""
            select t from Topic t left join t.major m
            where (:roundId is null or t.round.id = :roundId)
              and (:majorId is null or m.id = :majorId)
              and (:status is null or t.status = :status)
              and (:viewerEmail is null or t.createdBy.email = :viewerEmail or t.status = :publishedStatus)
            """)
    Page<Topic> search(
            @Param("roundId") Long roundId,
            @Param("majorId") Long majorId,
            @Param("status") TopicStatus status,
            @Param("viewerEmail") String viewerEmail,
            @Param("publishedStatus") TopicStatus publishedStatus,
            Pageable pageable);
}
