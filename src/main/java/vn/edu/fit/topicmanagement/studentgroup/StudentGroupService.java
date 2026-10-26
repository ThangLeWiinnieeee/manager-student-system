
package vn.edu.fit.topicmanagement.studentgroup;

import java.time.LocalDateTime;
import java.util.List;

import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountRepository;
import vn.edu.fit.topicmanagement.user.Role;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistration;
import vn.edu.fit.topicmanagement.registrationperiod.CourseRegistrationService;

@Service
@Transactional
public class StudentGroupService {

    private final StudentGroupRepository groups;
    private final StudentGroupMemberRepository members;
    private final UserAccountRepository users;
    private final CourseRegistrationService periods;
    private final GroupTopicRegistrationRepository registrations;
    private final GroupInvitationRepository invitations;
    private final vn.edu.fit.topicmanagement.result.TopicFinalResultRepository results;
    private final vn.edu.fit.topicmanagement.handover.HandoverLog handovers;




    public StudentGroupService(
            StudentGroupRepository groups,
            StudentGroupMemberRepository members,
            UserAccountRepository users,
            CourseRegistrationService periods,
            GroupTopicRegistrationRepository registrations, GroupInvitationRepository invitations,
            vn.edu.fit.topicmanagement.result.TopicFinalResultRepository results,
            vn.edu.fit.topicmanagement.handover.HandoverLog handovers) {

        this.groups = groups;
        this.members = members;
        this.users = users;
        this.periods = periods;
        this.registrations = registrations;
        this.invitations = invitations;
        this.results = results;
        this.handovers = handovers;
    }


    // 1. Lay danh sach tat ca nhom
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    @Transactional(readOnly = true)
    public List<StudentGroup> findAll() {
        var allowed = periods.findManageable().stream().map(CourseRegistration::getId).toList();
        return groups.findAllByOrderByCreatedAtDesc().stream()
                .filter(g -> CourseRegistrationService.isManager() || allowed.contains(g.getRoundId())).toList();
    }

    // 2. Lay nhom theo dot dang ky
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    @Transactional(readOnly = true)
    public List<StudentGroup> findByRound(Long roundId) {
        periods.assertCanManage(periods.get(roundId));
        return groups.findByRoundIdOrderByNameAsc(roundId);
    }

    // 3. Lay chi tiet nhom
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    @Transactional(readOnly = true)
    public StudentGroup get(Long id) {
        StudentGroup group = getExistingGroup(id);
        assertStaffCanManage(group);
        return group;
    }

    // 4. Tao nhom
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public StudentGroup create(StudentGroupForm form) {
        String name = normalizeName(form.getName());
        Long roundId = requireRoundId(form.getRoundId());
        periods.lock(roundId);

        if (groups.existsByRoundIdAndNameIgnoreCase(
                roundId, name)) {
            throw new BusinessRuleException(
                    "Ten nhom da ton tai trong dot dang ky");
        }

        StudentGroup group = new StudentGroup();
        group.setName(name);
        group.setRoundId(roundId);

        assertStaffCanManage(group);
        return groups.save(group);
    }

    // 5. Cap nhat nhom
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public StudentGroup update(
            Long id, StudentGroupForm form) {

        StudentGroup group = lockGroup(id);
        periods.assertCanManage(periods.get(group.getRoundId()));
        assertGroupNotApproved(id);

        String name = normalizeName(form.getName());
        Long roundId = requireRoundId(form.getRoundId());

        if (groups.existsByRoundIdAndNameIgnoreCaseAndIdNot(
                roundId, name, id)) {
            throw new BusinessRuleException(
                    "Ten nhom da ton tai trong dot dang ky");
        }

        // Khong doi dot dang ky neu nhom da co thanh vien
        if (!group.getRoundId().equals(roundId)
                && members.countByGroup_Id(id) > 0) {
            throw new BusinessRuleException(
                    "Khong the doi dot dang ky cua nhom da co thanh vien");
        }

        periods.assertCanManage(periods.get(roundId));
        if (!group.getRoundId().equals(roundId))
            throw new BusinessRuleException("Hãy tạo nhóm mới trong đợt cần chuyển đến");
        group.setName(name);
        return group;
    }

    // 6. Chuyen Entity thanh Form de sua tren JSP
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    @Transactional(readOnly = true)
    public StudentGroupForm toForm(StudentGroup group) {

        StudentGroupForm form = new StudentGroupForm();

        form.setName(group.getName());
        form.setRoundId(group.getRoundId());

        return form;
    }

