# BÀN GIAO TV2 → TV3 — Module "Đợt đăng ký + Đề tài"

> Người bàn giao: **Thanh Lâm (TV2)** · Ngày: 12/10/2026
> Người nhận: **Văn Minh (TV3)** — Module đăng ký đề tài của nhóm sinh viên

---

## 1. TV2 đã làm gì

### 1.1. Database (Flyway — TUYỆT ĐỐI KHÔNG SỬA FILE CŨ)

DB Supabase chung đã chạy tới **version 5**. Trên main có V1–V4 của TV1;
TV2 bổ sung **V5** (nhánh này): `V5__create_registration_and_topic_schema.sql`
= 3 bảng `registration_periods`, `topics`, `topic_advisors`.

Sau merge, `db/migration` phải đủ V1→V5. DB chung ĐÃ có v5 nên Flyway không chạy lại.
**Migration tiếp theo của TV3 = V6** (kiểm tra ledger:
`SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;` — chỉ SELECT).

### 1.2. Schema sẵn có (TV3 dùng trực tiếp)

```sql
registration_periods(id, name, round_type CHECK(MON_HOC|NCKH|TLCN|KLTN),
    gv_start, gv_end, sv_start, sv_end,
    gvpb_deadline,        -- hạn GVPB nộp điểm (bắt buộc với TLCN/KLTN)
    council_report_date,  -- ngày báo cáo hội đồng (bắt buộc với KLTN)
    created_at timestamptz)

topics(id, title vc200, description vc2000,
    status CHECK(DRAFT|PENDING|APPROVED|REJECTED|PUBLISHED),
    rejection_reason vc500, round_id → registration_periods,
    department_id → departments, created_by_id → users,
    created_at/updated_at timestamptz)

topic_advisors(topic_id, advisor_id, PK(topic_id, advisor_id))  -- 1–2 GVHD
```

### 1.3. Java (package `vn.edu.fit.topicmanagement`)

- `registrationperiod/`: `RegistrationPeriod` (có `isGvWindowOpen()`, `isSvWindowOpen()`,
  `getStage()`, các `getXxxText()`), `RegistrationPeriodForm`, `RegistrationPeriodService`,
  `RegistrationPeriodRepository`, `RoundType`, `RegistrationPeriodController`
  (`/admin/registration-periods`, @PreAuthorize ADMIN|DEAN).
- `topic/`: `Topic`, `TopicStatus` (`canTransitionTo`), `TopicForm`, `RejectForm`,
  `TopicService`, `TopicRepository` (`search(...)` — lọc + phân trang), `TopicController` (`/topics`).
- **Dành sẵn cho TV3:** `assertSvWindowOpen(roundId)` trong `RegistrationPeriodService` —
  gọi khi nhóm SV đăng ký đề tài; ném `BusinessRuleException` kèm mốc thời gian.

### 1.4. Quy tắc đã cài (TV3 KHÔNG cài lại)

1. Vòng đời (`TopicStatus.canTransitionTo`): DRAFT|REJECTED → PENDING → APPROVED|REJECTED → PUBLISHED (kết thúc).
2. GV chỉ sửa/xóa đề tài CỦA MÌNH ở DRAFT|REJECTED (`requireOwner`, `requireEditable`).
3. GV chỉ gửi duyệt khi cửa sổ GV đang mở (`assertGvWindowOpen`); gửi lại sau REJECTED tự xóa lý do.
4. GVHD: 1–2 người, không trùng, phải LECTURER còn hoạt động.
5. Validation đợt: gvStart<gvEnd; svStart<svEnd; svStart ≥ gvEnd; gvpbDeadline chỉ TLCN/KLTN và >svEnd; councilReportDate chỉ KLTN và >gvpbDeadline.
6. Hiển thị: người thường chỉ thấy đề tài của mình + PUBLISHED (`search` qua `viewerEmail`).
7. Phân trang 10 dòng, sort `updatedAt` giảm dần, filter giữ query-string.

### 1.5. Views + CSS

- `registration-period/{list,form}.jsp`, `topic/{list,form,detail}.jsp`.
- Include cấp 1 tầng: `../common/header.jsp` (đếm số tầng = số lần `../`).
- CSS: `app.css` (custom, không Bootstrap). Badge: `badge active|warn|info|inactive`.
  Nút hành động: `inline-form` + hidden `_csrf`. Form đề tài 2 nút `name="action"`: `submit`/`save`.
- ⚠️ KHÔNG Format Document `header.jsp`/`footer.jsp` (fragment — format sẽ bồi thẻ đóng vỡ layout).

### 1.6. Test — 29/29 XANH

`mvn test`: contextLoads(1) + UserAccountServiceTest(6, TV1)

- RegistrationPeriodServiceTest(9) + TopicServiceTest(13).

### 1.7. Dữ liệu demo trên DB chung

- Đợt KLTN "Đợt đăng ký KLTN HK1 2026-2027" (cửa GV mở tới 31/10/2026) + vài đợt test.
- Đề tài mẫu đã PUBLISHED. Tài khoản test: GV `lecturer1@fit.edu.vn`,
  SV `maitt@fit.edu.vn` (mật khẩu `Password123!`), admin thường trực.
- TV3 được xóa/sửa dữ liệu test của TV2, nhưng KHÔNG đụng departments/users gốc của TV1.

---

## 2. TV3 cần làm gì (theo bảng phân công)

- Đăng ký đề tài của NHÓM SV (≤3 SV, trưởng nhóm thao tác): entity nhóm + migration **V6**
  (tên bảng thường-số-nhiều, chuẩn V5); 1 nhóm 1 đề tài/1 đợt.
- Dùng `assertSvWindowOpen` để chặn đăng ký ngoài sv_start–sv_end.
- Chỉ cho nhóm đăng ký đề tài đang PUBLISHED (TV2 đã đảm bảo dữ liệu đầu vào).
- Trạng thái đăng ký nhóm: đề xuất PENDING → APPROVED/REJECTED (khoa duyệt — d12).
- Trang đề tài công khai cho SV (d15): tái dùng `TopicRepository.search`.

## 3. Quy ước giữ nguyên

- Boot 3.5.16 + JSP/JSTL (`jakarta.tags.core`) + PostgreSQL; không đổi version.
- `*Form` + Jakarta Validation; PRG + flash `success`/`error`; CSRF mọi POST; `@PreAuthorize`;
  **không đụng repository của module khác** — TV3 gọi service của TV2.
- Migration mới = version kế tiếp, không sửa file cũ.

## 4. Rủi ro / việc còn treo

- Sau merge chạy `mvn clean test` (contextLoads validate đủ V1→V5 trên H2).
- Header JSP: giữ bản TV1 + 2 menu TV2 đã thêm (đã xử lý trong PR này).
- Bug nhỏ phát hiện: `footer.jsp` (file TV1) thiếu khai báo UTF-8 → tiếng Việt sai font
  ở footer mọi trang — đã ghi chú trong PR, chờ TV1 sửa.
