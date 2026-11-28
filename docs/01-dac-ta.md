# ĐẶC TẢ YÊU CẦU & THIẾT KẾ HỆ THỐNG
## Dự án: Hệ thống Quản lý Quán Cà Phê & Bán Hàng tại Quầy (Coffee Shop POS)

* **Môn học:** MOB1024 – Lập trình Java 2 (FPT Polytechnic)
* **Kho mã nguồn:** `https://github.com/nlhnam/coffee-shop-management-system`
* **Công nghệ cốt lõi:** Java 17+, Java Collections & Stream API, File I/O & Serialization, SQL Server & JDBC, Custom Annotation & Reflection.
* **Quy mô nhóm:** 5 thành viên.

---

## 1. Tổng Quan Dự Án & Phân Công Thành Viên

### 1.1. Mục tiêu & Phạm vi
Hệ thống **Coffee Shop POS** được xây dựng theo kiến trúc phân tầng chuẩn (Layered Architecture + Repository Pattern) nhằm phục vụ nghiệp vụ bán hàng tại quầy, quản lý thực đơn, kiểm soát tồn kho, quản lý nhân viên trực ca và thống kê doanh thu quán cà phê.

Dự án triển khai xuyên suốt 2 giai đoạn học tập:
* **Giai đoạn 1 (Bước 1 – Bước 5):** Hoàn thiện toàn bộ mô hình miền, xử lý ngoại lệ, nghiệp vụ POS, tìm kiếm/phân trang, thống kê Stream API, xuất báo cáo HTML và lưu trữ dữ liệu bền vững xuống tệp nhị phân (`data/*.dat`) kèm cơ chế tự phục hồi tệp lỗi (`.bak`).
* **Giai đoạn 2 (Bước 6 – Bước 8):** Thiết kế cơ sở dữ liệu quan hệ SQL Server (`cafe_pos`), triển khai tầng truy cập dữ liệu JDBC (`PreparedStatement`, Transaction, Batch), chuyển đổi nguồn dữ liệu linh hoạt giữa File và SQL Server, di trú dữ liệu (Migration) và gắn siêu dữ liệu tác giả bằng `@Author` Reflection.

### 1.2. Bảng phân công 5 thành viên

| STT | Thành viên | Vai trò | Nhánh Git | Phân hệ & Trách nhiệm chính |
| :-: | :--- | :--- | :--- | :--- |
| 1 | **Nam** (`nlhnam`) | **Tech Lead / Sales & Core** | `feat/sales` | • Thiết kế kiến trúc tổng thể, `GenericRepository`, Custom Exception.<br>• Nghiệp vụ bán hàng tại quầy (`FR-05`), hủy hóa đơn hoàn kho (`FR-06`), tìm kiếm hóa đơn (`FR-09`).<br>• Cài đặt `FileInvoiceRepository`, `JdbcInvoiceRepository` (Transaction lập & hủy đơn).<br>• Xây dựng `MigrationService` (`FR-14`), menu chọn nguồn dữ liệu (`FR-13`) và duyệt Pull Request. |
| 2 | **Thiện** | **Catalog & Inventory** | `feat/catalog` | • Quản lý danh mục (`FR-01`) và sản phẩm (`FR-02`).<br>• Nhập kho theo lô đa sản phẩm (`FR-07`) sử dụng `JDBC Batch` trong Transaction.<br>• Tìm kiếm, lọc đa tiêu chí, sắp xếp và phân trang sản phẩm (`FR-08`).<br>• Cài đặt `FileCategoryRepository`, `FileProductRepository`, `JdbcCategoryRepository`, `JdbcProductRepository`. |
| 3 | **Tuấn** | **Staff, UI & QA** | `feat/staff-ui` | • Quản lý nhân viên (`FR-03`) và chọn/đổi nhân viên trực ca (`FR-04`).<br>• Xây dựng `Input`, `SampleData` (30 hóa đơn mẫu đa trạng thái) và chuẩn hóa định dạng hiển thị (`NFR-06`).<br>• Truy vấn `JOIN` đa bảng và phân trang hóa đơn trên SQL Server (`FR-09`).<br>• Xây dựng và vận hành bộ kiểm thử tự động `SystemTestRunner`. |
| 4 | **Bình** | **Statistics & Reporting** | `feat/report` | • Cấu trúc phân trang `Page<T>` và tiêu chí lọc `Criteria`.<br>• Phân hệ thống kê doanh thu (`FR-10`: 7 chỉ số `TK-01`..`TK-07` dùng Stream API, loại trừ đơn hủy `BR-08`).<br>• Giao diện `ReportMenu` và chức năng xuất báo cáo doanh thu ra tệp HTML chuẩn UTF-8 (`FR-11`). |
| 5 | **Tín** | **Persistence & DB Infrastructure** | `feat/file-core`<br>`feat/db-core` | • **Giai đoạn 1 (`feat/file-core`):** Xây dựng lớp nền `FileRepository<T, ID>` đọc/ghi `data/*.dat`, cơ chế tự đổi tên tệp hỏng sang `.bak` (`NFR-05`) và cài đặt `FileStaffRepository`.<br>• **Giai đoạn 2 (`feat/db-core`):** Viết kịch bản SQL khởi tạo CSDL `cafe_pos` (`01_schema.sql`, `02_sample_data.sql`), xây dựng `DBConnect`, `SqlFilter`, `JdbcStaffRepository` và `@Author` Reflection. |