    // 7. Xoa nhom
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public void delete(Long id) {

        StudentGroup group = lockGroup(id);
        periods.assertCanManage(periods.get(group.getRoundId()));
        assertGroupNotApproved(id);

        if (members.countByGroup_Id(id) > 0) {
            throw new BusinessRuleException(
                    "Khong the xoa nhom dang co thanh vien");
        }

        groups.delete(group);
    }

    // Ham noi bo: lay nhom khong qua method security proxy
    private StudentGroup getExistingGroup(Long id) {
        return groups.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Khong tim thay nhom sinh vien"));
    }

    private StudentGroup lockGroup(Long id) {
        periods.lock(groups.findRoundIdById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm")));
        return getExistingGroup(id);
    }

    @PreAuthorize("hasRole('STUDENT')")
    public StudentGroup lockMyLeaderGroup(Long id) {
        return requireMyLeaderGroup(id);
    }

    // Ham noi bo: chuan hoa ten nhom
    private String normalizeName(String name) {

        if (name == null || name.isBlank()) {
            throw new BusinessRuleException(
                    "Ten nhom khong duoc de trong");
        }

        String normalized = name.trim();

        if (normalized.length() > 150) {
            throw new BusinessRuleException(
                    "Ten nhom toi da 150 ky tu");
        }

        return normalized;
    }

    // Ham noi bo: kiem tra ID dot dang ky
    private Long requireRoundId(Long roundId) {
        if (roundId == null || roundId <= 0) {
            throw new BusinessRuleException(
                    "Vui long chon dot dang ky hop le");
        }

        periods.get(roundId);
        return roundId;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    @Transactional(readOnly = true)
    public List<StudentGroupMemberView> getMembers(Long groupId) {

        StudentGroup group = getExistingGroup(groupId);
        assertStaffCanManage(group);

        Long leaderId = group.getLeader() == null
                ? null
                : group.getLeader().getId();

        return members.findByGroup_IdOrderByJoinedAtAsc(groupId)
                .stream()
                .map(member -> {

                    UserAccount student = member.getStudent();

                    boolean isLeader = leaderId != null
                            && leaderId.equals(student.getId());

                    return new StudentGroupMemberView(
                            student.getId(),
                            student.getFullName(),
                            student.getEmail(),
                            isLeader,
                            member.getJoinedAt()
                    );
                })
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public void addMember(Long groupId, String email) {
        StudentGroup group = lockGroup(groupId);
        assertStaffCanManage(group);
        invite(group, email);
    }

    private UserAccount eligibleMember(StudentGroup group, String email) {
        assertGroupNotApproved(group.getId());
        periods.assertSvWindowOpen(group.getRoundId());
        if (email == null || email.isBlank()) throw new BusinessRuleException("Cần email sinh viên");
        UserAccount student = users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new BusinessRuleException("Không tìm thấy sinh viên"));
        if (student.getRole() != Role.STUDENT || !student.isEnabled())
            throw new BusinessRuleException("Sinh viên phải có tài khoản đang hoạt động");
        periods.assertParticipant(group.getRoundId(), student.getId());
        assertGroupHasSpace(group.getId());
        if (members.findByStudent_Id(student.getId()).stream()
                .anyMatch(m -> m.getGroup().getRoundId().equals(group.getRoundId())))
            throw new BusinessRuleException("Sinh viên đã thuộc một nhóm trong môn/lớp này");
        return student;
    }

    private void invite(StudentGroup group, String email) {
        UserAccount student = eligibleMember(group, email);
        var id = new StudentGroupMemberId(group.getId(), student.getId());
        if (!invitations.existsById(id)) invitations.save(new GroupInvitation(group, student));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<GroupInvitation> findMyInvitations() {
        return invitations.findByStudent_Id(getCurrentStudent().getId());
    }

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','STUDENT')")
    @Transactional(readOnly = true)
    public List<GroupInvitation> findInvitations(Long groupId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) getMyGroup(groupId);
        else get(groupId);
        return invitations.findByGroup_Id(groupId);
    }

    @PreAuthorize("hasRole('STUDENT')")
    public void respondToInvitation(Long groupId, boolean accept) {
        StudentGroup group = lockGroup(groupId);
        UserAccount student = getCurrentStudent();
        var id = new StudentGroupMemberId(groupId, student.getId());
        var invitation = invitations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lời mời của bạn"));
        if (accept) {
            eligibleMember(group, student.getEmail());
            members.save(new StudentGroupMember(group, student));
        }
        invitations.delete(invitation);
    }

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','STUDENT')")
    public void cancelInvitation(Long groupId, Long studentId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) requireMyLeaderGroup(groupId);
        else { StudentGroup group = lockGroup(groupId); assertStaffCanManage(group); }
        invitations.findById(new StudentGroupMemberId(groupId, studentId)).ifPresent(invitations::delete);
    }

//    Xóa thành viên khỏi nhóm

    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public void removeMember(Long groupId, Long studentId) {

        StudentGroup group = lockGroup(groupId);
        assertStaffCanManage(group);
        assertGroupNotApproved(groupId);

        StudentGroupMemberId memberId =
                new StudentGroupMemberId(groupId, studentId);

        if (!members.existsById(memberId)) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy thành viên trong nhóm");
        }

        if (group.getLeader() != null
                && group.getLeader().getId().equals(studentId)) {

            throw new BusinessRuleException(
                    "Không thể xóa nhóm trưởng. "
                            + "Hãy chọn nhóm trưởng khác trước");
        }

        members.deleteById(memberId);
    }
//    Chọn nhóm trưởng

    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    public void assignLeader(Long groupId, Long studentId) {

        StudentGroup group = lockGroup(groupId);
        assertStaffCanManage(group);
        registrations.findByGroup_Id(groupId).flatMap(r -> results.findByRegistration_Id(r.getId()))
                .filter(r -> r.isPresidentConfirmed() || r.getPublishedAt() != null)
                .ifPresent(r -> { throw new BusinessRuleException("Nhóm đã chốt kết quả: không đổi nhóm trưởng"); });

        StudentGroupMemberId memberId =
                new StudentGroupMemberId(groupId, studentId);

        StudentGroupMember member = members.findById(memberId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Sinh viên không thuộc nhóm này"));

        UserAccount student = member.getStudent();

        if (student.getRole() != Role.STUDENT
                || !student.isEnabled()) {

            throw new BusinessRuleException(
                    "Sinh viên không đủ điều kiện làm nhóm trưởng");
        }

        if (group.getLeader() != null && !group.getLeader().getId().equals(studentId))
            handovers.record(group.getRoundId(), "Nhóm trưởng", groupId, group.getLeader().getId(), studentId,
                    "GV phụ trách/quản trị viên chuyển quyền cho thành viên hiện hữu");
        group.setLeader(student);
    }


    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<MyGroupView> findMyGroups() {

        // Lay tai khoan dang dang nhap
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(
                    "Bạn chưa đăng nhập");
        }

        String email = authentication.getName();

        UserAccount student = users
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AccessDeniedException(
                        "Không tìm thấy tài khoản hiện tại"));

