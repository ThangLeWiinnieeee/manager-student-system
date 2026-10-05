# Quy ước JSP

Mỗi module Backend trả về view trong thư mục cùng tên:

| Java package | JSP directory | Phụ trách |
|---|---|---|
| `auth`, `user`, `department` | `auth`, `profile`, `admin` | TV1 |
| `registrationperiod` | `registration-period` | TV2 |
| `topic` | `topic` | TV2 |
| `studentgroup` | `student-group` | TV3 |
| `submission` | `submission` | TV3 |
| `council` | `council` | TV4 |
| `evaluation` | `evaluation` | TV4 |
| `result` | `result` | TV4 |
| `notification` | `notification` | TV4 |

Tên file thống nhất: `list.jsp`, `form.jsp`, `detail.jsp`. Thành phần dùng chung đặt trong `common`; không viết SQL hoặc nghiệp vụ trực tiếp trong JSP.
