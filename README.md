# coffee-shop-management-system
# Đặc tả yêu cầu – Đề tài 1: Quầy cà phê (POS)

> **Dành cho sinh viên.** Tài liệu này trả lời hai câu hỏi: *phần mềm phải làm được gì* (mục 1–7) và *làm theo thứ tự nào, dùng kiến thức slide nào* (mục 8).
> Mô hình dữ liệu chi tiết, script SQL và đoạn code gợi ý nằm trong đề [01-Quay-ca-phe.md](../01-Quay-ca-phe.md). Khung project chung nằm trong [00-Huong-dan-chung.md](../00-Huong-dan-chung.md).
> Nhóm chép file này vào `docs/01-dac-ta.md` của repo nhóm, điền tên thành viên ở cột "Phụ trách" và cập nhật khi thay đổi.
> **Mục 9 – Thiết kế lớp** liệt kê từng lớp cần viết: thuộc tính (kiểu, ý nghĩa, kiểm tra) và phương thức (tham số, ngoại lệ, việc làm).

| Thông tin | |
|---|---|
| Độ khó | ★★ · nhóm 4–5 người · 18 buổi |
| Database | `cafe_pos` |
| Project mẫu để đối chiếu | [01-Quay-ca-phe/](../01-Quay-ca-phe/README.md) |

---

## 1. Tổng quan

### 1.1 Mục tiêu
Phần mềm console cho một quán cà phê nhỏ: thu ngân chọn món, lập hóa đơn và thu tiền; tồn kho hàng đóng gói (chai nước, bánh…) tự giảm khi bán; quản lý xem doanh thu theo ngày, món bán chạy, nhân viên bán tốt. Giai đoạn 1 lưu dữ liệu bằng file, giai đoạn 2 lưu bằng SQL Server; giao diện và nghiệp vụ giữ nguyên.

### 1.2 Phạm vi
| Trong phạm vi | Ngoài phạm vi (không cần làm) |
|---|---|
| Danh mục, sản phẩm, nhân viên, hóa đơn nhiều món | Đăng nhập có mật khẩu (chỉ cần chọn "nhân viên trực ca") |
| Bán hàng, hủy hóa đơn, nhập thêm hàng | Công thức pha chế, trừ nguyên liệu theo ly |
| Tìm kiếm, phân trang, thống kê, xuất báo cáo HTML | In hóa đơn ra máy in thật, kết nối máy quẹt thẻ |
| Chuyển dữ liệu từ file sang CSDL | Giao diện Swing (chỉ là điểm cộng) |

### 1.3 Người dùng
| Vai trò | Được làm |
|---|---|
| Thu ngân (`CASHIER`) | Bán hàng, tìm sản phẩm |
| Quản lý (`MANAGER`) | Mọi chức năng của thu ngân + quản lý danh mục, sản phẩm, nhân viên, hủy hóa đơn, nhập hàng, xem thống kê, chuyển dữ liệu |

### 1.4 Thuật ngữ
| Thuật ngữ | Nghĩa |
|---|---|
| Hóa đơn | Một lần thanh toán của khách, gồm nhiều dòng chi tiết |
| Dòng chi tiết | Một món trong hóa đơn: sản phẩm, số lượng, đơn giá **tại thời điểm bán** |
| Tồn kho | Số lượng sản phẩm còn trong quán |
| Ngừng bán | Sản phẩm vẫn còn trong hệ thống (để giữ lịch sử) nhưng không được bán nữa |

---

## 2. Yêu cầu chức năng

Cột **Slide** cho biết kiến thức chính dùng để làm chức năng đó. GĐ1 = lưu file, GĐ2 = SQL Server.

| Mã | Chức năng | Người dùng | GĐ1 | GĐ2 | Slide | Phụ trách |
|---|---|---|:-:|:-:|---|---|
| FR-01 | Thêm / sửa / xóa / xem danh mục | Quản lý | ✔ | ✔ | 1, 2, 4 | |
| FR-02 | Thêm / sửa / xóa / xem sản phẩm, đổi trạng thái đang bán – ngừng bán | Quản lý | ✔ | ✔ | 1, 2, 4 | |
| FR-03 | Thêm / sửa / xóa / xem nhân viên | Quản lý | ✔ | ✔ | 1, 2 | |
| FR-04 | Chọn nhân viên trực ca khi khởi động | Tất cả | ✔ | ✔ | 2 | |
| FR-05 | **Lập hóa đơn nhiều món và thanh toán** | Thu ngân | ✔ | ✔ Transaction | 3, 4, 7 | |
| FR-06 | **Hủy hóa đơn đã thanh toán, cộng trả tồn kho** | Quản lý | ✔ | ✔ Transaction | 4, 7 | |
| FR-07 | **Nhập thêm hàng cho nhiều sản phẩm một lần** | Quản lý | ✔ | ✔ Batch | 7 | |
| FR-08 | Tìm sản phẩm nhiều tiêu chí + sắp xếp + phân trang | Tất cả | ✔ | ✔ | 3, 6 | |
| FR-09 | Tra cứu hóa đơn nhiều tiêu chí + phân trang, hiện tên nhân viên và tổng tiền | Quản lý | ✔ | ✔ JOIN | 3, 6 | |
| FR-10 | Thống kê doanh thu (mục 6) | Quản lý | ✔ | ✔ | 3, 8 | |
| FR-11 | Xuất báo cáo doanh thu ra file HTML | Quản lý | ✔ | ✔ | 5, 8 | |
| FR-12 | Lưu toàn bộ dữ liệu, mở lại chương trình dữ liệu còn nguyên | — | ✔ | ✔ | 5, 6 | |
| FR-13 | Chọn nguồn dữ liệu File / SQL Server khi khởi động | — | ✔ | ✔ | 1, 4 | |
| FR-14 | Chuyển dữ liệu từ file vào CSDL | Quản lý | | ✔ | 5, 6, 7 | |

### 2.1 Chi tiết các chức năng trọng tâm

#### FR-05 Lập hóa đơn
- **Đầu vào:** nhân viên trực ca; các cặp (mã sản phẩm, số lượng); phương thức thanh toán; phần trăm giảm giá.
- **Luồng chính:**
  1. Thu ngân nhập mã sản phẩm và số lượng. Nhập lại món đã có trong giỏ thì **cộng dồn** số lượng.
  2. Sau mỗi món, màn hình hiện giỏ hàng: tên món, số lượng, đơn giá, thành tiền, tạm tính.
  3. Thu ngân gõ lệnh kết thúc, chọn phương thức thanh toán (Tiền mặt / Thẻ / Ví điện tử), nhập giảm giá (0–50%).
  4. Màn hình hiện tổng cuối cùng, hỏi xác nhận.
  5. Xác nhận → hệ thống sinh mã `HDyyyyMMdd-nnnn`, lưu hóa đơn, trừ tồn kho từng món, báo "Đã lập hóa đơn HD…".
- **Luồng lỗi:** mã không tồn tại → báo, cho nhập lại; sản phẩm ngừng bán → BR-03; số lượng vượt tồn → BR-04 (cảnh báo ngay khi thêm vào giỏ và kiểm tra lại khi lưu); giỏ rỗng mà xác nhận → báo lỗi.
- **Nghiệm thu:** hóa đơn được tạo **khi và chỉ khi** tất cả món đủ hàng. Có một món thiếu hàng thì không món nào bị trừ kho và không có hóa đơn (TC-02).

#### FR-06 Hủy hóa đơn
- **Đầu vào:** mã hóa đơn.
- **Luồng chính:** hiện thông tin hóa đơn → hỏi xác nhận → đổi trạng thái sang "Đã hủy" → cộng lại tồn kho từng món.
- **Luồng lỗi:** mã không tồn tại → `NotFoundException`; hóa đơn đã hủy → BR-06.
- **Nghiệm thu:** đổi trạng thái và cộng kho cùng thành công hoặc cùng không xảy ra.

