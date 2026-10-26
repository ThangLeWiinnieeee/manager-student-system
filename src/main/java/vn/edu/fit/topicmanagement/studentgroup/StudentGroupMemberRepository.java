
package vn.edu.fit.topicmanagement.studentgroup;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentGroupMemberRepository
        extends JpaRepository<
        StudentGroupMember,
        StudentGroupMemberId> {

    // Danh sach thanh vien cua mot nhom
    List<StudentGroupMember> findByGroup_IdOrderByJoinedAtAsc(
            Long groupId
    );

    // Kiem tra sinh vien co thuoc nhom hay khong
    boolean existsByGroup_IdAndStudent_Id(
            Long groupId,
            Long studentId
    );

    // Dem so thanh vien cua mot nhom
    boolean existsByGroup_IdAndStudent_EmailIgnoreCase(Long groupId, String email);
    long countByGroup_Id(Long groupId);

    // Tim cac nhom ma sinh vien tham gia
    List<StudentGroupMember> findByStudent_Id(
            Long studentId
    );

    // Xoa mot thanh vien khoi nhom
    long deleteByGroup_IdAndStudent_Id(
            Long groupId,
            Long studentId
    );
}
