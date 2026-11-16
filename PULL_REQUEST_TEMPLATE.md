## 1. Summary / Mô tả thay đổi
<!-- Tóm tắt ngắn gọn chức năng bạn vừa hoàn thành hoặc lỗi vừa sửa -->
- **Nội dung thực hiện:** 
- **Mã chức năng / Quy tắc liên quan:** (VD: `FR-01`, `BR-02`, `TC-01`...)

---

## 2. Type of Change / Loại thay đổi
<!-- Đánh dấu [x] vào ô phù hợp -->
- [ ] `feat`: Thêm chức năng / nghiệp vụ mới (New feature)
- [ ] `fix`: Sửa lỗi logic hoặc lỗi biên dịch (Bug fix)
- [ ] `refactor`: Tái cấu trúc mã nguồn, tối ưu code (Code refactoring)
- [ ] `docs`: Cập nhật tài liệu hoặc SQL script (Documentation / SQL)
- [ ] `chore`: Cập nhật cấu hình dự án / `.gitignore` (Build / Config)

---

## 3. Affected Modules / Phân hệ ảnh hưởng
- [ ] `model` (Thực thể, Enum, Record, Validation)
- [ ] `service` (Xử lý nghiệp vụ)
- [ ] `repository` (Lưu trữ RAM / File `.dat` / JDBC SQL Server)
- [ ] `ui` (`ConsoleUI`, `Input`, định dạng hiển thị)
- [ ] `sql` / `docs` (Database scripts, tài liệu)

---

## 4. Definition of Done Checklist / Kiểm tra trước khi gửi duyệt
<!-- Vui lòng tự kiểm tra và đánh dấu [x] vào tất cả các mục dưới đây trước khi tạo PR -->
- [ ] Mã nguồn biên dịch sạch, **không còn lỗi đỏ** trong IDE.
- [ ] Tuân thủ đúng kiến trúc phân tầng (không dùng `Scanner` hay `System.out.println` trong `service` và `model`).
- [ ] Đã kiểm thử các trường hợp biên (nhập sai định dạng, giá $\le 0$, trùng mã ID, vượt tồn kho...).
- [ ] Đặt tên Class (`PascalCase`), biến/hàm (`camelCase`) đúng quy chuẩn tiếng Anh của dự án.
- [ ] Đã chạy `git pull origin main` về nhánh cá nhân và xử lý toàn bộ xung đột (conflict) nếu có.

---

## 5. Test Evidence / Kết quả chạy thử (Console Output)
<!-- Dán kết quả chạy thử trên màn hình Console hoặc ghi chú các Test Case đã pass -->
```text
Dán output chạy thử tại đây (ví dụ: thêm thành công SP01, chặn thành công khi nhập giá âm...)
```