#### FR-07 Nhập hàng
- **Đầu vào:** danh sách (mã sản phẩm, số lượng nhập > 0), nhập lần lượt đến khi gõ dòng trống.
- **Luồng chính:** hiện bảng tổng hợp → xác nhận → cộng tồn kho tất cả sản phẩm.
- **Luồng lỗi:** có mã sai → báo mã sai, **không cộng sản phẩm nào**.

#### FR-08 Tìm sản phẩm
- **Tiêu chí (bỏ trống = không lọc):** tên chứa chuỗi (không phân biệt hoa thường), danh mục, giá từ – đến, trạng thái, "chỉ hàng sắp hết" (tồn < 10).
- **Sắp xếp:** tên A–Z, giá tăng, giá giảm, tồn kho. Danh sách cách sắp xếp là một enum, người dùng chọn theo số.
- **Phân trang:** 5–10 dòng/trang, hiện "Trang x/y", lệnh `n` (trang sau), `p` (trang trước), số trang để nhảy tới.

#### FR-09 Tra cứu hóa đơn
- **Tiêu chí:** từ ngày – đến ngày, nhân viên, trạng thái, phương thức thanh toán.
- **Kết quả mỗi dòng:** mã, thời gian, **tên nhân viên**, phương thức, trạng thái, **tổng tiền**. Sắp xếp thời gian mới nhất hoặc tổng tiền giảm dần. Có phân trang như FR-08.
- Chọn một mã để xem chi tiết các món.

#### FR-14 Chuyển dữ liệu file → CSDL
Đọc toàn bộ file `.dat` của giai đoạn 1, ghi vào CSDL theo thứ tự khóa ngoại: danh mục → sản phẩm → nhân viên → hóa đơn (kèm chi tiết). Bản ghi đã có trong CSDL (trùng mã) thì bỏ qua. Cuối cùng in số bản ghi đã chuyển / bỏ qua của từng loại.

---

## 3. Quy tắc nghiệp vụ

| Mã | Quy tắc | Khi vi phạm | Áp dụng |
|---|---|---|---|
| BR-01 | Mã danh mục, sản phẩm, nhân viên là duy nhất | `DuplicateIdException` – "Mã SP01 đã tồn tại" | FR-01..03 |
| BR-02 | Giá bán > 0; tồn kho ≥ 0; giảm giá 0–50%; số lượng mua ≥ 1; SĐT nhân viên 10 số bắt đầu bằng 0 | Báo lỗi ngay khi nhập (validate trong setter) | FR-02, 03, 05 |
| BR-03 | Không bán sản phẩm ngừng bán | `BusinessException` – "Sản phẩm đã ngừng bán" | FR-05 |
| BR-04 | Không bán vượt tồn kho | `BusinessException` – "Không đủ hàng: Pepsi còn 3" | FR-05 |
| BR-05 | Không xóa danh mục còn sản phẩm; không xóa sản phẩm đã có trong hóa đơn | `BusinessException` + gợi ý chuyển sản phẩm sang "Ngừng bán" | FR-01, 02 |
| BR-06 | Chỉ hủy được hóa đơn đang "Đã thanh toán" | `BusinessException` – "Hóa đơn đã hủy trước đó" | FR-06 |
| BR-07 | Hóa đơn lưu đơn giá tại thời điểm bán; đổi giá sản phẩm không làm đổi hóa đơn cũ | — | FR-05, 09 |
| BR-08 | Tổng tiền = Σ(số lượng × đơn giá) × (100 − giảm giá) / 100. Thống kê doanh thu chỉ tính hóa đơn "Đã thanh toán" | — | FR-05, 10 |

---

## 4. Yêu cầu dữ liệu

| Thực thể | Thông tin cần lưu | Kiểm tra khi nhập |
|---|---|---|
| Danh mục | mã (dạng `DM01`), tên | tên không rỗng, không trùng |
| Sản phẩm | mã, tên, mã danh mục, giá, tồn kho, trạng thái | BR-02; danh mục phải tồn tại |
| Nhân viên | mã, tên, SĐT, vai trò | BR-02 |
| Hóa đơn | mã, mã nhân viên, thời điểm lập, phương thức TT, % giảm, trạng thái, danh sách dòng chi tiết | có ≥ 1 dòng |
| Dòng chi tiết | mã sản phẩm, số lượng, đơn giá lúc bán | số lượng ≥ 1 |

Danh sách giá trị cố định (làm bằng **enum có nhãn tiếng Việt**): trạng thái sản phẩm (Đang bán / Ngừng bán), vai trò (Thu ngân / Quản lý), trạng thái hóa đơn (Đã thanh toán / Đã hủy), phương thức thanh toán (Tiền mặt / Thẻ / Ví điện tử).

Dữ liệu mẫu tối thiểu: 4 danh mục, 15 sản phẩm, 4 nhân viên, 30 hóa đơn rải trong 2 tuần (để thống kê có ý nghĩa). Script SQL: xem đề mục 3.4.

---

## 5. Yêu cầu phi chức năng

| Mã | Yêu cầu |
|---|---|
| NFR-01 | Chương trình **không bao giờ dừng vì lỗi**: nhập chữ vào ô số, mã sai, mất kết nối CSDL… đều báo bằng tiếng Việt rồi quay lại menu |
| NFR-02 | Chia tầng: `ui` chỉ gọi `service`; `service` chỉ biết interface repository; mọi câu SQL nằm trong `repository/jdbc` |
| NFR-03 | 100% câu SQL dùng `PreparedStatement`; cột sắp xếp lấy từ enum, không ghép chuỗi người dùng nhập |
| NFR-04 | Đổi giữa File và SQL Server chỉ bằng lựa chọn lúc khởi động, không sửa code |
| NFR-05 | Tài khoản CSDL đọc từ `db.properties`, sửa được mà không biên dịch lại |
| NFR-06 | Tiền hiển thị có dấu phân cách hàng nghìn (`35,000 đ`); ngày giờ dạng `dd/MM/yyyy HH:mm` |
| NFR-07 | Đặt tên theo [Quy-tac-dat-ten.md](../../Quy-tac-dat-ten.md); mỗi thành viên commit phần mình làm |

---

## 6. Thống kê và báo cáo

Viết bằng Stream (slide 3), chỉ tính hóa đơn "Đã thanh toán". Kết quả trả về dạng `record` (slide 8). Cần làm **ít nhất 5**.

| Mã | Thống kê | Đầu ra gợi ý |
|---|---|---|
| TK-01 | Doanh thu từng ngày trong 7 ngày gần nhất (ngày không bán hiện 0) | `record DailyRevenue(LocalDate date, long invoices, double revenue)` |
| TK-02 | Top 5 món bán chạy theo số lượng | `record ProductSales(String productId, String name, int quantity)` |
| TK-03 | Doanh thu theo nhân viên | |
| TK-04 | Tỉ lệ (%) các phương thức thanh toán | |
| TK-05 | Giờ cao điểm (giờ có nhiều hóa đơn nhất) | |
| TK-06 | Doanh thu theo danh mục | |
| TK-07 | Sản phẩm sắp hết (tồn < 10) và sản phẩm không bán được trong 7 ngày | |

FR-11: báo cáo HTML gồm tiêu đề, khoảng ngày, bảng TK-01 và TK-02, tổng doanh thu; viết bằng Text Block; mở được bằng trình duyệt, hiển thị đúng tiếng Việt.

---

## 7. Kịch bản nghiệm thu

