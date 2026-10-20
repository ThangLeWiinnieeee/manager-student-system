package vn.edu.fit.topicmanagement.council;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouncilMemberRepository extends JpaRepository<CouncilMember, CouncilMemberId> {

    List<CouncilMember> findByCouncil_IdOrderByRoleAsc(Long councilId);

    List<CouncilMember> findByLecturer_Id(Long lecturerId);

    long countByCouncil_Id(Long councilId);

    boolean existsByCouncil_IdAndLecturer_Id(Long councilId, Long lecturerId);

    boolean existsByCouncil_IdAndRole(Long councilId, CouncilRole role);
}
