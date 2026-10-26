package vn.edu.fit.topicmanagement.studentgroup;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, StudentGroupMemberId> {
    @EntityGraph(attributePaths = {"group", "student"})
    List<GroupInvitation> findByStudent_Id(Long studentId);
    @EntityGraph(attributePaths = {"group", "student"})
    List<GroupInvitation> findByGroup_Id(Long groupId);
    void deleteByGroup_Id(Long groupId);
}