Dùng khi tự kiểm tra, kiểm thử chéo (buổi 16) và demo bảo vệ.

| Mã | Thao tác | Kết quả mong đợi | Liên quan |
|---|---|---|---|
| TC-01 | Lập hóa đơn 3 món đủ hàng | Có hóa đơn mới, tồn kho 3 món giảm đúng số lượng | FR-05 |
| TC-02 | Lập hóa đơn 2 món, trong đó 1 món vượt tồn | Báo "Không đủ hàng…"; **không có hóa đơn**; tồn kho món còn lại **không đổi** (GĐ2: kiểm tra bằng SSMS) | FR-05, BR-04 |
| TC-03 | Bán sản phẩm đang "Ngừng bán" | Báo lỗi, không thêm vào giỏ | BR-03 |
| TC-04 | Hủy hóa đơn vừa lập, rồi hủy lần nữa | Lần 1: tồn kho cộng lại; lần 2: báo đã hủy | FR-06, BR-06 |
| TC-05 | Đổi giá sản phẩm, xem lại hóa đơn cũ | Hóa đơn cũ vẫn giữ giá cũ | BR-07 |
| TC-06 | Nhập hàng 2 dòng, 1 dòng mã sai | Báo mã sai, sản phẩm đúng **không** được cộng | FR-07 |
| TC-07 | Tra cứu hóa đơn tuần này, chuyển sang trang 2 | Hiện đúng trang, có tên nhân viên và tổng tiền | FR-09 |
| TC-08 | Xóa danh mục còn sản phẩm | Báo lỗi, gợi ý chuyển sang ngừng bán | BR-05 |
| TC-09 | Nhập chữ ở ô số lượng / giá | Báo lỗi, hỏi lại, không thoát chương trình | NFR-01 |
| TC-10 | Bán vài hóa đơn, tắt chương trình, mở lại | Dữ liệu còn nguyên | FR-12 |
| TC-11 | Xuất báo cáo HTML | File mở được bằng trình duyệt, số liệu khớp màn hình thống kê | FR-11 |
| TC-12 | Chế độ File bán vài hóa đơn → chế độ DB → chuyển dữ liệu | Hóa đơn mới xuất hiện trong DB; chạy lại lần 2 thì bỏ qua, không lỗi trùng | FR-14 |

---

## 8. Các bước thực hiện theo 8 slide

Mỗi bước ứng với một slide và 2 buổi học. Làm xong bước nào thì đánh dấu checklist của bước đó trong `docs/01-dac-ta.md`.
Cột "Xem lại" trỏ tới code minh họa trong [Demo Slide](../../Demo%20Slide/README.md) (mở `Demo Slide/demo-slide.html` để xem từng bước).

### Bước 1 – Buổi 1–2 · Slide 1: OOP
**Kiến thức:** class, constructor, đóng gói (private + getter/setter có kiểm tra), `this`, kế thừa, interface, đa hình.

**Việc cần làm**
1. Chia nhóm, chọn đề, phân công theo thực thể (mục 2, cột "Phụ trách").
2. Viết `docs/01-dac-ta.md` từ tài liệu này; vẽ class diagram 5 lớp: `Category`, `Product`, `Staff`, `Invoice`, `InvoiceDetail` (thuộc tính, phương thức theo mục 9.2).
3. Tạo project theo cấu trúc chuẩn (00-Huong-dan-chung mục 2), viết package `model`:
   - Mỗi lớp có field `private`, constructor có tham số **gọi setter** để dùng chung phần kiểm tra.
   - Setter ném `IllegalArgumentException` khi sai BR-02 (giá ≤ 0, tồn âm, SĐT sai…).
   - `Invoice` chứa `List<InvoiceDetail>` và có `subtotal()`, `total()` (BR-08).
4. Viết interface `Identifiable<ID>` (có `getId()`), cho cả 4 thực thể cài đặt.

**Bàn giao:** đặc tả, class diagram, package `model` biên dịch được, một `Main` tạo thử vài đối tượng.

**Tự kiểm tra**
- [ ] `new Product("SP01", "Pepsi", "DM03", -5000, 10, ProductStatus.SELLING)` báo lỗi giá
- [ ] Không còn field `public` nào trong `model`
- [ ] `invoice.total()` tính đúng với giảm giá 10%

**Xem lại:** Demo Slide `slide1/demo1` (setter kiểm tra), `slide1/demo2` (đa hình).

### Bước 2 – Buổi 3–4 · Slide 2: Enum, Collection, Map
**Kiến thức:** enum có thuộc tính và constructor, `values()`, enum trong `switch`; `List`, `Set`, `Map`, `Comparator`.

**Việc cần làm**
1. Viết 4 enum `ProductStatus`, `StaffRole`, `InvoiceStatus`, `PaymentMethod`, mỗi enum có `label` tiếng Việt để hiển thị.
2. Viết repository **trong bộ nhớ**: mỗi thực thể một `Map<String, T>` (khóa là mã). Viết CRUD FR-01, 02, 03.
3. Viết menu console và lớp `Input` đọc số/chuỗi an toàn (nhập sai thì hỏi lại).
4. Giỏ hàng của FR-05 lưu tạm trong `Map<String, Integer>` (mã sản phẩm → số lượng) để cộng dồn món trùng.
5. FR-04: chọn nhân viên trực ca từ danh sách.

**Bàn giao:** CRUD 3 thực thể chạy trên bộ nhớ, giỏ hàng cộng dồn đúng.

**Tự kiểm tra**
- [ ] Thêm sản phẩm trùng mã báo lỗi (dùng `containsKey`)
- [ ] Menu chọn phương thức thanh toán in từ `PaymentMethod.values()`, không viết cứng
- [ ] Thêm "SP01 × 2" rồi "SP01 × 3" → giỏ có một dòng SP01 × 5

**Xem lại:** Demo Slide `slide2/demo2` (enum trạng thái), `slide2/demo3` (List + Set), `slide2/demo4` (Map).

### Bước 3 – Buổi 5–6 · Slide 3: Lambda & Stream API
**Kiến thức:** lambda, method reference, `filter`, `map`, `sorted`, `skip/limit`, `collect`, `groupingBy`, `flatMap`, `summingDouble`, `counting`.

**Việc cần làm**
1. FR-08 trên bộ nhớ: lọc nhiều tiêu chí bằng chuỗi `filter`, sắp xếp bằng `Comparator` lấy từ enum sắp xếp, phân trang bằng `skip/limit`.
2. Viết `record Page<T>` (items, page, size, totalItems, `totalPages()`), dùng chung cho mọi danh sách có phân trang.
3. Viết `ReportService` với ít nhất 5 thống kê mục 6 (TK-01, 02 bắt buộc vì dùng cho báo cáo).
4. Tạo dữ liệu mẫu trong code (khoảng 30 hóa đơn) để thống kê có số liệu.

**Bàn giao:** tìm kiếm + phân trang sản phẩm, ≥ 5 thống kê chạy trên dữ liệu bộ nhớ.

**Tự kiểm tra**
- [ ] Tìm "trà", giá 20.000–40.000, sắp giá giảm → đúng kết quả
- [ ] TK-02 dùng `flatMap` qua chi tiết hóa đơn
- [ ] Không còn vòng `for` lồng nhau trong `ReportService`

**Xem lại:** Demo Slide `slide3/vidu/FilterExample` (bấm "Chạy từng bước"), `slide3/demo` (`groupingBy`, `average`).

### Bước 4 – Buổi 7–8 · Slide 4: Exception & Generics
**Kiến thức:** checked / unchecked exception, `throw` / `throws`, custom exception, `try-catch-finally`; generic class và interface, `Optional`.

