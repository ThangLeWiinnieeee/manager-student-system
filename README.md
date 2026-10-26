# Hệ thống quản lý đề tài sinh viên

Ứng dụng Spring Boot/JSP/PostgreSQL quản lý đợt đăng ký, đề tài, nhóm sinh viên, báo cáo, phân công giảng viên và công bố kết quả của Khoa CNTT.

## Luồng quản lý

1. Trưởng khoa/quản trị viên mở **đợt chung**, chỉ chọn loại và lịch GV/SV; hạn phản biện cho TLCN/KLTN và ngày hội đồng cho KLTN. Tên đợt được tạo tự động. Trong giai đoạn GV, giảng viên đăng ký **môn/lớp** vào đợt, chọn bộ môn, giới hạn **1–5 SV/nhóm** và hình thức đề tài; người đăng nhập là GV phụ trách. Không thêm bước duyệt môn/lớp. GV không đổi lịch chung và chỉ sửa đăng ký môn/lớp trong giai đoạn GV.
2. Với danh sách có sẵn, GV tạo đề tài có nội dung và 1–2 GVHD; Trưởng khoa duyệt và công bố. Với hình thức tự đề xuất, SV nhập tên/nội dung thay vì chọn một đề tài có tên trống.
3. GV chia sẻ **mã tham gia lớp đề tài** tại trang chi tiết môn/lớp. Trong giai đoạn SV, sinh viên nhập mã ở **Nhóm của tôi**, sau đó tạo nhóm hoặc nhận lời mời và tự xác nhận vào nhóm. Chỉ SV đã tham gia cùng lớp mới được mời vào nhóm; đây không phải đăng ký học phần. Mỗi SV chỉ thuộc **một nhóm trong cùng môn/lớp của đợt**; được tham gia nhóm ở các lớp khác trong cùng đợt hoặc ở đợt khác.
4. Nhóm trưởng chọn đề tài. `EXCLUSIVE`: nhận ngay và ẩn khỏi danh sách chọn của nhóm khác; yêu cầu đồng thời chỉ có một nhóm thắng. `SHARED`: nhiều nhóm cùng nhận một đề tài. `SELF_PROPOSED`: tên trùng được phép, mỗi nhóm có đề xuất riêng chờ Khoa duyệt nội dung.
5. Nhóm trưởng nộp PDF; Khoa phân công chấm **riêng từng nhóm**. Sau khi có phiếu chấm, báo cáo và người chấm bị khóa. KLTN được bổ sung hội đồng sau khi GVPB đã đồng ý bảo vệ.
6. Chấm và công bố theo loại:

| Loại | Trình tự và điểm cuối |
|---|---|
| Môn học | Một GV độc lập chấm → Khoa xác nhận và công bố |
| TLCN | Khoa chọn một GVHD làm **GV hỗ trợ** chấm trước → GVPB khác người chấm sau → (GV hỗ trợ + GVPB) / 2 → Khoa công bố |
| KLTN | GVPB chấm, chọn rõ **được/không được bảo vệ**. Không được bảo vệ cần lý do, điểm cuối = 0 chờ Khoa công bố. Được bảo vệ → Khoa xếp hội đồng → hội đồng chấm → Chủ tịch xác nhận → Khoa công bố; điểm = (GVPB + trung bình hội đồng) / 2 |
| NCKH | Hội đồng nghiệm thu chấm → Chủ tịch xác nhận → Khoa công bố trung bình điểm hội đồng; chưa tự đặt ngưỡng đạt/xếp loại |

Hội đồng có 3–5 GV, đúng một chủ tịch và một thư ký. GVHD chỉ được chấm ở lượt hỗ trợ TLCN, không làm GVPB hoặc thành viên hội đồng của đề tài mình hướng dẫn. GVPB KLTN khác thành viên hội đồng để không tính hai lần điểm một người. Điểm thiếu không được coi là 0; điểm cuối làm tròn hai chữ số. Hạn GVPB TLCN/KLTN vẫn được kiểm tra. Phiếu chấm dùng thang thành phần 4–3–3 hiện có.

### Test nhanh luồng mới

