
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

import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriod;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodService;

@Service
@Transactional
public class StudentGroupService {

    private final StudentGroupRepository groups;
    private final StudentGroupMemberRepository members;
    private final UserAccountRepository users;
    private final RegistrationPeriodService periods;
    private final GroupTopicRegistrationRepository registrations;

    private static final int MAX_GROUP_MEMBERS = 5;


    public StudentGroupService(
            StudentGroupRepository groups,
            StudentGroupMemberRepository members,
            UserAccountRepository users,
            RegistrationPeriodService periods,
            GroupTopicRegistrationRepository registrations) {

        this.groups = groups;
        this.members = members;
        this.users = users;
        this.periods = periods;
        this.registrations = registrations;
    }


    // 1. Lay danh sach tat ca nhom
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    @Transactional(readOnly = true)
    public List<StudentGroup> findAll() {
        return groups.findAllByOrderByCreatedAtDesc();
    }

    // 2. Lay nhom theo dot dang ky
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    @Transactional(readOnly = true)
    public List<StudentGroup> findByRound(Long roundId) {
        return groups.findByRoundIdOrderByNameAsc(roundId);
    }

    // 3. Lay chi tiet nhom
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    @Transactional(readOnly = true)
    public StudentGroup get(Long id) {
        return groups.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Khong tim thay nhom sinh vien"));
    }

    // 4. Tao nhom
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    public StudentGroup create(StudentGroupForm form) {
        String name = normalizeName(form.getName());
        Long roundId = requireRoundId(form.getRoundId());

        if (groups.existsByRoundIdAndNameIgnoreCase(
                roundId, name)) {
            throw new BusinessRuleException(
                    "Ten nhom da ton tai trong dot dang ky");
        }

        StudentGroup group = new StudentGroup();
        group.setName(name);
        group.setRoundId(roundId);

        return groups.save(group);
    }

    // 5. Cap nhat nhom
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    public StudentGroup update(
            Long id, StudentGroupForm form) {

        StudentGroup group = getExistingGroup(id);
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

        group.setName(name);
        group.setRoundId(roundId);

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
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    public void delete(Long id) {

        StudentGroup group = getExistingGroup(id);
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

        return roundId;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    @Transactional(readOnly = true)
    public List<StudentGroupMemberView> getMembers(Long groupId) {

        StudentGroup group = getExistingGroup(groupId);

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

    // Thêm sinh viên vào nhóm
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    public void addMember(Long groupId, String email) {

        StudentGroup group = getExistingGroup(groupId);
        assertGroupNotApproved(groupId);

        if (email == null || email.isBlank()) {
            throw new BusinessRuleException(
                    "Email sinh viên không được để trống");
        }

        UserAccount student = users
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new BusinessRuleException(
                        "Không tìm thấy tài khoản sinh viên"));

        if (student.getRole() != Role.STUDENT) {
            throw new BusinessRuleException(
                    "Tài khoản không có vai trò sinh viên");
        }

        if (!student.isEnabled()) {
            throw new BusinessRuleException(
                    "Tài khoản sinh viên đã bị khóa");
        }

        if (members.existsByGroup_IdAndStudent_Id(
                groupId, student.getId())) {
            throw new BusinessRuleException(
                    "Sinh viên đã thuộc nhóm này");
        }


        // Kiem tra nhom chua du 5 thanh vien
        assertGroupHasSpace(groupId);

        // Mot sinh vien chi duoc thuoc mot nhom
        // trong cung dot dang ky
        boolean alreadyJoinedAnotherGroup = members
                .findByStudent_Id(student.getId())
                .stream()
                .anyMatch(member ->
                        member.getGroup()
                                .getRoundId()
                                .equals(group.getRoundId()));

        if (alreadyJoinedAnotherGroup) {

            throw new BusinessRuleException(
                    "Sinh viên đã thuộc một nhóm khác "
                            + "trong cùng đợt đăng ký");
        }


        StudentGroupMember member =
                new StudentGroupMember(group, student);

        members.save(member);
    }
//    Xóa thành viên khỏi nhóm

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    public void removeMember(Long groupId, Long studentId) {

        StudentGroup group = getExistingGroup(groupId);
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

    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN', 'LECTURER')")
    public void assignLeader(Long groupId, Long studentId) {

        StudentGroup group = getExistingGroup(groupId);
        assertGroupNotApproved(groupId);

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
    public List<RegistrationPeriod> findOpenStudentPeriods() {

        LocalDateTime now = LocalDateTime.now();

        return periods.findAll()
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
                    "Vui lòng chọn đợt đăng ký");
        }

        periods.assertSvWindowOpen(roundId);

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
                    "Bạn đã tham gia một nhóm trong đợt này");
        }

        // 4. Tao nhom
        StudentGroup group = create(form);

        // 5. Tu dong them nguoi tao vao nhom
        addMember(group.getId(), student.getEmail());

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

        StudentGroup group = getExistingGroup(groupId);

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
    @Transactional
    public void addMemberByLeader(
            Long groupId,
            String email) {

        // 1. Kiem tra quyen nhom truong
        StudentGroup group =
                requireMyLeaderGroup(groupId);
        assertGroupNotApproved(groupId);

        // 2. Kiem tra dot dang ky con mo
        periods.assertSvWindowOpen(group.getRoundId());

        // 3. Kiem tra gioi han toi da 5 thanh vien
        assertGroupHasSpace(groupId);

        // 4. Kiem tra email
        if (email == null || email.isBlank()) {
            throw new BusinessRuleException(
                    "Vui lòng nhập email sinh viên");
        }

        String normalizedEmail = email.trim();

        // 4. Tim tai khoan sinh vien
        UserAccount student = users
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BusinessRuleException(
                        "Không tìm thấy sinh viên"));

        if (student.getRole() != Role.STUDENT
                || !student.isEnabled()) {
            throw new BusinessRuleException(
                    "Tài khoản không phải sinh viên đang hoạt động");
        }

        // 5. Mot sinh vien chi thuoc mot nhom trong moi dot
        boolean alreadyJoined = members
                .findByStudent_Id(student.getId())
                .stream()
                .anyMatch(member ->
                        group.getRoundId().equals(
                                member.getGroup().getRoundId()));

        if (alreadyJoined) {
            throw new BusinessRuleException(
                    "Sinh viên đã thuộc một nhóm trong đợt này");
        }

        // 6. Su dung lai nghiep vu them thanh vien
        addMember(groupId, normalizedEmail);
    }



    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public void removeMemberByLeader(
            Long groupId,
            Long studentId) {

        // 1. Kiem tra quyen nhom truong
        StudentGroup group =
                requireMyLeaderGroup(groupId);
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

    private void assertGroupHasSpace(Long groupId) {

        long memberCount = members.countByGroup_Id(groupId);

        if (memberCount >= MAX_GROUP_MEMBERS) {
            throw new BusinessRuleException(
                    "Nhóm đã đủ 5 thành viên, không thể thêm sinh viên mới");
        }
    }

}