**Việc cần làm**
1. Viết 4 exception: `DuplicateIdException`, `NotFoundException`, `BusinessException` (checked), `DataAccessException` (unchecked).
2. Viết `GenericRepository<T, ID>` (findAll, findById trả `Optional`, existsById, insert, update, deleteById); các repository ở bước 2 cài interface này.
3. Viết nghiệp vụ FR-05, 06, 07 bản bộ nhớ trong service, ném đúng exception của BR-01..06.
   - FR-05 và FR-07: **kiểm tra tất cả trước, hợp lệ hết mới thay đổi dữ liệu** (chuẩn bị cho transaction ở bước 7).
4. Menu bắt exception, in thông báo tiếng Việt; vòng lặp chính có `catch` cuối cùng để không bao giờ crash (NFR-01).

**Bàn giao:** lập / hủy hóa đơn, nhập hàng chạy trên bộ nhớ; nhập sai không crash.

**Tự kiểm tra**
- [ ] TC-02, TC-03, TC-04, TC-06, TC-08, TC-09 đạt trên bộ nhớ
- [ ] Không có `catch (Exception e) {}` rỗng
- [ ] `service` không biết lớp cài đặt repository cụ thể, chỉ biết interface

**Xem lại:** Demo Slide `slide4/demo5` (GenericManager + custom exception), `slide4/demo1` (luồng try-catch).

### Bước 5 – Buổi 9–10 · Slide 5: I/O Streams → **Mốc 1**
**Kiến thức:** `Serializable`, `ObjectOutputStream` / `ObjectInputStream`, try-with-resources, `BufferedWriter`, xử lý `IOException`.

**Việc cần làm**
1. Cho các lớp `model` cài `Serializable`.
2. Viết `FileRepository<T, ID>` lưu `Map<ID, T>` vào `data/<thực-thể>.dat` (khung ở 00-Huong-dan-chung mục 3.3). Mỗi lần thêm / sửa / xóa thì ghi lại file.
3. Xử lý file lỗi: chưa có file hoặc file rỗng → bắt đầu rỗng; file hỏng → đổi tên thành `.bak`, cảnh báo, bắt đầu rỗng.
4. FR-11 bản đầu: ghi báo cáo HTML bằng `BufferedWriter` (UTF-8).

**Bàn giao (Mốc 1 – 20% điểm):** demo giai đoạn 1 theo điều kiện ở 00-Huong-dan-chung mục 5.

**Tự kiểm tra**
- [ ] TC-10: tắt mở lại, dữ liệu còn nguyên
- [ ] Xóa thư mục `data/` → chương trình vẫn chạy
- [ ] Ghi vài ký tự rác vào một file `.dat` → chương trình cảnh báo, không crash

**Xem lại:** Demo Slide `slide5/demo4` (đọc/ghi danh sách object, lớp service riêng), `slide5/demo2` (xử lý lỗi file).

### Bước 6 – Buổi 11–12 · Slide 6: JDBC
**Kiến thức:** `DriverManager`, `Connection`, `PreparedStatement`, `ResultSet`, ánh xạ dòng → đối tượng, try-with-resources.

**Việc cần làm**
1. Vẽ ERD; viết `sql/01_schema.sql` (theo đề mục 3.4) và `sql/02_sample_data.sql` (số lượng ở mục 4).
2. Viết `DBConnect` đọc `db.properties`.
3. Viết các lớp `Jdbc…Repository` cài **cùng interface** với bản file: phần đọc (findAll, findById) cho 4 thực thể.
4. FR-09 bằng JOIN `invoice` – `staff` – `invoice_detail` (câu SQL mẫu ở đề mục 6), phân trang `OFFSET … FETCH`, đếm tổng bằng `COUNT(*)` cùng điều kiện.
5. FR-13: `Main` hỏi File / SQL Server rồi tạo đúng bộ repository.

**Bàn giao:** chế độ SQL Server xem được danh sách sản phẩm, tra cứu hóa đơn có tên nhân viên.

**Tự kiểm tra**
- [ ] Chạy 2 script trên máy sạch không lỗi
- [ ] Không có câu SQL nào ghép chuỗi giá trị người dùng nhập
- [ ] Đổi `db.properties` sai mật khẩu → báo lỗi kết nối, không crash

**Xem lại:** Demo Slide `slide6/demo` (DBConnect, Repository; tab "Chạy từng bước" có ResultSet), `slide6/vidu/TungBuocJdbc`.

### Bước 7 – Buổi 13–14 · Slide 7: JDBC nâng cao
**Kiến thức:** `executeUpdate` và số dòng trả về, transaction (`setAutoCommit(false)`, `commit`, `rollback`), batch (`addBatch`, `executeBatch`), `CallableStatement`.

**Việc cần làm**
1. Phần ghi của mọi repository JDBC (insert, update, delete).
2. FR-05 transaction: `INSERT invoice` → batch `INSERT invoice_detail` → từng món `UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ? AND status = 'SELLING'`; một câu trả về 0 dòng thì rollback (đề mục 5.1).
3. FR-06 transaction: đổi trạng thái có điều kiện `AND status = 'PAID'`, cộng trả kho (đề mục 5.2).
4. FR-07 batch: `UPDATE product SET stock = stock + ?`; phần tử nào của mảng kết quả bằng 0 thì rollback.
5. FR-08 trên DB: lọc nhiều tiêu chí, sắp xếp theo enum, phân trang.
6. Điểm cộng: stored procedure `sp_revenue_by_day` gọi bằng `CallableStatement`.

**Bàn giao:** mọi nghiệp vụ chạy trên SQL Server, demo được rollback thật.

**Tự kiểm tra**
- [ ] TC-01, TC-02, TC-04, TC-06 đạt ở chế độ SQL Server, kiểm tra lại bằng SSMS
- [ ] Mọi lệnh của một transaction dùng **cùng một** `Connection`
- [ ] Sau rollback, `autoCommit` được trả lại hoặc connection được đóng

**Xem lại:** Demo Slide `slide7/demo2` (CRUD + executeUpdate), `slide7/vidu/TransactionDemo`, `slide7/vidu/BatchDemo`.

### Bước 8 – Buổi 15 · Slide 8: Record, Annotation, Text Block
**Kiến thức:** `record` (DTO bất biến), compact constructor, annotation tự tạo + Reflection, Text Block + `formatted()`, autoboxing (cẩn thận `null` và so sánh `==`).

**Việc cần làm**
1. Đổi kết quả thống kê sang `record` (`DailyRevenue`, `ProductSales`…).
2. Viết câu SQL dài (FR-09) bằng Text Block.
3. FR-11 hoàn chỉnh: khung HTML bằng Text Block, chèn số liệu bằng `formatted()`.
4. Tạo annotation `@Author("tên")` (RUNTIME) gắn lên các service; một mục menu dùng Reflection in "service nào do ai viết".
5. Rà soát chỗ so sánh `Integer`/`Double` bằng `==`, đổi sang `equals` hoặc kiểu nguyên thủy.

**Bàn giao:** báo cáo HTML, record DTO, annotation.

**Tự kiểm tra**
- [ ] TC-11 đạt
- [ ] Không còn SQL nhiều dòng nối bằng `+`

**Xem lại:** Demo Slide `slide8/vidu/RecordDemo`, `AnnotationDemo`, `TextBlockDemo`, `AutoboxingDemo` (tab "Thử Integer cache").

### Bước 9 – Buổi 16–18: Tích hợp, kiểm thử chéo, bảo vệ
1. FR-14 chuyển dữ liệu file → DB (TC-12).
2. Kiểm thử chéo với nhóm khác bằng toàn bộ mục 7; ghi lỗi tìm được và cách sửa vào biên bản.
3. Hoàn thiện `README.md`: cách chạy, tài khoản DB, bảng phân công, ảnh chụp chức năng.
4. Chạy checklist ở 00-Huong-dan-chung mục 7. Mỗi thành viên tự chạy và giải thích được phần mình làm, chuẩn bị các câu ở đề mục 12.

