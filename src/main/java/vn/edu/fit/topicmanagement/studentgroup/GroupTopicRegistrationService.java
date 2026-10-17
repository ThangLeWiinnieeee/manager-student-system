
package vn.edu.fit.topicmanagement.studentgroup;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.fit.topicmanagement.common.exception.BusinessRuleException;
import vn.edu.fit.topicmanagement.registrationperiod.RegistrationPeriodService;
import vn.edu.fit.topicmanagement.topic.Topic;
import vn.edu.fit.topicmanagement.topic.TopicService;
import vn.edu.fit.topicmanagement.topic.TopicStatus;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import vn.edu.fit.topicmanagement.common.exception.ResourceNotFoundException;
import vn.edu.fit.topicmanagement.user.UserAccount;
import vn.edu.fit.topicmanagement.user.UserAccountService;

@Service
public class GroupTopicRegistrationService {

    private final GroupTopicRegistrationRepository registrations;
    private final StudentGroupService groups;
    private final TopicService topics;
    private final RegistrationPeriodService periods;
    private final UserAccountService users;


    public GroupTopicRegistrationService(
            GroupTopicRegistrationRepository registrations,
            StudentGroupService groups,
            TopicService topics,
            RegistrationPeriodService periods,
            UserAccountService users) {

        this.registrations = registrations;
        this.groups = groups;
        this.topics = topics;
        this.periods = periods;
        this.users = users;
    }

    // TV3: Xem de tai da cong bo cua dung dot
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public Page<Topic> findPublishedTopics(
            Long groupId,
            int page) {

        StudentGroup group = groups.getMyGroup(groupId);

        return topics.search(
                group.getRoundId(),
                null,
                TopicStatus.PUBLISHED,
                null,
                page
        );
    }

    // TV3: Xem dang ky hien tai
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public GroupTopicRegistration findMyRegistration(
            Long groupId) {

        groups.getMyGroup(groupId);

        return registrations.findByGroup_Id(groupId)
                .orElse(null);
    }

    // TV3: Nhom truong gui dang ky de tai
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public GroupTopicRegistration register(
            Long groupId,
            Long topicId) {

        // 1. Kiem tra sinh vien thuoc nhom
        StudentGroup group = groups.getMyGroup(groupId);

        // 2. Kiem tra quyen nhom truong
        if (!groups.isMyGroupLeader(groupId)) {
            throw new AccessDeniedException(
                    "Chỉ nhóm trưởng được đăng ký đề tài");
        }

        // 3. Kiem tra thoi gian dang ky
        periods.assertSvWindowOpen(group.getRoundId());

        if (topicId == null) {
            throw new BusinessRuleException(
                    "Vui lòng chọn đề tài");
        }

        // 4. Lay de tai tu module TV2
        Topic topic = topics.get(topicId);

        // 5. Chi cho phep de tai da cong bo
        if (topic.getStatus() != TopicStatus.PUBLISHED) {
            throw new BusinessRuleException(
                    "Đề tài chưa được công bố");
        }

        // 6. De tai phai thuoc cung dot voi nhom
        if (!group.getRoundId().equals(
                topic.getRound().getId())) {

            throw new BusinessRuleException(
                    "Đề tài không thuộc đợt đăng ký của nhóm");
        }

        // 7. Kiem tra dang ky cu
        GroupTopicRegistration registration =
                registrations.findByGroup_Id(groupId)
                        .orElse(null);

        if (registration != null) {

            // Chi cho phep gui lai khi bi tu choi
            if (registration.getStatus()
                    != GroupTopicRegistrationStatus.REJECTED) {

                throw new BusinessRuleException(
                        "Nhóm đã đăng ký đề tài và đang được xử lý hoặc đã được chấp thuận");
            }

        } else {

            registration = new GroupTopicRegistration();
            registration.setGroup(group);
        }

        // 8. Luu de tai va trang thai
        registration.setTopic(topic);

        registration.setStatus(
                GroupTopicRegistrationStatus.PENDING);

        registration.setRejectionReason(null);

        registration.setReviewedBy(null);
        registration.setReviewedAt(null);

        registration.setSubmittedAt(Instant.now());

        // 9. Luu database
        return registrations.save(registration);
    }


    // =========================================
    // TV3: DEAN XEM DANH SACH DANG KY
    // =========================================

    @PreAuthorize("hasRole('DEAN')")
    @Transactional(readOnly = true)
    public List<GroupTopicRegistration> findAllForDean() {

        return registrations.findAllByOrderBySubmittedAtDesc();
    }


    // =========================================
    // TV3: LAY TAI KHOAN DANG XET DUYET
    // =========================================

    private UserAccount getCurrentReviewer() {

        Authentication auth =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Bạn chưa đăng nhập");
        }

        return users.getByEmail(auth.getName());
    }


    // =========================================
    // TV3: TRUONG KHOA CHAP THUAN DANG KY
    // =========================================

    @PreAuthorize("hasRole('DEAN')")
    @Transactional
    public void approve(Long registrationId) {

        GroupTopicRegistration registration =
                registrations.findByIdForUpdate(registrationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy đăng ký đề tài"));

        // Chi duyet dang ky dang cho xu ly
        if (registration.getStatus()
                != GroupTopicRegistrationStatus.PENDING) {

            throw new BusinessRuleException(
                    "Chỉ được chấp thuận đăng ký đang chờ duyệt");
        }

        // Cap nhat trang thai
        registration.setStatus(
                GroupTopicRegistrationStatus.APPROVED);

        registration.setRejectionReason(null);

        // Luu nguoi va thoi diem xet duyet
        registration.setReviewedBy(getCurrentReviewer());

        registration.setReviewedAt(Instant.now());

        registrations.save(registration);
    }


    // =========================================
    // TV3: TRUONG KHOA TU CHOI DANG KY
    // =========================================

    @PreAuthorize("hasRole('DEAN')")
    @Transactional
    public void reject(
            Long registrationId,
            String reason) {

        // Bat buoc nhap ly do
        if (reason == null || reason.isBlank()) {

            throw new BusinessRuleException(
                    "Vui lòng nhập lý do từ chối");
        }

        String normalizedReason = reason.trim();

        if (normalizedReason.length() > 500) {

            throw new BusinessRuleException(
                    "Lý do từ chối không được vượt quá 500 ký tự");
        }

        GroupTopicRegistration registration =
                registrations.findByIdForUpdate(registrationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy đăng ký đề tài"));

        // Chi xu ly dang ky dang cho duyet
        if (registration.getStatus()
                != GroupTopicRegistrationStatus.PENDING) {

            throw new BusinessRuleException(
                    "Chỉ được từ chối đăng ký đang chờ duyệt");
        }

        registration.setStatus(
                GroupTopicRegistrationStatus.REJECTED);

        registration.setRejectionReason(normalizedReason);

        registration.setReviewedBy(getCurrentReviewer());

        registration.setReviewedAt(Instant.now());

        registrations.save(registration);
    }


}
