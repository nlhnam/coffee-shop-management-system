# QUY TRÌNH LÀM VIỆC NHÓM & QUY CHUẨN GIT (CONTRIBUTING)

Tài liệu này quy định cách thức phối hợp, phân nhánh Git, quy chuẩn viết mã và kiểm duyệt Pull Request cho **5 thành viên** phát triển dự án **Coffee Shop Management & POS System**.

---

## 1. Cấu Trúc Nhánh & Phân Công Thành Viên

* **`main`:** Nhánh chính thức luôn ở trạng thái biên dịch sạch và vượt qua 100% bài kiểm thử trong `SystemTestRunner`. **Không push trực tiếp tính năng chưa kiểm thử lên `main`.**
* **Nhánh tính năng cá nhân:** Mỗi thành viên phát triển trên nhánh riêng tách từ `main`:

| Thành viên | Nhánh Git | Phạm vi tệp nguồn phụ trách |
| :--- | :--- | :--- |
| **Nam** *(Tech Lead)* | `feat/sales` | `Main.java`, `ConsoleUI.java`, `SalesService.java`, `InMemoryInvoiceRepository.java`, `FileInvoiceRepository.java`, `JdbcInvoiceRepository.java`, `MigrationService.java` |
| **Thiện** | `feat/catalog` | `CatalogMenu.java`, `CatalogService.java`, `InMemoryCategoryRepository.java`, `InMemoryProductRepository.java`, `FileCategoryRepository.java`, `FileProductRepository.java`, `JdbcCategoryRepository.java`, `JdbcProductRepository.java` |
| **Tuấn** | `feat/staff-ui` | `Input.java`, `StaffMenu.java`, `StaffService.java`, `SampleData.java`, `InMemoryStaffRepository.java`, `SystemTestRunner.java`, truy vấn phân trang hóa đơn SQL |
| **Bình** | `feat/report` | `Page.java`, `Criteria.java`, `ReportService.java`, `ReportMenu.java` |
| **Tín** | `feat/file-core`<br>`feat/db-core` | `repository/file/FileRepository.java`, `repository/file/FileStaffRepository.java`, `sql/01_schema.sql`, `sql/02_sample_data.sql`, `repository/jdbc/DBConnect.java`, `repository/jdbc/SqlFilter.java`, `repository/jdbc/JdbcStaffRepository.java`, `annotation/Author.java`, tài liệu dự án |

---

## 2. Quy Trình Phát Triển & Mở Pull Request

### Bước 1: Đồng bộ `main` và tạo/cập nhật nhánh cá nhân
```bash
git checkout main
git pull origin main
git checkout -b feat/<ten-nhanh-cua-ban>
```
*(Nếu nhánh đã tồn tại, chạy `git checkout feat/<ten-nhanh-cua-ban>` rồi `git merge main` để cập nhật code mới nhất của cả nhóm.)*

### Bước 2: Phát triển và kiểm thử cục bộ
1. Tuân thủ chuẩn mã hóa **UTF-8** cho tất cả tệp `.java`, `.sql`, `.md`.
2. Biên dịch và chạy bộ kiểm thử tự động trước khi commit:
   ```bash
   java -Dfile.encoding=UTF-8 -cp bin app.Main --test
   ```
3. Đảm bảo không commit các thư mục/tệp cục bộ đã khai báo trong `.gitignore` (`bin/`, `*.class`, `.vscode/`, `data/`, `data-test/`, `reports/`, `db.properties`).

### Bước 3: Commit theo chuẩn Conventional Commits
Sử dụng tiền tố rõ ràng để dễ theo dõi lịch sử đóng góp của từng thành viên:
* `feat(<scope>): ...` — Thêm chức năng mới (ví dụ: `feat(file): xay dung FileRepository generic va co che backup .bak`)
* `fix(<scope>): ...` — Sửa lỗi logic hoặc lỗi biên dịch
* `test(<scope>): ...` — Bổ sung hoặc cập nhật ca kiểm thử trong `SystemTestRunner`
* `docs(<scope>): ...` — Cập nhật tài liệu `.md` hoặc kịch bản `.sql`
* `chore(<scope>): ...` — Cấu hình `.gitignore`, thư viện `lib/`, cấu trúc dự án

### Bước 4: Đẩy nhánh và tạo Pull Request
```bash
git push -u origin feat/<ten-nhanh-cua-ban>
```
Mở Pull Request trên GitHub (`base: main` ← `compare: feat/<ten-nhanh-cua-ban>`), điền đầy đủ nội dung theo mẫu `PULL_REQUEST_TEMPLATE.md` và gắn **Nam** (`@nlhnam`) làm Reviewer.

---

## 3. Quy Chuẩn Viết Mã (Coding Conventions)

1. **Đặt tên chuẩn Java:**
   * Tên lớp, interface, enum, record: `PascalCase` (`FileRepository`, `InvoiceSummary`).
   * Tên phương thức, biến: `camelCase` (`revenueLastDays`, `addStock`).
   * Hằng số: `UPPER_SNAKE_CASE` (`DT_FMT`).
2. **Xử lý Ngoại lệ (Exception Handling):**
   * Tuyệt đối không nuốt lỗi (`catch (Exception e) {}`).
   * Sử dụng đúng hệ thống ngoại lệ của dự án: `NotFoundException`, `DuplicateIdException`, `BusinessException`, `DataAccessException`.
3. **Định dạng hiển thị (`NFR-06`):**
   * Tiền tệ: `String.format("%,.0f đ", amount)`
   * Ngày giờ: `DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")`
4. **Gắn siêu dữ liệu tác giả (`@Author`):**
   * Mỗi thành viên gắn annotation `@Author(name = "...", branch = "...", task = "...")` lên các lớp chính do mình trực tiếp xây dựng để phục vụ nghiệm thu và đối chiếu đóng góp.