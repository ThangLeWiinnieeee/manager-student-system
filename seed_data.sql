-- ====================================================================
-- FILE DỮ LIỆU MẪU (SEED DATA) CHO HỆ THỐNG QUẢN LÝ ĐỀ TÀI SINH VIÊN
-- Mật khẩu mặc định cho TẤT CẢ các tài khoản là: Admin123!
-- ====================================================================

-- 1. BỘ MÔN (DEPARTMENTS)
INSERT INTO departments (id, code, name, enabled) VALUES
(1, 'CNPM', 'Công nghệ Phần mềm', true),
(2, 'HTTT', 'Hệ thống Thông tin', true),
(3, 'KHMT', 'Khoa học Máy tính', true),
(4, 'MMT',  'Mạng máy tính và Truyền thông', true)
ON CONFLICT (code) DO NOTHING;

-- 2. TÀI KHOẢN NGƯỜI DÙNG (USERS)
-- password_hash là bcrypt của 'Admin123!'
INSERT INTO users (id, email, password_hash, full_name, role, enabled, department_id) VALUES
-- Admin hệ thống
(1, 'admin@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'Quản trị hệ thống', 'ADMIN', true, 1),
-- Trưởng khoa
(2, 'dean@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'TS. Nguyễn Văn An (Trưởng khoa)', 'DEAN', true, 1),
-- Giảng viên
(3, 'gv1@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'ThS. Trần Thị Mai', 'LECTURER', true, 1),
(4, 'gv2@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'TS. Lê Hoàng Nam', 'LECTURER', true, 2),
(5, 'gv3@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'ThS. Phạm Quốc Bảo', 'LECTURER', true, 3),
(6, 'gv4@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'TS. Vũ Minh Tuấn', 'LECTURER', true, 4),
(7, 'gv5@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'ThS. Đỗ Thu Hà', 'LECTURER', true, 1),
-- Sinh viên
(8, 'sv1@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'Nguyễn Văn Hùng', 'STUDENT', true, 1),
(9, 'sv2@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'Lê Thị Bích', 'STUDENT', true, 1),
(10, 'sv3@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'Trần Minh Quang', 'STUDENT', true, 2),
(11, 'sv4@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'Phạm Hải Đăng', 'STUDENT', true, 3),
(12, 'sv5@fit.edu.vn', '{bcrypt}$2a$10$.qoBCXd9xd10EKlFIZnfyeU9xfNoOmjLp5TUyV6W84HOS5yd3o2/a', 'Đặng Thu Trang', 'STUDENT', true, 4)
ON CONFLICT (email) DO UPDATE SET 
    password_hash = EXCLUDED.password_hash,
    full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    department_id = EXCLUDED.department_id;

-- Đồng bộ sequence cho bảng users và departments
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('departments_id_seq', (SELECT MAX(id) FROM departments));

-- 3. ĐỢT ĐĂNG KÝ (REGISTRATION PERIODS)
INSERT INTO registration_periods (id, name, round_type, gv_start, gv_end, sv_start, sv_end, gvpb_deadline, council_report_date) VALUES
(1, 'Đợt đăng ký KLTN Học kỳ 1 2026-2027', 'KLTN', 
 '2026-09-01 00:00:00', '2026-10-31 23:59:59', 
 '2026-10-01 00:00:00', '2026-11-30 23:59:59', 
 '2026-12-15 23:59:59', '2026-12-25 08:00:00')
ON CONFLICT (id) DO NOTHING;

SELECT setval('registration_periods_id_seq', (SELECT MAX(id) FROM registration_periods));

-- 4. ĐỀ TÀI (TOPICS)
INSERT INTO topics (id, title, description, status, round_id, department_id, created_by_id) VALUES
(1, 'Xây dựng Hệ thống Quản lý đề tài tốt nghiệp sinh viên theo mô hình Microservices', 
 'Phát triển nền tảng web phục vụ quản lý đồ án, chấm điểm hội đồng và thông báo tự động cho Khoa CNTT.', 
 'PUBLISHED', 1, 1, 3),
(2, 'Ứng dụng Học sâu và Thị giác máy tính trong Hệ thống Điểm danh sinh viên', 
 'Nhận diện khuôn mặt thời gian thực sử dụng YOLO và FaceNet tích hợp camera lớp học.', 
 'PUBLISHED', 1, 2, 4),
(3, 'Nghiên cứu và Triển khai giải pháp Giám sát an toàn thông tin mạng sử dụng ELK Stack', 
 'Thu thập log, phân tích hành vi bất thường và cảnh báo sớm các cuộc tấn công DDoS.', 
 'PUBLISHED', 1, 4, 6),
(4, 'Xây dựng ứng dụng Đặt phòng và Quản lý khách sạn thông minh', 
 'Hệ thống hỗ trợ thanh toán trực tuyến, quản lý buồng phòng và đề xuất phòng tự động.', 
 'PENDING', 1, 1, 7)
ON CONFLICT (id) DO NOTHING;

SELECT setval('topics_id_seq', (SELECT MAX(id) FROM topics));

-- 5. GIẢNG VIÊN HƯỚNG DẪN ĐỀ TÀI (TOPIC ADVISORS)
INSERT INTO topic_advisors (topic_id, advisor_id) VALUES
(1, 3), -- Đề tài 1: ThS. Trần Thị Mai hướng dẫn
(2, 4), -- Đề tài 2: TS. Lê Hoàng Nam hướng dẫn
(3, 6), -- Đề tài 3: TS. Vũ Minh Tuấn hướng dẫn
(4, 7)  -- Đề tài 4: ThS. Đỗ Thu Hà hướng dẫn
ON CONFLICT (topic_id, advisor_id) DO NOTHING;

-- 6. NHÓM SINH VIÊN (STUDENT GROUPS)
INSERT INTO student_groups (id, name, round_id, leader_id) VALUES
(1, 'Nhóm Kỹ sư Phần mềm 01', 1, 8),
(2, 'Nhóm AI & Vision', 1, 10)
ON CONFLICT (round_id, name) DO NOTHING;

SELECT setval('student_groups_id_seq', (SELECT MAX(id) FROM student_groups));

-- Thành viên nhóm
INSERT INTO student_group_members (group_id, student_id) VALUES
(1, 8), -- sv1
(1, 9), -- sv2
(2, 10) -- sv3
ON CONFLICT (group_id, student_id) DO NOTHING;

-- 7. ĐĂNG KÝ ĐỀ TÀI CỦA NHÓM (GROUP TOPIC REGISTRATION)
INSERT INTO group_topic_registrations (id, group_id, topic_id, status, reviewed_by) VALUES
(1, 1, 1, 'APPROVED', 2),
(2, 2, 2, 'PENDING', NULL)
ON CONFLICT (group_id) DO NOTHING;

SELECT setval('group_topic_registrations_id_seq', (SELECT MAX(id) FROM group_topic_registrations));

-- 8. HỘI ĐỒNG BẢO VỆ (COUNCILS - TV4)
INSERT INTO councils (id, name, round_id, location, report_date, status) VALUES
(1, 'Hội đồng KLTN số 01 - Bộ môn CNPM', 1, 'Phòng Hội đồng A2-301', '2026-12-25 08:00:00', 'ACTIVE')
ON CONFLICT (round_id, name) DO NOTHING;

SELECT setval('councils_id_seq', (SELECT MAX(id) FROM councils));

-- Thành viên Hội đồng (Quy định: 3-5 GV, không trùng GVHD là gv1)
INSERT INTO council_members (council_id, lecturer_id, role) VALUES
(1, 4, 'PRESIDENT'), -- TS. Lê Hoàng Nam (Chủ tịch)
(1, 5, 'SECRETARY'), -- ThS. Phạm Quốc Bảo (Thư ký)
(1, 6, 'MEMBER')     -- TS. Vũ Minh Tuấn (Ủy viên)
ON CONFLICT (council_id, lecturer_id) DO NOTHING;

-- Phân công Hội đồng & Giảng viên phản biện cho Đề tài 1
INSERT INTO topic_assignments (id, topic_id, council_id, reviewer_id) VALUES
(1, 1, 1, 7) -- GVPB: ThS. Đỗ Thu Hà
ON CONFLICT (topic_id) DO NOTHING;

SELECT setval('topic_assignments_id_seq', (SELECT MAX(id) FROM topic_assignments));

-- 9. THÔNG BÁO CHÍNH THỨC (NOTIFICATIONS - TV4)
INSERT INTO notifications (id, title, content, target_role, pinned, published, created_by_id) VALUES
(1, 'Thông báo kế hoạch nộp báo cáo và bảo vệ KLTN Học kỳ 1 (2026-2027)', 
 'Khoa Công nghệ Thông tin thông báo lịch bảo vệ Khóa luận tốt nghiệp dự kiến bắt đầu từ ngày 25/12/2026. Yêu cầu tất cả sinh viên nộp đầy đủ báo cáo đúng thời hạn quy định.', 
 'ALL', true, true, 2),
(2, 'Nhắc nhở nộp báo cáo tiến độ đợt 1 cho Giảng viên hướng dẫn', 
 'Các nhóm sinh viên lưu ý hoàn thiện chương 1 và chương 2 của đề cương để gửi GVHD kiểm tra trước ngày 15/11/2026.', 
 'STUDENT', false, true, 3)
ON CONFLICT (id) DO NOTHING;

SELECT setval('notifications_id_seq', (SELECT MAX(id) FROM notifications));