---

## 2. Kiến Trúc Hệ Thống & Cấu Trúc Thư Mục

Hệ thống áp dụng **Repository Pattern** kết hợp **Dependency Injection** qua Constructor. Tầng `Service` và `UI` chỉ giao tiếp thông qua các interface trong `repository.Repositories`, hoàn toàn độc lập với cơ chế lưu trữ bên dưới (RAM, File `.dat` hay SQL Server).

```text
coffee-shop-management-system/
├── docs/
│   └── 01-dac-ta.md                    # Tài liệu đặc tả yêu cầu & thiết kế hệ thống
├── sql/
│   ├── 01_schema.sql                   # Script tạo CSDL cafe_pos, 5 bảng & ràng buộc (Tín)
│   └── 02_sample_data.sql              # Script nạp dữ liệu mẫu chuẩn khớp SampleData (Tín)
├── lib/                                # Thư viện JDBC Driver (mssql-jdbc-*.jar)
├── data/                               # Thư mục lưu tệp nhị phân *.dat (bỏ qua trên Git)
├── reports/                            # Thư mục xuất báo cáo HTML (bỏ qua trên Git)
├── src/
│   ├── annotation/
│   │   └── Author.java                 # Custom Annotation @Author & Reflection đọc thông tin (Tín)
│   ├── app/
│   │   └── Main.java                   # Điểm khởi chạy, chọn nguồn dữ liệu & chế độ --test (Nam)
│   ├── exception/
│   │   ├── BusinessException.java      # Lỗi vi phạm quy tắc nghiệp vụ
│   │   ├── DataAccessException.java    # Lỗi truy xuất File I/O hoặc SQL/JDBC
│   │   ├── DuplicateIdException.java   # Lỗi trùng khóa chính / mã định danh
│   │   └── NotFoundException.java      # Lỗi không tìm thấy thực thể theo mã
│   ├── model/
│   │   ├── Identifiable.java           # Interface chuẩn hóa getId() cho GenericRepository
│   │   ├── Category.java               # Thực thể Danh mục (DMxx)
│   │   ├── Product.java                # Thực thể Sản phẩm (SPxx)
│   │   ├── ProductStatus.java          # Enum: SELLING, STOPPED
│   │   ├── Staff.java                  # Thực thể Nhân viên (NVxx)
│   │   ├── StaffRole.java              # Enum: MANAGER, CASHIER
│   │   ├── Invoice.java                # Thực thể Hóa đơn (HDyyyyMMdd-xxxx)
│   │   ├── InvoiceDetail.java          # Thực thể Chi tiết hóa đơn
│   │   ├── InvoiceStatus.java          # Enum: PAID, CANCELLED
│   │   ├── PaymentMethod.java          # Enum: CASH, CARD, E_WALLET
│   │   ├── Page.java                   # Record phân trang tổng quát Page<T>
│   │   └── Criteria.java               # Các DTO tiêu chí lọc, sắp xếp & thống kê
│   ├── repository/
│   │   ├── GenericRepository.java      # Interface CRUD chuẩn <T, ID>
│   │   ├── Repositories.java           # Các interface Repository chuyên biệt
│   │   ├── memory/                     # Lưu trữ trên RAM (In-Memory)
│   │   ├── file/                       # Lưu trữ tệp nhị phân data/*.dat (Tín, Nam, Thiện)
│   │   └── jdbc/                       # Lưu trữ SQL Server cafe_pos (Tín, Nam, Thiện, Tuấn)
│   ├── service/
│   │   ├── CatalogService.java         # Nghiệp vụ danh mục, sản phẩm & nhập kho (Thiện)
│   │   ├── StaffService.java           # Nghiệp vụ nhân viên (Tuấn)
│   │   ├── SalesService.java           # Nghiệp vụ lập đơn, hủy đơn & tra cứu (Nam)
│   │   ├── ReportService.java        # Nghiệp vụ thống kê & xuất báo cáo HTML (Bình)
│   │   ├── MigrationService.java       # Di trú dữ liệu File -> SQL Server (Nam)
│   │   └── SampleData.java             # Khởi tạo dữ liệu mẫu chuẩn (Tuấn)
│   ├── ui/
│   │   ├── Input.java                  # Tiện ích nhập & kiểm tra dữ liệu Console (Tuấn)
│   │   ├── ConsoleUI.java              # Menu chính & luồng bán hàng POS (Nam)
│   │   ├── CatalogMenu.java            # Giao diện quản lý danh mục & sản phẩm (Thiện)
│   │   ├── StaffMenu.java              # Giao diện quản lý nhân viên & đổi ca (Tuấn)
│   │   └── ReportMenu.java             # Giao diện thống kê & xuất HTML (Bình)
│   └── test/
│       └── SystemTestRunner.java       # Bộ kiểm thử tự động toàn diện (Tuấn & Nam)
├── db.properties.example               # Mẫu cấu hình kết nối SQL Server
├── CONTRIBUTING.md                     # Quy trình làm việc nhóm & chuẩn Git
├── PULL_REQUEST_TEMPLATE.md            # Mẫu kiểm duyệt Pull Request
└── README.md                           # Hướng dẫn cài đặt & vận hành hệ thống
```

