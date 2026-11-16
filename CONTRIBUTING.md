# Contributing Guidelines / Quy Chuẩn Đóng Góp Dự Án
**Project:** Coffee Shop Management & POS System (`coffee-shop-management-system`)  
**Course / Track:** MOB1024 – Java OOP, File I/O & SQL Server JDBC

Tài liệu này quy định quy trình làm việc trên Git, tiêu chuẩn viết mã và quy tắc phối hợp dành cho toàn bộ thành viên phát triển dự án.

---

## 1. Team Ownership & Branching Strategy / Phân Công & Chia Nhánh

Mọi thành viên **tuyệt đối không commit hoặc push trực tiếp lên nhánh `main`**. Tất cả thay đổi phải được thực hiện trên nhánh tính năng cá nhân và ghép vào `main` thông qua **Pull Request (PR)**.

| Thành viên | Vai trò | Nhánh làm việc (`Branch`) | Phân hệ & Mã chức năng (`FR`) |
| :--- | :--- | :--- | :--- |
| **Nam** | Tech Lead / Core | `feat/sales` | Kiến trúc khung, Bán hàng, Giỏ hàng, Hóa đơn (`FR-05`, `FR-06`, `FR-13`, `FR-14`) |
| **Thiện** | Core Developer | `feat/catalog` | Danh mục, Sản phẩm, Nhập kho (`FR-01`, `FR-02`, `FR-07`, `FR-08`) |
| **Tuấn** | Core Developer | `feat/staff-ui` | Nhân sự, Phân ca, Tiện ích `Input`, `SampleData` (`FR-03`, `FR-04`, `FR-09`) |
| **Bình** | Data & Reporting | `feat/report` | Thống kê doanh thu, Xuất báo cáo HTML, SQL Script (`FR-10`, `FR-11`, `FR-12`) |

### Quy trình Git hằng ngày (Daily Git Workflow)
1. **Đồng bộ `main` trước khi bắt đầu code:**
   ```bash
   git checkout feat/<ten-nhanh-cua-ban>
   git pull origin main
   ```
2. **Code và kiểm thử cục bộ** trong phạm vi package được phân công.
3. **Commit theo từng đầu việc nhỏ** đã chạy ổn định và đẩy lên nhánh cá nhân:
   ```bash
   git add .
   git commit -m "feat(scope): mo ta ngan gon thay doi"
   git push origin feat/<ten-nhanh-cua-ban>
   ```
4. **Tạo Pull Request** trên GitHub (`base: main` <- `compare: feat/<ten-nhanh-cua-ban>`) và thông báo cho Leader kiểm duyệt.

---

## 2. Commit Message Convention / Quy Tắc Đặt Tên Commit

Sử dụng chuẩn **Conventional Commits** (tiếng Việt không dấu hoặc tiếng Anh ngắn gọn, rõ động từ):

```text
<type>(<scope>): <subject>
```

* **`feat`**: Thêm tính năng hoặc nghiệp vụ mới (VD: `feat(sales): implement cart item quantity accumulation`)
* **`fix`**: Sửa lỗi logic hoặc lỗi biên dịch (VD: `fix(catalog): prevent deleting category containing active products`)
* **`refactor`**: Tối ưu cấu trúc code nhưng không đổi nghiệp vụ (VD: `refactor(report): migrate revenue loop to Stream API`)
* **`docs`**: Cập nhật tài liệu, README, SQL script (VD: `docs(sql): add initial schema for cafe_pos database`)
* **`chore`**: Cấu hình `.gitignore`, dọn dẹp file rác (VD: `chore: ignore compiled class files and IDE configs`)

---

## 3. Coding Standards & Architecture Rules / Tiêu Chuẩn Lập Trình

### A. Kiến trúc phân tầng (`NFR-02`)
* **`ui` (`ConsoleUI`, `Input`)**: Chỉ chịu trách nhiệm hiển thị menu, định dạng bảng và nhận dữ liệu từ bàn phím. Không chứa logic tính tiền hay kiểm tra nghiệp vụ.
* **`service`**: Chứa toàn bộ xử lý nghiệp vụ và ràng buộc (`BR-01` – `BR-08`). **Tuyệt đối không** gọi `Scanner` hoặc `System.out.println` bên trong tầng `service` và `model`.
* **`repository`**: Chỉ đảm nhận đọc/ghi dữ liệu (RAM `Map`, File `.dat`, hoặc SQL Server `PreparedStatement`).
* **`model`**: Đóng gói dữ liệu, kiểm tra tính hợp lệ (`Validation`) ngay trong các phương thức `set...()` và ném ngoại lệ rõ nghĩa khi dữ liệu sai.

### B. Quy tắc đặt tên Java (Naming Conventions)
* **Class, Enum, Interface, Record:** `PascalCase` (VD: `InvoiceDetail`, `ProductStatus`, `DailyRevenue`).
* **Method, Variable:** `camelCase` (VD: `calculateTotalAmount()`, `discountPercent`).
* **Constant (`static final`):** `UPPER_SNAKE_CASE` (VD: `MAX_DISCOUNT_PERCENT = 50`).
* **Database & SQL:** `snake_case` (VD: `invoice_details`, `unit_price`).

### C. Ràng buộc dữ liệu bắt buộc
* **Không crash khi nhập sai (`NFR-01`)**: Mọi thao tác nhập số/ngày/mã từ console phải đi qua lớp tiện ích `Input`.
* **Định dạng hiển thị (`NFR-06`)**: Tiền tệ hiển thị dạng `35,000 đ`; thời gian hiển thị dạng `dd/MM/yyyy HH:mm`.
* **Chống SQL Injection (Giai đoạn 2)**: 100% truy vấn động phải sử dụng `PreparedStatement` với tham số `?`, tuyệt đối không nối chuỗi giá trị người dùng nhập vào câu lệnh SQL.

---

## 4. Code Review & Escalation / Duyệt Mã & Xử Lý Vướng Mắc

* **Tiêu chí hoàn thành (Definition of Done):** Code biên dịch sạch không lỗi, đã test các trường hợp biên (giá <= 0, bán vượt tồn kho, trùng mã ID, xóa dữ liệu đang có liên kết) và đã `git pull origin main` để xử lý hết xung đột trước khi mở PR.
* **Quy tắc 45 phút (Escalation Rule):** Nếu gặp lỗi logic hoặc xung đột Git xử lý quá **45 phút** chưa giải quyết được, thành viên cần chụp màn hình lỗi kèm đoạn mã liên quan gửi ngay lên nhóm chat và gắn thẻ `@Nam` để cùng tháo gỡ kịp thời.