<!-- BEGIN thiet-ke-lop (sinh tự động từ project mẫu) -->

---

## 9. Thiết kế lớp

Mục này liệt kê **các lớp cần viết, mỗi lớp có thuộc tính nào, phương thức nào**. Thiết kế lấy đúng theo project mẫu [01-Quay-ca-phe/](../01-Quay-ca-phe/README.md), nên nhóm có thể đối chiếu code khi bí. Nhóm được đổi tên, gộp hoặc tách lớp, nhưng phải giữ đủ trách nhiệm và giữ cách chia tầng dưới đây.

Quy ước chung cho mọi lớp:
- Thuộc tính luôn `private`. Lớp thực thể có getter cho mọi thuộc tính; setter kiểm tra dữ liệu, sai thì ném `IllegalArgumentException` với thông báo tiếng Việt (cột "Ràng buộc").
- Constructor có tham số **gọi lại setter** để không viết kiểm tra hai lần.
- Lớp thực thể cài `Identifiable<ID>` (có `getId()`) và `Serializable` (để ghi file bằng Object Stream ở Bước 5).
- Enum có `label` tiếng Việt, `getLabel()` và `toString()` trả về `label` để in menu.
- Tầng trên chỉ gọi tầng dưới qua **interface**: `ui` → `service` → `repository` (interface) ← `repository.file` / `repository.jdbc`. `service` và `ui` không chứa SQL, không đọc ghi file.
- Cột "Bước" trong tài liệu này: thực thể và enum viết ở Bước 1–2; tìm kiếm, thống kê ở Bước 3; exception, repository ở Bước 4; bản file ở Bước 5; bản JDBC ở Bước 6–7; record, `@Author` ở Bước 8.

### 9.1 Sơ đồ gói và lớp

```
app/
  Main
  model/            Category, Criteria, Identifiable, Invoice,
                    InvoiceDetail, InvoiceStatus, Page, PaymentMethod,
                    Product, ProductStatus, Staff, StaffRole
  exception/        BusinessException, DataAccessException,
                    DuplicateIdException, NotFoundException
  repository/       GenericRepository, Repositories
  repository/file/  FileRepositories, FileRepository
  repository/jdbc/  DBConnect, JdbcRepositories, JdbcRepository, SqlFilter
  service/          CatalogService, MigrationService, ReportService,
                    SalesService, SampleData, StaffService
  ui/               ConsoleUI, Input
  annotation/       Author
```

Quan hệ giữa các thực thể:
- `Invoice` → `Staff` qua `staffId` (N–1, lưu mã như khóa ngoại)
- `Invoice` ◆ `InvoiceDetail` qua `details` (1–N, dòng con không tồn tại độc lập, lưu cùng `Invoice`)
- `InvoiceDetail` → `Product` qua `productId` (N–1, lưu mã như khóa ngoại)
- `Product` → `Category` qua `categoryId` (N–1, lưu mã như khóa ngoại)

### 9.2 Lớp thực thể (package `model`) – Bước 1, 2

#### `Category` – Danh mục
Cài đặt: `Identifiable<String>`, `Serializable`.

| Thuộc tính | Kiểu | Ý nghĩa | Kiểm tra (thông báo lỗi khi sai) / ghi chú |
|---|---|---|---|
| `id` | `String` | Mã danh mục | Mã danh mục phải dạng DM01 |
| `name` | `String` | Tên | Tên danh mục không được rỗng |

| Phương thức | Mô tả |
|---|---|
| `Category(String id, String name)` | Constructor đầy đủ tham số; gọi setter nên dùng chung phần kiểm tra |
| `getId() : String` | Trả mã (cài đặt `Identifiable`) |
| `get…() / is…()` | Getter cho mọi thuộc tính |
| `set…(…)` | Setter có kiểm tra (sai → `IllegalArgumentException`): `setId`, `setName` |
| `toString() : String` | Chuỗi một dòng để in danh sách |

#### `Invoice` – Hóa đơn
Hóa đơn – thực thể gốc chứa List chi tiết (chi tiết không tồn tại độc lập). Cài đặt: `Identifiable<String>`, `Serializable`.

| Thuộc tính | Kiểu | Ý nghĩa | Kiểm tra (thông báo lỗi khi sai) / ghi chú |
|---|---|---|---|
| `id` | `String` | Mã hóa đơn | Mã hóa đơn rỗng |
| `staffId` | `String` | Mã nhân viên lập (tham chiếu Staff) | Chưa chọn nhân viên |
| `createdAt` | `LocalDateTime` | Thời điểm tạo | Thiếu thời gian lập |
| `paymentMethod` | `PaymentMethod` | Phương thức thanh toán | Chưa chọn phương thức thanh toán |
| `discountPercent` | `int` | Phần trăm giảm giá | Giảm giá phải 0–50% |
| `status` | `InvoiceStatus` | Trạng thái | Thiếu trạng thái |
| `details` | `List<InvoiceDetail>` | Danh sách dòng chi tiết (quan hệ 1–N, lưu cùng đối tượng cha) | Khởi tạo rỗng, thêm qua phương thức của lớp |

| Phương thức | Mô tả |
|---|---|
| `Invoice(String id, String staffId, LocalDateTime createdAt, PaymentMethod paymentMethod, int discountPercent, InvoiceStatus status)` | Constructor đầy đủ tham số; gọi setter nên dùng chung phần kiểm tra |
| `getId() : String` | Trả mã (cài đặt `Identifiable`) |
| `get…() / is…()` | Getter cho mọi thuộc tính |
| `set…(…)` | Setter có kiểm tra (sai → `IllegalArgumentException`): `setId`, `setStaffId`, `setCreatedAt`, `setPaymentMethod`, `setDiscountPercent`, `setStatus` |
| `addItem(String productId, int quantity, double unitPrice)` | Thêm món; trùng món thì cộng dồn số lượng. |
| `subtotal() : double` | Tạm tính = Σ số lượng × đơn giá (trước giảm giá) |
| `total() : double` | Tổng phải trả = tạm tính × (100 − giảm giá) / 100 |

#### `InvoiceDetail` – Dòng hóa đơn
Một dòng hóa đơn. unitPrice là giá TẠI THỜI ĐIỂM BÁN – đổi giá sản phẩm sau này không ảnh hưởng. Cài đặt: `Serializable`.

| Thuộc tính | Kiểu | Ý nghĩa | Kiểm tra (thông báo lỗi khi sai) / ghi chú |
|---|---|---|---|
| `productId` | `String` | Mã sản phẩm | `final`: gán một lần trong constructor (kiểm tra ở constructor) |
| `quantity` | `int` | Số lượng | Số lượng phải >= 1 |
| `unitPrice` | `double` | Đơn giá tại thời điểm lập | `final`: gán một lần trong constructor (kiểm tra ở constructor) |

| Phương thức | Mô tả |
|---|---|
| `InvoiceDetail(String productId, int quantity, double unitPrice)` | Constructor; kiểm tra: Thiếu mã sản phẩm; Đơn giá phải > 0 |
| `get…() / is…()` | Getter cho mọi thuộc tính |
| `set…(…)` | Setter có kiểm tra (sai → `IllegalArgumentException`): `setQuantity` |
| `amount() : double` | Thành tiền = số lượng × đơn giá |

#### `Product` – Sản phẩm
Sản phẩm. Tham chiếu danh mục bằng mã (categoryId) – giống khóa ngoại. Cài đặt: `Identifiable<String>`, `Serializable`.