---

## 3. Đặc Tả Yêu Cầu Chức Năng & Quy Tắc Nghiệp Vụ

### 3.1. Danh sách Yêu cầu Chức năng (`FR-01` – `FR-14`)

| Mã | Tên chức năng | Mô tả chi tiết | Phụ trách |
| :--- | :--- | :--- | :--- |
| **FR-01** | Quản lý Danh mục | Thêm, sửa, xóa, xem danh sách danh mục (`DM01`..); chặn xóa danh mục đang có sản phẩm thuộc về. | Thiện |
| **FR-02** | Quản lý Sản phẩm | Thêm, sửa, đổi trạng thái (`SELLING`/`STOPPED`), xóa sản phẩm; chặn xóa sản phẩm đã từng xuất hiện trong hóa đơn. | Thiện |
| **FR-03** | Quản lý Nhân viên | Thêm, sửa thông tin, phân quyền (`MANAGER`/`CASHIER`), xóa nhân viên; chặn xóa nhân viên đã lập hóa đơn. | Tuấn / Tín |
| **FR-04** | Chọn & Đổi ca trực | Bắt buộc chọn nhân viên trực ca khi vào quầy POS; hỗ trợ đổi ca trực trong phiên làm việc và ghi nhận đúng `staffId` lên hóa đơn. | Tuấn |
| **FR-05** | Bán hàng / Lập hóa đơn | Thêm nhiều món vào giỏ hàng, kiểm tra trạng thái đang bán và tồn kho, áp dụng giảm giá `0–50%`, chọn phương thức thanh toán (`CASH`, `CARD`, `E_WALLET`), lưu hóa đơn và trừ tồn kho nguyên tử. | Nam |
| **FR-06** | Hủy hóa đơn | Chuyển trạng thái hóa đơn từ `PAID` sang `CANCELLED` và tự động cộng hoàn trả số lượng tồn kho cho từng món trong đơn. | Nam |
| **FR-07** | Nhập kho theo lô | Nhập thêm số lượng tồn kho cho nhiều sản phẩm cùng lúc trong một phiên; kiểm tra toàn bộ danh sách hợp lệ trước khi cập nhật (sử dụng JDBC Batch ở GĐ2). | Thiện |
| **FR-08** | Tìm kiếm & Phân trang Sản phẩm | Tìm theo từ khóa (mã/tên), danh mục, khoảng giá, trạng thái, chỉ món sắp hết hàng (`stock < 10`); hỗ trợ sắp xếp và phân trang `Page<Product>`. | Thiện |
| **FR-09** | Tra cứu & Phân trang Hóa đơn | Lọc hóa đơn theo khoảng ngày, mã nhân viên, trạng thái, phương thức thanh toán; hiển thị tên nhân viên, sắp xếp và phân trang `Page<InvoiceSummary>`. | Nam / Tuấn |
| **FR-10** | Thống kê Doanh thu | Cung cấp 7 báo cáo thống kê (`TK-01` đến `TK-07`) bằng Stream API; mọi phép tính doanh thu chỉ tính hóa đơn `PAID`. | Bình |
| **FR-11** | Xuất báo cáo HTML | Xuất báo cáo doanh thu tổng hợp và danh sách món bán chạy ra tệp `.html` chuẩn `UTF-8` bằng Java Text Block. | Bình |
| **FR-12** | Lưu trữ File nhị phân | Lưu và đọc toàn bộ dữ liệu thực thể xuống `data/*.dat` qua Serialization (`FileRepository<T, ID>`). | Tín / Nam / Thiện |
| **FR-13** | Lưu trữ SQL Server & Chuyển đổi nguồn | Cài đặt tầng `JdbcRepository` kết nối SQL Server `cafe_pos` và cho phép chọn nguồn dữ liệu (RAM / File / SQL Server) khi khởi động. | Tín / Nam / Thiện |
| **FR-14** | Di trú dữ liệu (Migration) | Chuyển dữ liệu từ tệp `data/*.dat` sang SQL Server theo đúng thứ tự khóa ngoại (`Category` → `Staff` → `Product` → `Invoice`), tự động bỏ qua bản ghi đã tồn tại. | Nam |