- GV: vào /course-registrations → chi tiết lớp → lấy/tạo mã tham gia. Đổi mã vô hiệu mã cũ; đóng mã ngăn người mới, không loại SV đã tham gia.
- SV: vào /student/my-groups → nhập mã → tạo nhóm. Thử thêm SV chưa nhập mã, thêm quá số người và tạo nhóm thứ hai cùng lớp: phải bị chặn.
- Khoa: vào /admin/councils/assignments → chọn lớp → phân công theo loại. TLCN chọn GV hỗ trợ trong các GVHD; NCKH chỉ chọn hội đồng; KLTN ban đầu chỉ chọn GVPB.
- GV: vào /lecturer/evaluations → nộp điểm. TLCN thử chấm phản biện trước lượt hỗ trợ; KLTN thử nộp khi chưa chọn quyết định bảo vệ: phải bị chặn.
- KLTN được bảo vệ: Khoa quay lại phân công để thêm hội đồng. Ví dụ GVPB 10, ba thành viên hội đồng đều 6 → điểm cuối **8.00**.
- KLTN không được bảo vệ: lưu điểm/nhận xét GVPB, Khoa xác nhận **0.00** tại /dean/results; không cần hội đồng.
- SV chỉ xem được điểm tại /student/my-results sau khi Khoa công bố; các nhóm làm cùng đề tài có kết quả riêng.

### Xác nhận nhóm và bàn giao nhiệm vụ

- Nhóm trưởng/GV gửi **lời mời** tại chi tiết nhóm. SV đồng ý hoặc từ chối ở **Nhóm của tôi**; lời mời chưa chiếm chỗ hay giữ SV khỏi nhóm khác. Khi đồng ý, hệ thống kiểm tra lại thời hạn, giới hạn người và quy tắc một nhóm/lớp. Nhóm trưởng có thể thu hồi lời mời. Cần xử lý hết lời mời trước khi nhận đề tài để tránh khóa nhóm khi các bạn chưa vào đủ.
- GV phụ trách/quản trị viên dùng **Chọn trưởng** tại chi tiết nhóm để chuyển quyền cho thành viên hiện hữu, kể cả sau khi nhận đề tài; không thêm/bớt thành viên và không đổi người nộp trên báo cáo cũ. Không chuyển sau khi kết quả đã chốt.
- Khoa/quản trị viên mở **Môn/lớp → Chi tiết → Bàn giao giảng viên**. Chọn nhiệm vụ, người nhận và lý do. Đổi GV phụ trách chuyển quyền quản lý lớp/đề tài nháp, không đổi người tạo lịch sử và không tự thay GVHD. Bàn giao GVHD chuyển cả lượt hỗ trợ TLCN chưa nộp nếu có.
- Có thể thay người chấm đang có phiếu nháp: bản nháp cũ được giữ dưới người cũ, không tính vào điểm hiện hành và không chuyển cho GV mới. Không thay người đã nộp điểm hoặc nhóm đã chốt kết quả. Người từng có phiếu chấm của nhóm không được nhận lại nhiệm vụ qua luồng này. GVHD của đề tài dùng chung có nhóm đã chốt không được đổi để bảo toàn hồ sơ lịch sử.
- Thay thành viên hội đồng áp dụng riêng nhóm được chọn: sao chép hội đồng với cùng vai trò, lịch và địa điểm, chỉ đổi người chưa nộp điểm. Các nhóm khác và điểm của các thành viên còn lại giữ nguyên. Lịch sử ghi người bàn giao, người cũ/mới, lý do và thời gian.
- V19 chỉ thêm bảng lời mời và lịch sử; thành viên hiện hữu không phải xác nhận lại. Khởi động lại ứng dụng để Flyway áp dụng migration; không cần chạy lại seed.

### URL chính

Khoa có thể xóa đợt chưa kết thúc và chưa có môn/lớp đăng ký. Môn/lớp đã có đề tài, nhóm hoặc hội đồng không được xóa. Lịch chung áp dụng trực tiếp cho mọi môn/lớp; không thể đổi loại đợt sau khi có môn/lớp tham gia.

| Chức năng | Đường dẫn |
|---|---|
| Bộ môn | `/admin/departments` |
| Đợt chung của Khoa (GV được xem) | `/admin/registration-periods` |
| GV đăng ký và quản lý môn/lớp | `/course-registrations` |
| Đề tài | `/topics` |
| Nhóm của sinh viên | `/student/my-groups` |
| Duyệt đề xuất nhóm | `/dean/group-topic-registrations` |
| Phân công chấm theo nhóm | `/admin/councils/assignments` |
| Giảng viên chấm điểm | `/lecturer/evaluations` |
| Khoa công bố kết quả | `/dean/results` |
| Kết quả của sinh viên | `/student/my-results` |

### Nâng cấp database