| Thuộc tính | Kiểu | Ý nghĩa | Kiểm tra (thông báo lỗi khi sai) / ghi chú |
|---|---|---|---|
| `id` | `String` | Mã sản phẩm | Mã sản phẩm phải dạng SP01 |
| `name` | `String` | Tên | Tên sản phẩm không được rỗng |
| `categoryId` | `String` | Mã danh mục (tham chiếu Category) | Chưa chọn danh mục |
| `price` | `double` | Giá bán | Giá phải > 0 |
| `stock` | `int` | Số lượng tồn kho | Tồn kho phải >= 0 |
| `status` | `ProductStatus` | Trạng thái | Chưa chọn trạng thái |

| Phương thức | Mô tả |
|---|---|
| `Product(String id, String name, String categoryId, double price, int stock, ProductStatus status)` | Constructor đầy đủ tham số; gọi setter nên dùng chung phần kiểm tra |
| `getId() : String` | Trả mã (cài đặt `Identifiable`) |
| `get…() / is…()` | Getter cho mọi thuộc tính |
| `set…(…)` | Setter có kiểm tra (sai → `IllegalArgumentException`): `setId`, `setName`, `setCategoryId`, `setPrice`, `setStock`, `setStatus` |
| `toString() : String` | Chuỗi một dòng để in danh sách |

#### `Staff` – Nhân viên
Cài đặt: `Identifiable<String>`, `Serializable`.

| Thuộc tính | Kiểu | Ý nghĩa | Kiểm tra (thông báo lỗi khi sai) / ghi chú |
|---|---|---|---|
| `id` | `String` | Mã nhân viên | Mã nhân viên phải dạng NV01 |
| `name` | `String` | Tên | Tên nhân viên không được rỗng |
| `phone` | `String` | Số điện thoại | Số điện thoại phải gồm 10 số, bắt đầu bằng 0 |
| `role` | `StaffRole` | Vai trò | Chưa chọn vai trò |

| Phương thức | Mô tả |
|---|---|
| `Staff(String id, String name, String phone, StaffRole role)` | Constructor đầy đủ tham số; gọi setter nên dùng chung phần kiểm tra |
| `getId() : String` | Trả mã (cài đặt `Identifiable`) |
| `get…() / is…()` | Getter cho mọi thuộc tính |
| `set…(…)` | Setter có kiểm tra (sai → `IllegalArgumentException`): `setId`, `setName`, `setPhone`, `setRole` |
| `toString() : String` | Chuỗi một dòng để in danh sách |

### 9.3 Enum (package `model`) – Bước 2

| Enum | Ý nghĩa | Hằng (nhãn, tham số) | Thuộc tính / phương thức thêm |
|---|---|---|---|
| `InvoiceStatus` | Trạng thái hóa đơn | `PAID` ("Đã thanh toán"); `CANCELLED` ("Đã hủy") | — |
| `PaymentMethod` | Phương thức thanh toán | `CASH` ("Tiền mặt"); `CARD` ("Thẻ"); `E_WALLET` ("Ví điện tử") | — |
| `ProductStatus` | Trạng thái sản phẩm | `SELLING` ("Đang bán"); `STOPPED` ("Ngừng bán") | — |
| `StaffRole` | Vai trò nhân viên | `CASHIER` ("Thu ngân"); `MANAGER` ("Quản lý") | — |

### 9.4 Record tiêu chí tìm kiếm, kết quả và kiểu sắp xếp – Bước 3, 8

Project mẫu gom các record và enum sắp xếp vào lớp `Criteria` (lớp chỉ để chứa, constructor `private`). Record là bất biến, có sẵn constructor, getter dạng `ten()`, `equals`, `toString`.

| Kiểu | Nằm trong | Thành phần | Mô tả |
|---|---|---|---|
| `ProductCriteria` | Criteria | `String keyword`, `String categoryId`, `Double minPrice`, `Double maxPrice`, `ProductStatus status`, `boolean lowStockOnly`, `ProductSort sort` | Mọi tiêu chí có thể null = bỏ qua. |
| `InvoiceCriteria` | Criteria | `LocalDate from`, `LocalDate to`, `String staffId`, `InvoiceStatus status`, `PaymentMethod method`, `InvoiceSort sort` | Tiêu chí tìm kiếm; thành phần nào `null` (hoặc `false`) thì bỏ qua tiêu chí đó |
| `InvoiceSummary` | Criteria | `String id`, `LocalDateTime createdAt`, `String staffName`, `PaymentMethod method`, `InvoiceStatus status`, `double total` | Một dòng danh sách hóa đơn (đã JOIN tên nhân viên, tính tổng). |
| `DailyRevenue` | Criteria | `LocalDate date`, `long invoices`, `double revenue` | DTO kết quả thống kê / báo cáo |
| `ProductSales` | Criteria | `String productId`, `String name`, `int quantity`, `double revenue` | DTO kết quả thống kê / báo cáo |
| `Page` | model | `List<T> items`, `int page`, `int size`, `long totalItems` | Một trang kết quả. page đếm từ 1.<br>`totalPages() : int` – Tổng số trang = ⌈totalItems / size⌉<br>`of(List<T> all, int page, int size) : Page<T>` – Cắt một danh sách đã lọc + sắp xếp thành trang (dùng cho bản file). |
| `ProductSort` | Criteria | `NAME` ("Tên A→Z"); `PRICE_ASC` ("Giá tăng dần"); `PRICE_DESC` ("Giá giảm dần"); `STOCK_ASC` ("Tồn kho ít nhất") | Kiểu sắp xếp: mỗi hằng có `label` (hiện menu), `sql` (đoạn ORDER BY cho bản JDBC), `comparator` (cho bản file) |
| `InvoiceSort` | Criteria | `NEWEST` ("Mới nhất"); `TOTAL_DESC` ("Tổng tiền giảm dần") | Kiểu sắp xếp: mỗi hằng có `label` (hiện menu), `sql` (đoạn ORDER BY cho bản JDBC), `comparator` (cho bản file) |

### 9.5 Ngoại lệ (package `exception`) – Bước 4

| Lớp | Kế thừa | Dùng khi | Phương thức thêm |
|---|---|---|---|
| `BusinessException` | `Exception` | Vi phạm quy tắc nghiệp vụ (hết hàng, trùng lịch, vượt sĩ số…). | Constructor `BusinessException(String message)` |
| `DataAccessException` | `RuntimeException` | Unchecked – bọc IOException / SQLException để tầng service, ui không phụ thuộc java.io / java.sql. | Constructor `DataAccessException(String message, Throwable cause)` |
| `DuplicateIdException` | `Exception` | Trùng mã khi thêm mới. | Constructor `DuplicateIdException(String what, Object id)` |
| `NotFoundException` | `Exception` | Không tìm thấy theo mã. | Constructor `NotFoundException(String what, Object id)` |

Exception checked (`extends Exception`) bắt buộc service khai báo `throws`, `ConsoleUI` bắt và in thông báo. `DataAccessException` là unchecked, bọc `IOException` / `SQLException` để tầng trên không phụ thuộc `java.io` / `java.sql`.

### 9.6 Repository (package `repository`) – Bước 4, 5, 6, 7

**Interface chung** `Identifiable<ID>`: `getId() : ID`.

**Interface chung** `GenericRepository<T, ID>` – CRUD dùng cho mọi thực thể:

