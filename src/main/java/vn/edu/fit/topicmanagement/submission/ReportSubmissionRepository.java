
package vn.edu.fit.topicmanagement.submission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportSubmissionRepository
        extends JpaRepository<ReportSubmission, Long> {

    // Lich su bao cao cua mot nhom
    @EntityGraph(attributePaths = {"uploadedBy"})
    List<ReportSubmission>
    findByGroup_IdOrderByUploadedAtDesc(Long groupId);

    // Tim dung bao cao thuoc dung nhom
    Optional<ReportSubmission> findByIdAndGroup_Id(
            Long reportId,
            Long groupId
    );

    // Dem so lan nop bao cao cua nhom
    long countByGroup_Id(Long groupId);
}
