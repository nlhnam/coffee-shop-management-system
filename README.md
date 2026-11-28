# Hệ Thống Quản Lý Quán Cà Phê & Bán Hàng Tại Quầy (Coffee Shop POS)

Ứng dụng quản lý quán cà phê và bán hàng tại quầy (POS) viết bằng **Java 17+**, thiết kế theo **kiến trúc phân tầng (Layered Architecture)** kết hợp **Repository Pattern**, hỗ trợ chuyển đổi linh hoạt giữa **Bộ nhớ RAM (In-Memory)**, **Tệp nhị phân (`data/*.dat`)** và **Cơ sở dữ liệu SQL Server (`cafe_pos`)**.

---

## 1. Thành Viên Nhóm Phát Triển (5 Thành Viên)

| Thành viên | Vai trò | Nhánh Git chính | Phụ trách chính |
| :--- | :--- | :--- | :--- |
| **Nam** (`nlhnam`) | Tech Lead | `feat/sales` | Kiến trúc lõi, Bán hàng & Hủy đơn, Transaction hóa đơn, Di trú dữ liệu File → SQL Server |
| **Thiện** | Developer | `feat/catalog` | Quản lý Danh mục & Sản phẩm, Nhập kho theo lô (Batch), Tìm kiếm & Phân trang sản phẩm |
| **Tuấn** | Developer | `feat/staff-ui` | Quản lý Nhân viên & Đổi ca trực, Dữ liệu mẫu, Chuẩn hóa UI & Bộ kiểm thử tự động |
| **Bình** | Developer | `feat/report` | Phân trang `Page<T>`, Thống kê doanh thu Stream API (`TK-01`..`TK-07`) & Xuất báo cáo HTML |
| **Tín** | Developer | `feat/file-core`<br>`feat/db-core` | Lớp nền `FileRepository` & phục hồi `.bak`, CSDL SQL Server `cafe_pos`, `DBConnect`, `SqlFilter`, `@Author` Reflection |

---

## 2. Tính Năng Chính Của Hệ Thống

1. **Quản lý Quầy Bán Hàng (POS):**
   * Chọn và đổi nhân viên trực ca (`MANAGER` / `CASHIER`).
   * Lập hóa đơn nhiều món, kiểm tra tự động trạng thái ngừng bán (`STOPPED`) và tồn kho thực tế.
   * Áp dụng giảm giá `0–50%`, hỗ trợ thanh toán Tiền mặt (`CASH`), Thẻ (`CARD`), Ví điện tử (`E_WALLET`).
   * Hủy hóa đơn đã thanh toán và tự động hoàn trả tồn kho nguyên tử.
2. **Quản lý Danh mục, Sản phẩm & Kho hàng:**
   * CRUD danh mục và sản phẩm kèm kiểm tra ràng buộc toàn vẹn tham chiếu.
   * Nhập kho theo lô nhiều sản phẩm cùng lúc.
   * Tìm kiếm đa tiêu chí (từ khóa, danh mục, khoảng giá, trạng thái, sắp hết hàng), sắp xếp và phân trang.
3. **Tra cứu Hóa đơn & Thống kê Doanh thu:**
   * Lọc hóa đơn theo khoảng ngày, nhân viên, trạng thái, phương thức thanh toán và phân trang kết quả.
   * 7 báo cáo thống kê doanh thu (theo ngày, tốp sản phẩm bán chạy, theo nhân viên, phương thức thanh toán, giờ cao điểm, theo danh mục, cảnh báo tồn kho/chậm bán) và xuất báo cáo ra tệp `.html`.
4. **Lưu trữ Đa nguồn & Di trú Dữ liệu:**
   * Chuyển đổi nguồn dữ liệu giữa **In-Memory**, **File nhị phân (`data/*.dat` với sao lưu `.bak`)** và **SQL Server (`cafe_pos`)**.
   * Công cụ di trú dữ liệu từ File sang SQL Server đảm bảo không trùng lặp khóa chính.

---

## 3. Hướng Dẫn Cài Đặt & Chạy Chương Trình

### 3.1. Yêu cầu môi trường
* **JDK 17** trở lên (khuyến nghị JDK 17 hoặc JDK 21).
* **SQL Server** (2017 trở lên) và thư viện `mssql-jdbc-*.jar` trong thư mục `lib/` (dùng cho Giai đoạn 2).
* IDE: Visual Studio Code (Extension Pack for Java) hoặc IntelliJ IDEA / Eclipse.

### 3.2. Cấu hình kết nối SQL Server (Giai đoạn 2)
1. Chạy lần lượt 2 tệp trong thư mục `sql/` trên SQL Server Management Studio (SSMS):
   * `sql/01_schema.sql` — Tạo cơ sở dữ liệu `cafe_pos` và 5 bảng dữ liệu.
   * `sql/02_sample_data.sql` — Nạp dữ liệu mẫu chuẩn.
2. Sao chép tệp `db.properties.example` thành `db.properties` ở thư mục gốc dự án và cập nhật thông tin tài khoản SQL Server của máy bạn:
   ```properties
   db.url=jdbc:sqlserver://localhost:1433;databaseName=cafe_pos;encrypt=true;trustServerCertificate=true;
   db.user=sa
   db.password=your_password
   ```

### 3.3. Biên dịch và Chạy từ Terminal (PowerShell)
Di chuyển vào thư mục gốc của dự án (thư mục chứa `src/`):

```powershell
# 1. Tạo thư mục bin và biên dịch toàn bộ mã nguồn với chuẩn UTF-8
New-Item -ItemType Directory -Force -Path bin | Out-Null
$sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp "lib/*" -d bin $sources

# 2. Chạy ứng dụng POS
java -Dfile.encoding=UTF-8 -cp "bin;lib/*" app.Main

# 3. Chạy bộ kiểm thử tự động toàn diện
java -Dfile.encoding=UTF-8 -cp "bin;lib/*" app.Main --test
```

---

## 4. Dữ Liệu Mẫu Mặc Định (`SampleData`)

Khi khởi tạo lần đầu, hệ thống cung cấp sẵn bộ dữ liệu mẫu đồng nhất giữa bộ nhớ RAM, tệp `.dat` và SQL Server:
* **4 Danh mục:** `DM01` (Cà phê), `DM02` (Trà), `DM03` (Sinh tố và nước ép), `DM04` (Bánh ngọt).
* **4 Nhân viên:** `NV01` (Nguyễn Văn An – Quản lý), `NV02` (Trần Thị Bình – Thu ngân), `NV03` (Lê Văn Chí – Thu ngân), `NV04` (Phạm Thị Dung – Thu ngân).
* **15 Sản phẩm (`SP01`–`SP15`):** Gồm 12 món đang bán (`SELLING`, trong đó có 3 món tồn kho thấp `< 10` là `SP03`, `SP06`, `SP10`) và 3 món ngừng bán (`STOPPED` là `SP04`, `SP08`, `SP15`).
* **30 Hóa đơn lịch sử:** Phân bổ trên nhiều ngày, nhiều nhân viên và bao gồm cả đơn `PAID` lẫn `CANCELLED` để kiểm thử tìm kiếm, phân trang và thống kê doanh thu.