| Phương thức | Mô tả |
|---|---|
| `findAll() : List<T>` | Lấy tất cả bản ghi |
| `findById(ID id) : Optional<T>` | Tìm theo mã; trả `Optional` rỗng nếu không có |
| `existsById(ID id) : boolean` | Mã đã tồn tại chưa |
| `insert(T entity)` | Thêm mới |
| `update(T entity) : boolean` | Cập nhật; trả `false` nếu mã không tồn tại |
| `deleteById(ID id) : boolean` | Xóa theo mã; trả `false` nếu mã không tồn tại |

**Interface riêng từng thực thể** (project mẫu gom trong lớp `Repositories`), kế thừa `GenericRepository` và thêm phương thức đặc thù:

| Interface | Phương thức thêm | Ngoại lệ | Mô tả |
|---|---|---|---|
| `CategoryRepository`<br>`GenericRepository<Category, String>` | — | | Chỉ dùng CRUD chung |
| `StaffRepository`<br>`GenericRepository<Staff, String>` | — | | Chỉ dùng CRUD chung |
| `ProductRepository`<br>`GenericRepository<Product, String>` | `search(ProductCriteria criteria, int page, int size) : Page<Product>` |  | Tìm sản phẩm theo nhiều tiêu chí + sắp xếp + phân trang (trang đếm từ 1) |
|  | `findByCategory(String categoryId) : List<Product>` |  | Sản phẩm thuộc một danh mục |
|  | `addStock(Map<String, Integer> quantities)` | `NotFoundException` | Nhập hàng nhiều sản phẩm: được hết hoặc không gì cả. Mã sai → NotFoundException. |
| `InvoiceRepository`<br>`GenericRepository<Invoice, String>` | `create(Invoice invoice)` | `BusinessException` | Lưu hóa đơn + trừ tồn kho – được hết hoặc không gì cả. |
|  | `cancel(String id)` | `BusinessException`, `NotFoundException` | Đổi trạng thái sang CANCELLED + cộng trả tồn kho. |
|  | `search(InvoiceCriteria criteria, int page, int size) : Page<InvoiceSummary>` |  | Tìm hóa đơn theo nhiều tiêu chí + sắp xếp + phân trang (trang đếm từ 1) |
|  | `existsByProduct(String productId) : boolean` |  | Có hóa đơn nào tham chiếu tới sản phẩm này không (kiểm tra trước khi xóa) |
|  | `existsByStaff(String staffId) : boolean` |  | Có hóa đơn nào tham chiếu tới nhân viên này không (kiểm tra trước khi xóa) |
|  | `nextId(LocalDate date) : String` |  | Mã kế tiếp dạng HD20261025-0001. |

**Hai bản cài đặt** – service chỉ biết interface nên đổi File ↔ SQL Server không phải sửa service:

| Lớp | Bước | Vai trò |
|---|---|---|
| `FileRepository<T, ID>` | 5 | Cài đặt Giai đoạn 1 dùng chung: mỗi thực thể 1 file .dat, trong bộ nhớ là Map<ID, T>. File không tồn tại / rỗng → bắt đầu rỗng. File hỏng → đổi tên thành .bak, bắt đầu rỗng, ghi cảnh báo. Thuộc tính: `file`, `Map<ID, T> data`. Phương thức: CRUD của `GenericRepository` + `save()` ghi cả Map xuống file. |
| `FileCategoryRepository`, `FileStaffRepository`, `FileProductRepository`, `FileInvoiceRepository` | 5 | Lớp con của `FileRepository`, cài các phương thức riêng bằng Stream (lọc, sắp xếp, `Page.of`). |
| `DBConnect` | 6 | Kết nối SQL Server. Cấu hình đọc từ file db.properties ở thư mục gốc project (url, user, password); có thể ghi đè bằng -Ddb.url=.. -Ddb.user=.. -Ddb.password=... Phương thức: `getConnection() : Connection`. |
| `JdbcRepository<T, ID>` | 6 | Phần JDBC dùng chung cho bảng có khóa chính 1 cột: findAll, findById, existsById, deleteById. Lớp con chỉ viết mapRow, insert, update và các truy vấn riêng. Phương thức trừu tượng `mapRow(ResultSet) : T`; tiện ích `query`, `count`, `execute`, `bind`. |
| `SqlFilter` | 6 | Ghép điều kiện WHERE động cho tìm kiếm nhiều tiêu chí – giá trị luôn đi qua dấu ?, không nối chuỗi. Ví dụ: f.add("p.price >= ?", minPrice).add("p.name LIKE ?", "%" + kw + "%"); |
| `JdbcCategoryRepository`, `JdbcStaffRepository`, `JdbcProductRepository`, `JdbcInvoiceRepository` | 6, 7 | Lớp con của `JdbcRepository`, mỗi lớp một bảng; mọi câu SQL dùng `PreparedStatement`. |

Các phương thức JDBC quan trọng (Bước 6–7) trong project mẫu:
- `JdbcProductRepository.addStock` – Batch trong transaction: 1 mã sai (0 dòng) → rollback cả lô.
- `JdbcInvoiceRepository.create` – TRANSACTION: INSERT invoice → INSERT chi tiết (batch) → trừ kho có điều kiện → commit.
- `JdbcInvoiceRepository.cancel` – TRANSACTION: đổi trạng thái có điều kiện + cộng trả tồn kho từng món.
- `JdbcInvoiceRepository.search` – JOIN staff + invoice_detail, lọc nhiều tiêu chí, sắp xếp theo enum, phân trang OFFSET/FETCH.
- `JdbcInvoiceRepository.decreaseStock` – Cập nhật có điều kiện: 0 dòng = không đủ hàng / ngừng bán. Chống bán quá tồn khi 2 quầy cùng bán.

### 9.7 Service (package `service`) – Bước 3, 4 trở đi

Service chứa nghiệp vụ: kiểm tra quy tắc (BR), gọi repository, ném exception. Mỗi service gắn `@Author` ghi người phụ trách (Bước 8).

#### `CatalogService` – Danh mục, sản phẩm, nhập hàng
Danh mục + sản phẩm + nhập hàng. Chỉ biết interface repository, không biết file hay JDBC. Nhận qua constructor: `CategoryRepository`, `ProductRepository`, `InvoiceRepository`.

| Phương thức | Ngoại lệ | Mô tả |
|---|---|---|
| `allCategories() : List<Category>` |  | Danh sách tất cả danh mục |
| `addCategory(Category c)` | `DuplicateIdException`, `BusinessException` | Thêm danh mục mới (kiểm tra hợp lệ); trùng mã → `DuplicateIdException` |
| `updateCategory(Category c)` | `NotFoundException`, `BusinessException` | Sửa thông tin danh mục; không có → `NotFoundException`; vi phạm quy tắc → `BusinessException` |
| `deleteCategory(String id)` | `NotFoundException`, `BusinessException` | Xóa danh mục; đã có dữ liệu liên quan → `BusinessException` |
| `getProduct(String id) : Product` | `NotFoundException` | Tìm sản phẩm theo mã; không có → `NotFoundException` |
| `addProduct(Product p)` | `DuplicateIdException`, `NotFoundException` | Thêm sản phẩm mới (kiểm tra hợp lệ); trùng mã → `DuplicateIdException`; mã tham chiếu không tồn tại → `NotFoundException` |
| `updateProduct(Product p)` | `NotFoundException` | Sửa thông tin sản phẩm; không có → `NotFoundException` |
| `deleteProduct(String id)` | `NotFoundException`, `BusinessException` | Xóa sản phẩm; đã có dữ liệu liên quan → `BusinessException` |
| `search(ProductCriteria criteria, int page, int size) : Page<Product>` |  | Tìm bản ghi theo nhiều tiêu chí + sắp xếp + phân trang (trang đếm từ 1) |
| `addStock(Map<String, Integer> quantities)` | `NotFoundException`, `BusinessException` | Nhập thêm hàng cho nhiều sản phẩm một lần (mã → số lượng); có mã sai thì không cộng sản phẩm nào |

