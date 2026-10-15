
package vn.edu.fit.topicmanagement.studentgroup;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentGroupRepository
        extends JpaRepository<StudentGroup, Long> {

    // Lay tat ca nhom theo thu tu moi nhat
    List<StudentGroup> findAllByOrderByCreatedAtDesc();

    // Lay danh sach nhom cua mot dot dang ky
    List<StudentGroup> findByRoundIdOrderByNameAsc(Long roundId);

    // Kiem tra ten nhom da ton tai trong dot dang ky
    boolean existsByRoundIdAndNameIgnoreCase(
            Long roundId,
            String name
    );

    // Kiem tra trung ten khi cap nhat nhom
    boolean existsByRoundIdAndNameIgnoreCaseAndIdNot(
            Long roundId,
            String name,
            Long id
    );
}