### 3.2. Quy tắc Nghiệp vụ Bắt buộc (`BR-01` – `BR-08`)
* **BR-01 (Tính duy nhất của mã định danh):** Mã thực thể không phân biệt chữ hoa/thường khi tra cứu, tự động chuẩn hóa in hoa (`DM01`, `SP01`, `NV01`). Trùng mã khi thêm mới ném `DuplicateIdException`.
* **BR-02 (Ràng buộc giá & tồn kho):** Giá bán sản phẩm phải `> 0`; số lượng tồn kho phải `>= 0`.
* **BR-03 (Sản phẩm ngừng bán):** Sản phẩm có trạng thái `STOPPED` không được phép thêm vào giỏ hàng khi lập hóa đơn mới.
* **BR-04 (Kiểm soát tồn kho khi bán):** Số lượng đặt mua của mỗi món không được vượt quá tồn kho hiện tại (`stock`).
* **BR-05 (Giới hạn chiết khấu):** Phần trăm giảm giá trên hóa đơn phải nằm trong đoạn `[0, 50]`.
* **BR-06 (Tính toàn vẹn tham chiếu):** Không cho phép xóa danh mục đang chứa sản phẩm; không cho phép xóa sản phẩm hoặc nhân viên đã có dữ liệu hóa đơn liên quan.
* **BR-07 (Quy tắc hủy đơn):** Chỉ được hủy hóa đơn đang ở trạng thái `PAID`; hóa đơn đã `CANCELLED` không thể hủy lần hai.
* **BR-08 (Nguyên tắc tính doanh thu):** Các hóa đơn có trạng thái `CANCELLED` bị loại trừ hoàn toàn khỏi mọi báo cáo doanh thu và số lượng bán ra.

