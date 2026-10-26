package vn.edu.fit.topicmanagement.topic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository đề tài.
 * search(): lọc theo đợt / bộ môn / trạng thái (bắt buộc của ngày 10 - kế hoạch TV2),
 * kèm phân trang. Với giảng viên/sinh viên (viewerEmail khác null), chỉ thấy
 * đề tài do mình tạo hoặc đề tài đã công bố.
 */
public interface TopicRepository extends JpaRepository<Topic, Long> {

    @org.springframework.data.jpa.repository.Query("""
            select t from Topic t left join t.department m left join t.round.lecturer lecturer
            where t.proposedGroupId is null and (:roundId is null or t.round.id = :roundId)
              and (:departmentId is null or m.id = :departmentId)
              and (:status is null or t.status = :status)
              and (:viewerEmail is null or t.createdBy.email = :viewerEmail or lecturer.email = :viewerEmail or t.status = :publishedStatus)
            """)
    Page<Topic> search(
            @Param("roundId") Long roundId,
            @Param("departmentId") Long departmentId,
            @Param("status") TopicStatus status,
            @Param("viewerEmail") String viewerEmail,
            @Param("publishedStatus") TopicStatus publishedStatus,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Query("""
            select t from Topic t where t.round.id = :roundId and t.status = :status
              and t.proposedGroupId is null
              and not exists (select r.id from GroupTopicRegistration r where r.exclusiveTopicId = t.id)
            """)
    Page<Topic> findAvailable(@Param("roundId") Long roundId, @Param("status") TopicStatus status, Pageable pageable);
}