Flyway tự chạy V13–V19 khi khởi động. V13 khôi phục bảng `departments`, giữ ID và dữ liệu, bổ sung cấu hình môn/lớp và ràng buộc thành viên. V14 chuyển dữ liệu chấm điểm sang `group_assignments`, `group_evaluations`, `group_results` theo đăng ký của nhóm. V15 bỏ ràng buộc vai trò trên cột chuyên ngành lịch sử để quản trị viên vẫn đổi vai trò tài khoản bình thường. Bảng điểm cũ được giữ làm lịch sử; điểm đã công bố không bị tính lại.

V16 chuyển lịch cũ sang `registration_windows`; bảng `registration_periods` giữ ID môn/lớp và các khóa `round_id` lịch sử để nhóm/đề tài/điểm không bị đổi liên kết. Mỗi bản ghi cũ có một đợt chung riêng, không tự gộp lịch. Dừng ứng dụng cũ trước khi chạy V16.

V17 thêm mã tham gia, danh sách SV thuộc lớp và dữ liệu cho các luồng đánh giá mới. Thành viên nhóm hiện hữu tự được ghi nhận đã tham gia lớp; GV tạo mã cho lớp cũ tại trang chi tiết. Các hồ sơ **đã phân công trước V17** tiếp tục quy trình hội đồng/trung bình cũ để không thay quy tắc giữa lúc chấm; màn hình đánh dấu rõ. Hồ sơ cũ chưa có phiếu chấm/kết quả có thể được Khoa phân công lại theo quy trình mới. Điểm đã chốt được giữ nguyên. Dừng phiên bản cũ trước khi nâng cấp.

V18 thêm phiên bản quyền tài khoản. Khóa/mở lại, đổi vai trò hoặc email làm phiên đăng nhập cũ hết hiệu lực ở yêu cầu tiếp theo; sửa họ tên/bộ môn không bắt đăng nhập lại. Tài khoản đang phụ trách lớp còn mở đăng ký hoặc tham gia nhóm chưa chốt kết quả không được khóa/đổi role. Kết quả đã chốt vẫn xem và công bố được khi GV liên quan ngừng hoạt động. Lớp có SV đã tham gia, đề tài, nhóm hoặc hội đồng không được đổi các thông tin đã thiết lập; vẫn cho bổ sung thông tin thiếu của lớp lịch sử.

Các đợt cũ từ V12 giữ hình thức `SHARED`, tối đa 5 người, mã lớp `LEGACY`. Khoa cần mở **Môn/lớp đăng ký → Sửa** để bổ sung đúng môn/lớp, GV phụ trách và bộ môn; hệ thống không tự đoán giảng viên. Nếu đã có nhóm, không đổi hình thức và giới hạn nhóm. Dừng phiên bản cũ trước khi chạy phiên bản mới vì tên bảng đã thay đổi. Không sửa migration đã áp dụng.

`seed_data.sql` chỉ dành cho database demo mới sau migration; không chạy lên dữ liệu đang sử dụng.

## Cấu trúc dự án

Đây là một Maven module Spring MVC duy nhất:

- Backend Java: [`src/main/java`](src/main/java).
- Frontend JSP: [`src/main/webapp/WEB-INF/views`](src/main/webapp/WEB-INF/views).
- CSS/JavaScript/hình ảnh: [`src/main/resources/static`](src/main/resources/static).
- PostgreSQL migration: [`src/main/resources/db/migration`](src/main/resources/db/migration).
- Kiểm thử: [`src/test`](src/test).

Xem [cấu trúc chi tiết](docs/PROJECT_STRUCTURE.md) và [phân chia module cho nhóm](docs/TEAM_MODULES.md).

Luồng MVC:

```text
JSP/CSS/JavaScript → Controller → Service → Repository → PostgreSQL
```

## Công nghệ

- Java 21, Spring Boot 3.5, Spring MVC, Spring Security
- JSP/JSTL, CSS thuần
- Spring Data JPA, PostgreSQL, Flyway
- Maven, executable WAR, Tomcat

## Chạy ứng dụng

Trên Windows với VS Code, điền cấu hình trong `.env`, mở project và chạy tại terminal:

```powershell
.\run.ps1
```

Script dùng Java/Maven trong `.build-tools` nếu có, nạp `.env` và bật chế độ phát triển. Địa chỉ web theo `SERVER_PORT` trong `.env` (mặc định `http://localhost:8080`). Dừng bằng **Ctrl+C**.