### 3.3. Danh mục 7 Chỉ số Thống kê (`TK-01` – `TK-07`)
* **TK-01:** Doanh thu và số lượng đơn theo từng ngày trong `N` ngày gần nhất (`DailyRevenue`, điền đủ cả ngày có doanh thu `0 đ`).
* **TK-02:** Tốp `N` sản phẩm bán chạy nhất theo tổng số lượng bán (`ProductSales`).
* **TK-03:** Tổng doanh thu theo từng nhân viên thu ngân/quản lý.
* **TK-04:** Số lượng hóa đơn theo từng phương thức thanh toán (`CASH`, `CARD`, `E_WALLET`).
* **TK-05:** Khung giờ cao điểm (0–23h) có số lượng giao dịch thành công nhiều nhất.
* **TK-06:** Cơ cấu doanh thu theo từng danh mục sản phẩm.
* **TK-07:** Cảnh báo vận hành gồm danh sách sản phẩm tồn kho thấp (`stock < ngưỡng`) và danh sách sản phẩm đang bán nhưng không phát sinh đơn trong `N` ngày gần đây.

---

## 4. Thiết Kế Mô Hình Dữ Liệu & Cơ Sở Dữ Liệu

### 4.1. Các lớp Thực thể (`model`)
* `Category(String id, String name)`
* `Product(String id, String name, String categoryId, double price, int stock, ProductStatus status)`
* `Staff(String id, String name, String phone, StaffRole role)`
* `Invoice(String id, String staffId, LocalDateTime createdAt, PaymentMethod paymentMethod, int discountPercent, InvoiceStatus status, List<InvoiceDetail> details)`
* `InvoiceDetail(String invoiceId, String productId, int quantity, double unitPrice)` — lưu `unitPrice` tại thời điểm bán để bảo toàn lịch sử giá khi giá sản phẩm thay đổi về sau.

### 4.2. Lược đồ CSDL SQL Server (`cafe_pos`)
Cơ sở dữ liệu gồm 5 bảng quan hệ với các ràng buộc toàn vẹn ở mức CSDL:
1. `categories(id VARCHAR(10) PK, name NVARCHAR(100) NOT NULL)`
2. `products(id VARCHAR(10) PK, name NVARCHAR(150) NOT NULL, category_id VARCHAR(10) FK, price DECIMAL(12,2) CHECK(price > 0), stock INT CHECK(stock >= 0), status VARCHAR(20) CHECK(status IN ('SELLING','STOPPED')))`
3. `staff(id VARCHAR(10) PK, name NVARCHAR(100) NOT NULL, phone VARCHAR(15) NOT NULL, role VARCHAR(20) CHECK(role IN ('MANAGER','CASHIER')))`
4. `invoices(id VARCHAR(20) PK, staff_id VARCHAR(10) FK, created_at DATETIME2 NOT NULL, payment_method VARCHAR(20) CHECK(payment_method IN ('CASH','CARD','E_WALLET')), discount_percent INT CHECK(discount_percent BETWEEN 0 AND 50), status VARCHAR(20) CHECK(status IN ('PAID','CANCELLED')))`
5. `invoice_details(invoice_id VARCHAR(20) FK, product_id VARCHAR(10) FK, quantity INT CHECK(quantity >= 1), unit_price DECIMAL(12,2) CHECK(unit_price > 0), PRIMARY KEY(invoice_id, product_id))`

---

## 5. Yêu Cầu Phi Chức Năng (`NFR-01` – `NFR-06`)

* **NFR-01 (Toàn vẹn giao dịch):** Thao tác lập hóa đơn (`FR-05`), hủy hóa đơn (`FR-06`) và nhập kho theo lô (`FR-07`) phải đảm bảo tính nguyên tử (Transaction `commit`/`rollback`).
* **NFR-02 (Bảo mật & Chống SQL Injection):** Mọi truy vấn động trong `SqlFilter` và `JdbcRepository` phải dùng tham số `?` của `PreparedStatement`; thông tin đăng nhập SQL Server lưu tại `db.properties` (bị loại trừ khỏi Git).
* **NFR-03 (Kiến trúc mở rộng):** Việc bổ sung nguồn lưu trữ mới không làm thay đổi mã nguồn tầng `Service` và `UI`.
* **NFR-04 (Mã hóa UTF-8):** Toàn bộ mã nguồn, dữ liệu tệp, tệp HTML xuất ra và chuỗi Unicode (`NVARCHAR`) trên SQL Server hiển thị chính xác tiếng Việt có dấu.
* **NFR-05 (Khả năng phục hồi tệp lỗi):** Khi tệp `.dat` bị hỏng định dạng hoặc không đọc được, `FileRepository` tự động sao lưu tệp lỗi sang `<tên-tệp>.bak` và khởi tạo trạng thái rỗng an toàn mà không làm dừng chương trình.
* **NFR-06 (Chuẩn định dạng hiển thị):** Tiền tệ hiển thị phân cách hàng nghìn kèm đơn vị `đ` (`%,.0f đ`); ngày giờ hiển thị thống nhất theo mẫu `dd/MM/yyyy HH:mm`.

