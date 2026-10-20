
package vn.edu.fit.topicmanagement.studentgroup;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupTopicRegistrationRepository
        extends JpaRepository<GroupTopicRegistration, Long> {

    // Lay dang ky cua mot nhom
    Optional<GroupTopicRegistration> findByGroup_Id(
            Long groupId
    );

    // Kiem tra nhom da duoc chap thuan hay chua
    boolean existsByGroup_IdAndStatus(
            Long groupId,
            GroupTopicRegistrationStatus status
    );

    // Lay dang ky theo de tai
    List<GroupTopicRegistration> findByTopic_Id(Long topicId);

    // Kiem tra de tai da duoc chap thuan dang ky hay chua
    boolean existsByTopic_IdAndStatus(
            Long topicId,
            GroupTopicRegistrationStatus status
    );

    // DEAN: Danh sach tat ca dang ky
    @EntityGraph(attributePaths = {"group", "topic"})
    List<GroupTopicRegistration>
    findAllByOrderBySubmittedAtDesc();

    // Khoa ban ghi khi duyet, tranh xu ly dong thoi
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM GroupTopicRegistration r
            WHERE r.id = :id
            """)
    Optional<GroupTopicRegistration> findByIdForUpdate(
            @Param("id") Long id
    );
}