- Cần Java Extension Pack của VS Code và mở project ở chế độ Java **Standard**. Workspace đã bật tự biên dịch: lưu file Java hợp lệ → VS Code biên dịch → Spring Boot DevTools tự restart. Lần đầu thêm DevTools cần dừng phiên chạy cũ và chạy lại lệnh trên.
- Sửa JSP/CSS/JavaScript rồi nhấn **F5** trên trình duyệt. Script đọc trực tiếp tài nguyên nguồn, không cần đóng gói WAR mỗi lần sửa giao diện.
- Nếu IDE chưa biên dịch hoặc bạn dùng editor khác, chạy `mvn compile` ở terminal thứ hai để kích hoạt restart. Sửa `.env` hoặc `pom.xml` thì dừng và chạy lại script.
- Không cần cài LiveReload để tự restart server; tự tải lại trình duyệt cần tiện ích LiveReload tương thích. DevTools không được đưa vào WAR đóng gói mặc định.

Xem cơ chế [DevTools](https://docs.spring.io/spring-boot/3.5/reference/using/devtools.html). Cách chạy thủ công bên dưới dành cho máy đã có Java/Maven:

Yêu cầu: Java 21, Maven 3.6.3+ và PostgreSQL.

1. Khởi động PostgreSQL:

   ```bash
   docker compose up -d postgres
   ```

2. Cấu hình biến môi trường. `ADMIN_PASSWORD` bắt buộc ở lần chạy đầu để tạo tài khoản quản trị:

   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/student_topic_management"
   $env:DB_USERNAME="postgres"
   $env:DB_PASSWORD="postgres"
   $env:ADMIN_EMAIL="admin@fit.edu.vn"
   $env:ADMIN_PASSWORD="YourStrongAdminPassword123!"
   ```

3. Chạy ứng dụng:

   ```bash
   mvn spring-boot:run
   ```

4. Mở `http://localhost:8080` và đăng nhập bằng email quản trị vừa cấu hình.

Có thể build hoàn toàn bằng Docker nếu máy chưa có Java 21/Maven:

```bash
docker build -t student-topic-management .
```

## Kiểm thử và đóng gói

```bash
mvn test
node src/test/js/registration-period.test.cjs
mvn clean package
java -jar target/student-topic-management.war
```

Test dùng H2 trong bộ nhớ và SMTP giả lập, không cần kết nối database/email thật. Flyway chạy migration trong `db/migration/common` và thư mục tương ứng loại database (`postgresql` hoặc `h2`). Bản H2 của V4 chỉ có trong `src/test/resources`; khi đổi dữ liệu seed cần giữ hai bản đồng bộ. Sau khi chuyển vị trí migration, chạy `mvn clean test` để xóa resource cũ trong `target`.

`ApplicationWebTest` khởi động Tomcat trên cổng ngẫu nhiên và gửi HTTP thật để kiểm tra JSP, đăng nhập, phân quyền, quản lý tài khoản/bộ môn và đổi mật khẩu. `PasswordRecoveryTest` kiểm tra luồng OTP với SMTP giả lập. Cách chọn migration theo database dùng cấu hình `{vendor}` của [Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway).

Trên Windows, nếu đã có Java/Maven trong `.build-tools`, có thể chạy:

```powershell
$env:JAVA_HOME = Join-Path $PWD '.build-tools\jdk\jdk-21.0.12.1+1'
& .\.build-tools\maven\apache-maven-3.9.11\bin\mvn.cmd clean verify
```

## Phân quyền

| Vai trò | Quyền chính |
|---|---|
| `ADMIN` | Quản lý tài khoản; bộ môn, mọi đợt; duyệt đề tài/đề xuất, phân công và công bố điểm |
| `DEAN` | Bộ môn, mọi đợt; xem nhóm phục vụ duyệt đề tài/đề xuất, phân công và công bố điểm; không quản lý tài khoản hoặc trực tiếp tạo/sửa/xóa nhóm, thay thành viên/nhóm trưởng |
| `LECTURER` | Đăng ký môn/lớp trong đợt đang mở, quản lý lớp phụ trách, đề tài của mình, hướng dẫn và chấm nhóm được phân công |
| `STUDENT` | Nhóm của mình, đăng ký đề tài, báo cáo và kết quả đã công bố |

`CourseWorkflowTest` kiểm tra giới hạn nhóm, đăng ký đồng thời, ba hình thức, quyền truy cập, báo cáo và điểm riêng cho các nhóm cùng đề tài; đồng thời mở các JSP qua HTTP thật. `CourseMigrationTest` kiểm tra giữ dữ liệu khi nâng từ V12 lên V16.