---

## 6. Lộ Trình Triển Khai 8 Bước

| Bước | Nội dung trọng tâm | Mốc bàn giao |
| :--- | :--- | :--- |
| **Bước 1–2** | Xây dựng 5 lớp thực thể `model`, 4 `Enum`, `Input`, `SampleData`, luồng CRUD và bán hàng cơ bản. | Hoàn thành |
| **Bước 3** | Lọc đa tiêu chí, sắp xếp, phân trang `Page<T>` bằng Stream API và `ReportService` (`TK-01`..`TK-07`). | Hoàn thành tìm kiếm/phân trang; Bình hoàn tất `ReportService` |
| **Bước 4** | Hệ thống `Exception` riêng, `GenericRepository<T, ID>`, `InMemory*Repository` và bộ test `SystemTestRunner`. | Hoàn thành |
| **Bước 5** | **Mốc 1:** Xây dựng `FileRepository<T, ID>` (Tín), lưu/đọc `data/*.dat`, sao lưu `.bak` (Tín, Nam, Thiện) và xuất báo cáo HTML (Bình). | **Đang triển khai** |
| **Bước 6** | Tạo CSDL `cafe_pos` (`01_schema.sql`, `02_sample_data.sql`), viết `DBConnect`, `SqlFilter` và `JdbcStaffRepository` (Tín). | Kế hoạch GĐ2 |
| **Bước 7** | Triển khai `JdbcCategoryRepository`, `JdbcProductRepository` (Batch), `JdbcInvoiceRepository` (Transaction, JOIN phân trang) và `MigrationService` (Nam, Thiện, Tuấn). | Kế hoạch GĐ2 |
| **Bước 8** | **Mốc 2:** Gắn `@Author` Reflection lên các lớp (Tín), kiểm thử tích hợp toàn bộ trên cả 2 nguồn dữ liệu và nghiệm thu. | Kế hoạch GĐ2 |

---

## 7. Kịch Bản Kiểm Thử Nghiệm Thu (`TC-01` – `TC-11`)

* **TC-01:** Khởi tạo dữ liệu mẫu chuẩn (`4` danh mục, `4` nhân viên, `15` sản phẩm, `30` hóa đơn).
* **TC-02:** Lập hóa đơn hợp lệ nhiều món có giảm giá, kiểm tra tính tổng tiền và tự động trừ tồn kho.
* **TC-03:** Chặn bán sản phẩm `STOPPED` (`BR-03`), chặn mua vượt tồn kho (`BR-04`) và chặn giảm giá ngoài khoảng `0–50%` (`BR-05`).
* **TC-04:** Hủy hóa đơn `PAID`, kiểm tra trạng thái chuyển sang `CANCELLED`, cộng trả tồn kho và chặn hủy lần hai (`BR-07`).
* **TC-05:** Chặn xóa danh mục đang có sản phẩm và chặn xóa sản phẩm/nhân viên đã có hóa đơn (`BR-06`).
* **TC-06:** Nhập kho theo lô nhiều sản phẩm; kiểm tra rollback toàn bộ nếu có một sản phẩm không hợp lệ (`FR-07`).
* **TC-07:** Tìm kiếm, lọc đa tiêu chí, sắp xếp và phân trang sản phẩm (`FR-08`) và hóa đơn (`FR-09`).
* **TC-08:** Kiểm thử 7 báo cáo thống kê (`TK-01`..`TK-07`) và xác minh đơn `CANCELLED` không tính vào doanh thu (`BR-08`).
* **TC-09:** Lưu và đọc lại dữ liệu bền vững qua `data/*.dat` (`FR-12`).
* **TC-10:** Phục hồi an toàn và tạo tệp `.bak` khi tệp `.dat` bị hỏng (`NFR-05`).
* **TC-11:** Di trú dữ liệu từ File sang SQL Server (`FR-14`) đảm bảo không trùng lặp bản ghi khi chạy lại.