#### `MigrationService` – Chuyển dữ liệu file → CSDL
Chuyển dữ liệu Giai đoạn 1 (file) sang Giai đoạn 2 (SQL Server). Chuyển theo thứ tự khóa ngoại (bảng cha trước), mã đã có trong DB thì bỏ qua.

| Phương thức | Ngoại lệ | Mô tả |
|---|---|---|
| `copy(String name, GenericRepository<T, ID> from, GenericRepository<T, ID> to) : int` |  | Gọi lần lượt cho từng cặp repository: file → jdbc. Trả về số dòng đã chuyển. |
| `report() : List<String>` |  | Các dòng tổng kết sau khi chuyển: mỗi bảng chuyển bao nhiêu dòng, lỗi gì |

#### `ReportService` – Thống kê, báo cáo
7 thống kê bằng Stream (chỉ tính hóa đơn PAID) + xuất báo cáo HTML bằng Text Block. Nhận qua constructor: `InvoiceRepository`, `ProductRepository`, `CategoryRepository`, `StaffRepository`.

| Phương thức | Ngoại lệ | Mô tả |
|---|---|---|
| `revenueLastDays(int n) : List<DailyRevenue>` |  | 1. Doanh thu theo ngày trong n ngày gần nhất (kể cả ngày không bán = 0). |
| `topProducts(int n) : List<ProductSales>` |  | 2. Top n món bán chạy theo số lượng – flatMap chi tiết của mọi hóa đơn. |
| `revenueByStaff() : Map<String, Double>` |  | 3. Doanh thu theo nhân viên (tên). |
| `countByPaymentMethod() : Map<PaymentMethod, Long>` |  | 4. Số hóa đơn theo phương thức thanh toán. |
| `peakHour() : Optional<Map.Entry<Integer, Long>>` |  | 5. Giờ cao điểm: giờ có nhiều hóa đơn nhất. |
| `revenueByCategory() : Map<String, Double>` |  | 6. Doanh thu (trước giảm giá) theo danh mục: flatMap chi tiết → productId → categoryId. |
| `lowStock(int threshold) : List<Product>` |  | 7a. Sản phẩm sắp hết hàng. |
| `notSoldInDays(int n) : List<Product>` |  | 7b. Sản phẩm không bán được trong n ngày gần nhất. |
| `exportHtml(String path) : Path` | `IOException` | Xuất báo cáo HTML – khung trang viết bằng Text Block, dữ liệu chèn bằng formatted(). |

#### `SalesService` – Bán hàng
Lập hóa đơn, hủy hóa đơn, tra cứu. Nhận qua constructor: `InvoiceRepository`, `ProductRepository`, `StaffRepository`.

| Phương thức | Ngoại lệ | Mô tả |
|---|---|---|
| `checkout(String staffId, Map<String, Integer> cart, PaymentMethod method, int discount) : Invoice` | `BusinessException`, `NotFoundException` | Lập hóa đơn từ giỏ hàng (mã SP → số lượng). Đơn giá lấy từ giá hiện tại của sản phẩm. Tồn kho / trạng thái KHÔNG kiểm tra ở đây mà trong transaction của repository – vì giữa lúc thêm vào giỏ và lúc thanh toán, quầy khác có thể đã bán mất hàng. |
| `getProduct(String productId) : Product` | `NotFoundException` | Tìm sản phẩm theo mã; không có → `NotFoundException` |
| `warning(Product p, int quantityInCart) : String` |  | Cảnh báo khi thêm vào giỏ (không chặn) – null nếu không có gì đáng lo. |
| `cancel(String invoiceId)` | `BusinessException`, `NotFoundException` | Hủy hóa đơn |
| `get(String id) : Invoice` | `NotFoundException` | Tìm hóa đơn theo mã; không có → `NotFoundException` |
| `search(InvoiceCriteria criteria, int page, int size) : Page<InvoiceSummary>` |  | Tìm hóa đơn theo nhiều tiêu chí + sắp xếp + phân trang (trang đếm từ 1) |

#### `StaffService` – Nhân viên
Nhận qua constructor: `StaffRepository`, `InvoiceRepository`.

| Phương thức | Ngoại lệ | Mô tả |
|---|---|---|
| `all() : List<Staff>` |  | Danh sách tất cả nhân viên |
| `get(String id) : Staff` | `NotFoundException` | Tìm nhân viên theo mã; không có → `NotFoundException` |
| `add(Staff s)` | `DuplicateIdException` | Thêm nhân viên mới (kiểm tra hợp lệ); trùng mã → `DuplicateIdException` |
| `update(Staff s)` | `NotFoundException` | Sửa thông tin nhân viên; không có → `NotFoundException` |
| `delete(String id)` | `NotFoundException`, `BusinessException` | Xóa nhân viên; đã có dữ liệu liên quan → `BusinessException` |

`SampleData` – Dữ liệu mẫu cho lần chạy đầu ở chế độ File (chế độ DB dùng sql/02_sample_data.sql). Phương thức `seed(…)` (static) chỉ chạy khi file dữ liệu còn rỗng.

### 9.8 Giao diện, khởi động, annotation – Bước 1 trở đi

| Lớp | Thuộc tính | Phương thức | Ghi chú |
|---|---|---|---|
| `Main` | — | `main(String[] args)` | Điểm vào: chọn nguồn dữ liệu File (Giai đoạn 1) hoặc SQL Server (Giai đoạn 2). Service và UI dùng chung cho cả hai – chỉ khác bộ repository truyền vào. Có thể truyền tham số "file" hoặc "db" để bỏ qua câu hỏi. |
| `ConsoleUI` | `catalog`, `staffService`, `sales`, `reports`, `migration`, `onShift` | `start()`; các menu con `private`: `categoryMenu()`, `productMenu()`, `staffMenu()`, `invoiceMenu()`, `reportMenu()`; `pager(…)` hiện từng trang; `printAuthors()` đọc `@Author` bằng Reflection | Menu console. Chỉ gọi service – không SQL, không đọc/ghi file ở đây. Mỗi thao tác chạy trong `run(Action)` để bắt mọi exception. |
| `Input` | `DATE` (dd/MM/yyyy), `Scanner` dùng chung | `line(…)` – Đọc một dòng (đã trim, có thể rỗng)<br>`required(…)` – Bắt buộc nhập; rỗng thì hỏi lại<br>`optional(…)` – Enter để bỏ trống → null.<br>`integer(…)` – Số nguyên trong khoảng [min, max]; sai thì hỏi lại<br>`optionalInt(…)` – Enter để bỏ trống → null.<br>`decimal(…)` – Số thực ≥ min; sai thì hỏi lại<br>`optionalDecimal(…)` – Enter để bỏ trống → null.<br>`date(…)` – Ngày dạng dd/MM/yyyy; sai thì hỏi lại<br>`optionalDate(…)` – Enter để bỏ trống → null.<br>`choose(…)` – Chọn 1 hằng enum theo số thứ tự. Enum nên có toString() trả nhãn tiếng Việt.<br>`chooseOptional(…)` – Như choose nhưng cho phép 0 = bỏ qua → null.<br>`confirm(…)` – Hỏi có / không (y/n) | Nhập liệu an toàn: luôn đọc cả dòng, nhập sai thì hỏi lại – chương trình không bao giờ crash vì nhập. Mọi phương thức là `static`. |
| `@Author` | `name()`, `task()` | — | Annotation `@Retention(RUNTIME)`, `@Target(TYPE)` gắn lên service; đọc lúc chạy bằng Reflection. |

<!-- END thiet-ke-lop -->