        if (student.getRole() != Role.STUDENT
                || !student.isEnabled()) {
            throw new AccessDeniedException(
                    "Tài khoản không có quyền sinh viên");
        }

        // Chi lay nhom ma sinh vien tham gia
        return members.findByStudent_Id(student.getId())
                .stream()
                .map(member -> {

                    StudentGroup group = member.getGroup();

                    Long leaderId = group.getLeader() == null
                            ? null
                            : group.getLeader().getId();

                    boolean isLeader =
                            student.getId().equals(leaderId);

                    return new MyGroupView(
                            group.getId(),
                            group.getName(),
                            group.getRoundId(),
                            isLeader
                    );
                })
                .toList();
    }


    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<CourseRegistration> findOpenStudentPeriods() {

        LocalDateTime now = LocalDateTime.now();

        return periods.findJoinedCourses()
                .stream()
                .filter(period -> period.isSvWindowOpen(now))
                .toList();
    }


    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public StudentGroup createMyGroup(StudentGroupForm form) {

        // 1. Xac dinh sinh vien dang dang nhap
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn chưa đăng nhập");
        }

        String email = authentication.getName();

        UserAccount student = users
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AccessDeniedException(
                        "Không tìm thấy tài khoản sinh viên"));

        if (student.getRole() != Role.STUDENT
                || !student.isEnabled()) {
            throw new AccessDeniedException(
                    "Tài khoản không có quyền sinh viên");
        }

        // 2. Kiem tra dot dang ky
        Long roundId = form.getRoundId();

        if (roundId == null || roundId <= 0) {
            throw new BusinessRuleException(
                    "Vui lòng chọn môn/lớp đăng ký");
        }

        periods.lock(roundId);
        periods.assertSvWindowOpen(roundId);
        periods.assertParticipant(roundId, student.getId());

        // 3. Khong cho mot sinh vien thuoc hai nhom
        // trong cung mot dot dang ky
        boolean alreadyJoined = members
                .findByStudent_Id(student.getId())
                .stream()
                .anyMatch(member ->
                        roundId.equals(
                                member.getGroup().getRoundId()));

        if (alreadyJoined) {
            throw new BusinessRuleException(
                    "Bạn đã tham gia một nhóm trong môn/lớp này");
        }

        // 4. Tao nhom
        StudentGroup group = create(form);

        // 5. Tu dong them nguoi tao vao nhom
        members.save(new StudentGroupMember(group, eligibleMember(group, student.getEmail())));

        // 6. Tu dong dat nguoi tao lam nhom truong
        assignLeader(group.getId(), student.getId());

        return group;
    }


    private UserAccount getCurrentStudent() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(
                    "Bạn chưa đăng nhập");
        }

        UserAccount student = users
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException(
                        "Không tìm thấy tài khoản đăng nhập"));

        if (student.getRole() != Role.STUDENT
                || !student.isEnabled()) {
            throw new AccessDeniedException(
                    "Tài khoản không có quyền sinh viên");
        }

        return student;
    }


    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public StudentGroup getMyGroup(Long groupId) {

        UserAccount student = getCurrentStudent();

        StudentGroup group = getExistingGroup(groupId);

        if (!members.existsByGroup_IdAndStudent_Id(
                groupId, student.getId())) {

            throw new AccessDeniedException(
                    "Bạn không thuộc nhóm này");
        }

        return group;
    }

    // Chi duoc goi ben trong cac phuong thuc Service
    private StudentGroup requireMyLeaderGroup(Long groupId) {

        UserAccount student = getCurrentStudent();

        StudentGroup group = lockGroup(groupId);

        if (!members.existsByGroup_IdAndStudent_Id(
                groupId, student.getId())) {

            throw new AccessDeniedException(
                    "Bạn không thuộc nhóm này");
        }

        if (group.getLeader() == null
                || !student.getId().equals(
                group.getLeader().getId())) {

            throw new AccessDeniedException(
                    "Chỉ nhóm trưởng được thực hiện thao tác này");
        }

        return group;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<StudentGroupMemberView> getMyGroupMembers(
            Long groupId) {

        getMyGroup(groupId);

        return getMembers(groupId);
    }


    @PreAuthorize("hasRole('STUDENT')")
    public void addMemberByLeader(Long groupId, String email) {
        invite(requireMyLeaderGroup(groupId), email);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public void removeMemberByLeader(
            Long groupId,
            Long studentId) {

        // 1. Kiem tra quyen nhom truong
        StudentGroup group =
                requireMyLeaderGroup(groupId);
        periods.lock(group.getRoundId());
        assertGroupNotApproved(groupId);

        // 2. Kiem tra dot dang ky con mo
        periods.assertSvWindowOpen(group.getRoundId());

        // 3. Khong cho nhom truong tu xoa minh
        if (group.getLeader() != null
                && group.getLeader().getId().equals(studentId)) {

            throw new BusinessRuleException(
                    "Không thể xóa nhóm trưởng hiện tại");
        }

        // 4. Goi nghiep vu xoa thanh vien da co
        removeMember(groupId, studentId);
    }


    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public boolean isMyGroupLeader(Long groupId) {

        StudentGroup group = getMyGroup(groupId);
        UserAccount student = getCurrentStudent();

        return group.getLeader() != null
                && student.getId().equals(
                group.getLeader().getId());
    }


    @Transactional(readOnly = true)
    public boolean isMembershipOpen(Long groupId) {
        var group = getExistingGroup(groupId);
        return periods.get(group.getRoundId()).isSvWindowOpen(LocalDateTime.now())
                && !registrations.existsByGroup_IdAndStatus(groupId, GroupTopicRegistrationStatus.APPROVED);
    }

    private void assertGroupNotApproved(Long groupId) {

        boolean approved =
                registrations.existsByGroup_IdAndStatus(
                        groupId,
                        GroupTopicRegistrationStatus.APPROVED
                );

        if (approved) {

            throw new BusinessRuleException(
                    "Nhóm đã được chấp thuận đề tài, " +
                            "không thể thay đổi thông tin hoặc thành viên");
        }
    }

    private void assertStaffCanManage(StudentGroup group) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LECTURER")))
            periods.assertCanManage(periods.get(group.getRoundId()));
    }

    private void assertGroupHasSpace(Long groupId) {

        long memberCount = members.countByGroup_Id(groupId);

        int limit = periods.get(getExistingGroup(groupId).getRoundId()).getMaxMembers();
        if (memberCount >= limit) {
            throw new BusinessRuleException(
                    "Nhóm chỉ được tối đa " + limit + " thành viên trong môn/lớp này");
        }
    }

}
