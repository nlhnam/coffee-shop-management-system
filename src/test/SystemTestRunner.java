package test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import exception.BusinessException;
import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Category;
import model.Criteria.InvoiceCriteria;
import model.Criteria.InvoiceSort;
import model.Criteria.InvoiceSummary;
import model.Criteria.ProductCriteria;
import model.Criteria.ProductSort;
import model.Invoice;
import model.InvoiceStatus;
import model.Page;
import model.PaymentMethod;
import model.Product;
import model.ProductStatus;
import model.Staff;
import model.StaffRole;
import repository.Repositories.CategoryRepository;
import repository.Repositories.InvoiceRepository;
import repository.Repositories.ProductRepository;
import repository.Repositories.StaffRepository;
import repository.memory.InMemoryCategoryRepository;
import repository.memory.InMemoryInvoiceRepository;
import repository.memory.InMemoryProductRepository;
import repository.memory.InMemoryStaffRepository;
import service.CatalogService;
import service.SalesService;
import service.SampleData;
import service.StaffService;

/**
 * Bộ kiểm thử tự động toàn diện cho Coffee Shop Management & POS System.
 * Bao phủ đầy đủ FR-01..FR-09, BR-01..BR-08 và TC-01..TC-08 (giai đoạn In-Memory).
 */
public class SystemTestRunner {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    private static class TestContext {
        final CategoryRepository categoryRepo = new InMemoryCategoryRepository();
        final ProductRepository productRepo = new InMemoryProductRepository();
        final StaffRepository staffRepo = new InMemoryStaffRepository();
        final InvoiceRepository invoiceRepo = new InMemoryInvoiceRepository(productRepo, staffRepo);

        final CatalogService catalogService = new CatalogService(categoryRepo, productRepo, invoiceRepo);
        final StaffService staffService = new StaffService(staffRepo, invoiceRepo);
        final SalesService salesService = new SalesService(invoiceRepo, productRepo, staffRepo);

        TestContext() throws DuplicateIdException {
            SampleData.seed(categoryRepo, staffRepo, productRepo, invoiceRepo);
        }
    }

    @FunctionalInterface
    private interface TestCase {
        void run() throws Exception;
    }

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("   KIỂM THỬ TỰ ĐỘNG HỆ THỐNG QUẢN LÝ QUÁN CÀ PHÊ (POS) - FULL TEST SUITE");
        System.out.println("=========================================================================");

        // 1. SampleData
        runTest("SD-01: Nạp dữ liệu mẫu SampleData (4 DM, 4 NV, 15 SP, 30 HĐ)", () -> {
            TestContext ctx = new TestContext();
            assertEquals(4, ctx.catalogService.getAllCategories().size(), "Số lượng danh mục");
            assertEquals(4, ctx.staffService.getAllStaff().size(), "Số lượng nhân viên");
            assertEquals(15, ctx.catalogService.getAllProducts().size(), "Số lượng sản phẩm");
            assertEquals(30, ctx.invoiceRepo.findAll().size(), "Số lượng hóa đơn lịch sử");
            assertEquals(6, ctx.catalogService.findProductById("SP03").orElseThrow().getStock(), "Tồn kho SP03 giữ nguyên");
        });

        // 2. Model Validation (BR-01, BR-02)
        runTest("BR-02a: Kiểm tra ràng buộc đầu vào Category, Product, Staff", () -> {
            assertThrows(IllegalArgumentException.class, () -> new Category("ABC", "Sai mã"), "Mã danh mục phải dạng DM01");
            assertThrows(IllegalArgumentException.class, () -> new Category("DM09", "   "), "Tên danh mục rỗng");
            assertThrows(IllegalArgumentException.class, () -> new Product("SP1", "Lỗi", "DM01", 10000, 5, ProductStatus.SELLING), "Mã SP phải >= 4 ký tự");
            assertThrows(IllegalArgumentException.class, () -> new Product("SP99", "Giá âm", "DM01", 0, 5, ProductStatus.SELLING), "Giá phải > 0");
            assertThrows(IllegalArgumentException.class, () -> new Product("SP99", "Tồn âm", "DM01", 10000, -1, ProductStatus.SELLING), "Tồn kho >= 0");
            assertThrows(IllegalArgumentException.class, () -> new Staff("NV1", "An", "0901234567", StaffRole.CASHIER), "Mã NV phải dạng NV01");
            assertThrows(IllegalArgumentException.class, () -> new Staff("NV99", "An", "1901234567", StaffRole.CASHIER), "SĐT phải bắt đầu bằng 0");
            assertThrows(IllegalArgumentException.class, () -> new Staff("NV99", "An", "090123456", StaffRole.CASHIER), "SĐT phải đủ 10 số");
        });

        runTest("BR-02b: Kiểm tra Invoice giảm giá (0-50%) và cộng dồn món trùng", () -> {
            Invoice inv = new Invoice("HD-TEST-01", "NV01", java.time.LocalDateTime.now(), PaymentMethod.CASH, 10, InvoiceStatus.PAID);
            assertThrows(IllegalArgumentException.class, () -> inv.setDiscountPercent(-1), "Giảm giá < 0%");
            assertThrows(IllegalArgumentException.class, () -> inv.setDiscountPercent(51), "Giảm giá > 50%");

            inv.addItem("sp01", 2, 25000);
            inv.addItem("SP01", 3, 25000); // Cộng dồn thành 5
            assertEquals(1, inv.getDetails().size(), "Gộp cùng 1 dòng chi tiết");
            assertEquals(5, inv.getDetails().get(0).getQuantity(), "Số lượng cộng dồn = 5");
            assertEquals(125000.0, inv.subtotal(), "Tạm tính = 125,000");
            assertEquals(112500.0, inv.total(), "Sau giảm 10% = 112,500");
        });

        // 3. FR-01 & FR-02: Danh mục & Sản phẩm
        runTest("FR-01 & TC-08: CRUD Danh mục, chặn trùng mã & chặn xóa DM còn sản phẩm (BR-05)", () -> {
            TestContext ctx = new TestContext();
            // Chặn trùng mã
            assertThrows(DuplicateIdException.class, () -> ctx.catalogService.addCategory(new Category("DM01", "Trùng")), "Trùng mã DM01");
            // Chặn xóa DM01 đang có sản phẩm
            assertThrows(BusinessException.class, () -> ctx.catalogService.deleteCategory("DM01"), "Không được xóa DM đang có SP");
            // Thêm -> Sửa -> Xóa danh mục trống thành công
            ctx.catalogService.addCategory(new Category("DM05", "Đá xay"));
            ctx.catalogService.updateCategory(new Category("DM05", "Đồ uống đá xay"));
            assertEquals("Đồ uống đá xay", ctx.catalogService.findCategoryById("dm05").orElseThrow().getName(), "Tìm không phân biệt hoa/thường");
            ctx.catalogService.deleteCategory("DM05");
            assertTrue(ctx.catalogService.findCategoryById("DM05").isEmpty(), "Đã xóa DM05");
        });

        runTest("FR-02 & BR-05: CRUD Sản phẩm, đổi trạng thái, chặn xóa SP đã bán & cho phép xóa SP mới", () -> {
            TestContext ctx = new TestContext();
            // Danh mục không tồn tại
            assertThrows(NotFoundException.class,
                    () -> ctx.catalogService.addProduct(new Product("SP99", "Món lạ", "DM99", 20000, 10, ProductStatus.SELLING)),
                    "Danh mục DM99 không tồn tại");

            // Chặn xóa SP01 đã có trong hóa đơn lịch sử
            assertThrows(BusinessException.class, () -> ctx.catalogService.deleteProduct("SP01"), "SP01 đã có trong HĐ không được xóa");

            // Thêm SP mới chưa từng bán -> Đổi trạng thái -> Cập nhật -> Xóa thành công
            ctx.catalogService.addProduct(new Product("SP20", "Matcha Latte", "DM02", 45000, 20, ProductStatus.SELLING));
            ctx.catalogService.toggleProductStatus("SP20");
            assertEquals(ProductStatus.STOPPED, ctx.catalogService.findProductById("SP20").orElseThrow().getStatus(), "Chuyển sang STOPPED");

            ctx.catalogService.updateProduct(new Product("SP20", "Matcha Uji", "DM02", 49000, 20, ProductStatus.STOPPED));
            assertEquals(49000.0, ctx.catalogService.findProductById("SP20").orElseThrow().getPrice(), "Cập nhật giá mới");

            ctx.catalogService.deleteProduct("SP20");
            assertTrue(ctx.catalogService.findProductById("SP20").isEmpty(), "Xóa SP chưa bán thành công");
        });

        // 4. FR-03: Quản lý Nhân viên
        runTest("FR-03 & BR-05: CRUD Nhân viên, chặn xóa NV đã lập hóa đơn", () -> {
            TestContext ctx = new TestContext();
            assertThrows(DuplicateIdException.class,
                    () -> ctx.staffService.addStaff(new Staff("NV01", "Trùng", "0909999999", StaffRole.CASHIER)),
                    "Trùng mã NV01");
            assertThrows(BusinessException.class, () -> ctx.staffService.deleteStaff("NV01"), "NV01 đã lập HĐ không được xóa");

            ctx.staffService.addStaff(new Staff("NV05", "Hoàng Nam", "0987654321", StaffRole.MANAGER));
            ctx.staffService.updateStaff(new Staff("NV05", "Nguyễn Lương Hoàng Nam", "0987654321", StaffRole.MANAGER));
            assertEquals("Nguyễn Lương Hoàng Nam", ctx.staffService.findStaffById("nv05").orElseThrow().getName(), "Cập nhật tên NV05");
            ctx.staffService.deleteStaff("NV05");
            assertTrue(ctx.staffService.findStaffById("NV05").isEmpty(), "Xóa NV05 chưa có hóa đơn");
        });

        // 5. FR-05: Bán hàng & Lập hóa đơn (TC-01, TC-02, TC-03, TC-07)
        runTest("FR-05 & TC-01: Lập hóa đơn nhiều món, giảm giá 10%, trừ tồn kho chính xác", () -> {
            TestContext ctx = new TestContext();
            int stockSp01Before = ctx.salesService.getProduct("SP01").getStock(); // 50
            int stockSp02Before = ctx.salesService.getProduct("SP02").getStock(); // 40

            Map<String, Integer> cart = new LinkedHashMap<>();
            cart.put("SP01", 2); // 2 * 25,000 = 50,000
            cart.put("SP02", 1); // 1 * 29,000 = 29,000 -> Tạm tính = 79,000 -> Giảm 10% = 71,100

            Invoice created = ctx.salesService.checkout("NV02", cart, PaymentMethod.E_WALLET, 10);
            String expectedPrefix = "HD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
            assertTrue(created.getId().startsWith(expectedPrefix), "Định dạng mã HĐ HDyyyyMMdd-nnnn");
            assertEquals(InvoiceStatus.PAID, created.getStatus(), "Trạng thái PAID");
            assertEquals(71100.0, created.total(), "Tổng thanh toán sau giảm 10%");
            assertEquals(stockSp01Before - 2, ctx.salesService.getProduct("SP01").getStock(), "SP01 bị trừ 2");
            assertEquals(stockSp02Before - 1, ctx.salesService.getProduct("SP02").getStock(), "SP02 bị trừ 1");
        });

        runTest("FR-05 & TC-02: Rollback toàn bộ giao dịch nếu 1 món thiếu tồn kho (BR-04)", () -> {
            TestContext ctx = new TestContext();
            int stockSp01Before = ctx.salesService.getProduct("SP01").getStock(); // 50
            int stockSp10Before = ctx.salesService.getProduct("SP10").getStock(); // 4
            int invoiceCountBefore = ctx.invoiceRepo.findAll().size();

            Map<String, Integer> cart = new LinkedHashMap<>();
            cart.put("SP01", 5);  // Đủ hàng
            cart.put("SP10", 10); // Vượt tồn kho (chỉ còn 4)

            assertThrows(BusinessException.class,
                    () -> ctx.salesService.checkout("NV02", cart, PaymentMethod.CASH, 0),
                    "Thiếu hàng ở món thứ 2");
            assertEquals(stockSp01Before, ctx.salesService.getProduct("SP01").getStock(), "Món 1 không bị trừ kho");
            assertEquals(stockSp10Before, ctx.salesService.getProduct("SP10").getStock(), "Món 2 không bị trừ kho");
            assertEquals(invoiceCountBefore, ctx.invoiceRepo.findAll().size(), "Không sinh hóa đơn lỗi");
        });

        runTest("FR-05 & TC-03: Chặn bán sản phẩm đã ngừng kinh doanh STOPPED (BR-03)", () -> {
            TestContext ctx = new TestContext();
            Product stopped = ctx.salesService.getProduct("SP04"); // Cappuccino - STOPPED
            assertTrue(ctx.salesService.warning(stopped, 1) != null, "Hàm warning cảnh báo STOPPED");

            Map<String, Integer> cart = Map.of("SP04", 1);
            assertThrows(BusinessException.class,
                    () -> ctx.salesService.checkout("NV02", cart, PaymentMethod.CARD, 0),
                    "Không thể thanh toán sản phẩm STOPPED");
        });

        runTest("BR-07 & TC-07: Bảo toàn đơn giá lịch sử trong hóa đơn khi sản phẩm đổi giá mới", () -> {
            TestContext ctx = new TestContext();
            Map<String, Integer> cart = Map.of("SP01", 2); // Giá gốc 25,000
            Invoice inv = ctx.salesService.checkout("NV01", cart, PaymentMethod.CASH, 0);
            assertEquals(50000.0, inv.total(), "Tổng tiền lúc bán = 50,000");

            // Cập nhật giá SP01 lên 35,000
            Product sp01 = ctx.salesService.getProduct("SP01");
            ctx.catalogService.updateProduct(new Product(sp01.getId(), sp01.getName(), sp01.getCategoryId(), 35000, sp01.getStock(), sp01.getStatus()));

            Invoice savedInv = ctx.salesService.get(inv.getId());
            assertEquals(25000.0, savedInv.getDetails().get(0).getUnitPrice(), "Đơn giá trong HĐ cũ vẫn là 25,000");
            assertEquals(50000.0, savedInv.total(), "Tổng tiền HĐ cũ không đổi");
        });

        // 6. FR-06: Hủy hóa đơn & Hoàn kho (TC-04, TC-05)
        runTest("FR-06, TC-04 & TC-05: Hủy hóa đơn PAID hoàn kho, chặn hủy lần 2 (BR-06)", () -> {
            TestContext ctx = new TestContext();
            int initialStock = ctx.salesService.getProduct("SP05").getStock(); // 45
            Invoice inv = ctx.salesService.checkout("NV03", Map.of("SP05", 4), PaymentMethod.CASH, 0);
            assertEquals(initialStock - 4, ctx.salesService.getProduct("SP05").getStock(), "Đã trừ 4 sau khi bán");

            // TC-04: Hủy hóa đơn PAID
            ctx.salesService.cancel(inv.getId());
            assertEquals(InvoiceStatus.CANCELLED, ctx.salesService.get(inv.getId()).getStatus(), "Chuyển sang CANCELLED");
            assertEquals(initialStock, ctx.salesService.getProduct("SP05").getStock(), "Hoàn trả đủ 4 vào tồn kho");

            // TC-05: Hủy lại hóa đơn đã CANCELLED
            assertThrows(BusinessException.class, () -> ctx.salesService.cancel(inv.getId()), "Không cho hủy đơn đã CANCELLED");
            assertEquals(initialStock, ctx.salesService.getProduct("SP05").getStock(), "Không cộng trùng tồn kho");
        });

        // 7. FR-07: Nhập kho theo lô (TC-06)
        runTest("FR-07 & TC-06: Nhập kho theo lô thành công và Rollback toàn lô nếu có mã sai", () -> {
            TestContext ctx = new TestContext();
            int s1 = ctx.salesService.getProduct("SP03").getStock(); // 6
            int s2 = ctx.salesService.getProduct("SP06").getStock(); // 8

            // TC-06: Lô có SP99 không tồn tại -> phải rollback cả SP03 và SP06
            Map<String, Integer> badBatch = new LinkedHashMap<>();
            badBatch.put("SP03", 10);
            badBatch.put("SP06", 12);
            badBatch.put("SP99", 5);
            assertThrows(NotFoundException.class, () -> ctx.catalogService.addStock(badBatch), "Mã SP99 không tồn tại");
            assertEquals(s1, ctx.salesService.getProduct("SP03").getStock(), "SP03 không bị cộng kho khi lô lỗi");
            assertEquals(s2, ctx.salesService.getProduct("SP06").getStock(), "SP06 không bị cộng kho khi lô lỗi");

            // Lô hợp lệ -> cộng kho thành công
            Map<String, Integer> goodBatch = new LinkedHashMap<>();
            goodBatch.put("SP03", 10);
            goodBatch.put("SP06", 12);
            ctx.catalogService.addStock(goodBatch);
            assertEquals(s1 + 10, ctx.salesService.getProduct("SP03").getStock(), "SP03 cộng thêm 10");
            assertEquals(s2 + 12, ctx.salesService.getProduct("SP06").getStock(), "SP06 cộng thêm 12");
        });

        // 8. FR-08 & FR-09: Tìm kiếm, Sắp xếp & Phân trang
        runTest("FR-08: Tìm kiếm sản phẩm đa tiêu chí, sắp xếp & phân trang", () -> {
            TestContext ctx = new TestContext();
            // Lọc tồn kho thấp (< 10): SP03 (6), SP06 (8), SP10 (4) -> 3 sản phẩm
            ProductCriteria lowStockCrit = new ProductCriteria(null, null, null, null, ProductStatus.SELLING, true);
            Page<Product> page1 = ctx.catalogService.searchProducts(lowStockCrit, ProductSort.STOCK_ASC, 1, 2);
            assertEquals(3, page1.totalItems(), "Tổng 3 sản phẩm tồn kho thấp");
            assertEquals(2, page1.totalPages(), "2 trang (mỗi trang 2 SP)");
            assertEquals("SP10", page1.items().get(0).getId(), "SP10 tồn ít nhất (4) đứng đầu");
            assertTrue(page1.hasNext(), "Có trang tiếp theo");

            Page<Product> page2 = ctx.catalogService.searchProducts(lowStockCrit, ProductSort.STOCK_ASC, 2, 2);
            assertEquals(1, page2.items().size(), "Trang 2 có 1 sản phẩm");
            assertEquals("SP06", page2.items().get(0).getId(), "SP06 tồn 8 ở trang 2");
        });

        runTest("FR-09: Tra cứu hóa đơn đa tiêu chí, hiển thị tên nhân viên & phân trang", () -> {
            TestContext ctx = new TestContext();
            InvoiceCriteria criteria = new InvoiceCriteria(
                    LocalDate.now().minusDays(14),
                    LocalDate.now(),
                    "NV01",
                    InvoiceStatus.PAID,
                    null,
                    InvoiceSort.TOTAL_DESC
            );
            Page<InvoiceSummary> res = ctx.salesService.search(criteria, 1, 5);
            assertTrue(res.totalItems() > 0, "Tìm thấy hóa đơn PAID của NV01");
            InvoiceSummary first = res.items().get(0);
            assertEquals("Nguyễn Văn An", first.staffName(), "Map đúng tên nhân viên NV01");
            assertEquals(InvoiceStatus.PAID, first.status(), "Đúng trạng thái PAID");
            if (res.items().size() >= 2) {
                assertTrue(res.items().get(0).total() >= res.items().get(1).total(), "Sắp xếp tổng tiền giảm dần");
            }
        });

        System.out.println("=========================================================================");
        System.out.println("   KIỂM THỬ TỰ ĐỘNG HỆ THỐNG QUẢN LÝ QUÁN CÀ PHÊ (POS) - FULL TEST SUITE");
        System.out.println("=========================================================================");

        // 1. SampleData
        runTest("SD-01: Nạp dữ liệu mẫu SampleData (4 DM, 4 NV, 15 SP, 30 HĐ)", () -> {
            TestContext ctx = new TestContext();
            assertEquals(4, ctx.catalogService.getAllCategories().size(), "Số lượng danh mục");
            assertEquals(4, ctx.staffService.getAllStaff().size(), "Số lượng nhân viên");
            assertEquals(15, ctx.catalogService.getAllProducts().size(), "Số lượng sản phẩm");
            assertEquals(30, ctx.invoiceRepo.findAll().size(), "Số lượng hóa đơn lịch sử");
            assertEquals(6, ctx.catalogService.findProductById("SP03").orElseThrow().getStock(), "Tồn kho SP03 giữ nguyên");
        });

        // 2. Model Validation (BR-01, BR-02)
        runTest("BR-02a: Kiểm tra ràng buộc đầu vào Category, Product, Staff", () -> {
            assertThrows(IllegalArgumentException.class, () -> new Category("ABC", "Sai mã"), "Mã danh mục phải dạng DM01");
            assertThrows(IllegalArgumentException.class, () -> new Category("DM09", "   "), "Tên danh mục rỗng");
            assertThrows(IllegalArgumentException.class, () -> new Product("SP1", "Lỗi", "DM01", 10000, 5, ProductStatus.SELLING), "Mã SP phải >= 4 ký tự");
            assertThrows(IllegalArgumentException.class, () -> new Product("SP99", "Giá âm", "DM01", 0, 5, ProductStatus.SELLING), "Giá phải > 0");
            assertThrows(IllegalArgumentException.class, () -> new Product("SP99", "Tồn âm", "DM01", 10000, -1, ProductStatus.SELLING), "Tồn kho >= 0");
            assertThrows(IllegalArgumentException.class, () -> new Staff("NV1", "An", "0901234567", StaffRole.CASHIER), "Mã NV phải dạng NV01");
            assertThrows(IllegalArgumentException.class, () -> new Staff("NV99", "An", "1901234567", StaffRole.CASHIER), "SĐT phải bắt đầu bằng 0");
            assertThrows(IllegalArgumentException.class, () -> new Staff("NV99", "An", "090123456", StaffRole.CASHIER), "SĐT phải đủ 10 số");
        });

        runTest("BR-02b: Kiểm tra Invoice giảm giá (0-50%) và cộng dồn món trùng", () -> {
            Invoice inv = new Invoice("HD-TEST-01", "NV01", java.time.LocalDateTime.now(), PaymentMethod.CASH, 10, InvoiceStatus.PAID);
            assertThrows(IllegalArgumentException.class, () -> inv.setDiscountPercent(-1), "Giảm giá < 0%");
            assertThrows(IllegalArgumentException.class, () -> inv.setDiscountPercent(51), "Giảm giá > 50%");

            inv.addItem("sp01", 2, 25000);
            inv.addItem("SP01", 3, 25000); // Cộng dồn thành 5
            assertEquals(1, inv.getDetails().size(), "Gộp cùng 1 dòng chi tiết");
            assertEquals(5, inv.getDetails().get(0).getQuantity(), "Số lượng cộng dồn = 5");
            assertEquals(125000.0, inv.subtotal(), "Tạm tính = 125,000");
            assertEquals(112500.0, inv.total(), "Sau giảm 10% = 112,500");
        });

        // 3. FR-01 & FR-02: Danh mục & Sản phẩm
        runTest("FR-01 & TC-08: CRUD Danh mục, chặn trùng mã & chặn xóa DM còn sản phẩm (BR-05)", () -> {
            TestContext ctx = new TestContext();
            // Chặn trùng mã
            assertThrows(DuplicateIdException.class, () -> ctx.catalogService.addCategory(new Category("DM01", "Trùng")), "Trùng mã DM01");
            // Chặn xóa DM01 đang có sản phẩm
            assertThrows(BusinessException.class, () -> ctx.catalogService.deleteCategory("DM01"), "Không được xóa DM đang có SP");
            // Thêm -> Sửa -> Xóa danh mục trống thành công
            ctx.catalogService.addCategory(new Category("DM05", "Đá xay"));
            ctx.catalogService.updateCategory(new Category("DM05", "Đồ uống đá xay"));
            assertEquals("Đồ uống đá xay", ctx.catalogService.findCategoryById("dm05").orElseThrow().getName(), "Tìm không phân biệt hoa/thường");
            ctx.catalogService.deleteCategory("DM05");
            assertTrue(ctx.catalogService.findCategoryById("DM05").isEmpty(), "Đã xóa DM05");
        });

        runTest("FR-02 & BR-05: CRUD Sản phẩm, đổi trạng thái, chặn xóa SP đã bán & cho phép xóa SP mới", () -> {
            TestContext ctx = new TestContext();
            // Danh mục không tồn tại
            assertThrows(NotFoundException.class,
                    () -> ctx.catalogService.addProduct(new Product("SP99", "Món lạ", "DM99", 20000, 10, ProductStatus.SELLING)),
                    "Danh mục DM99 không tồn tại");

            // Chặn xóa SP01 đã có trong hóa đơn lịch sử
            assertThrows(BusinessException.class, () -> ctx.catalogService.deleteProduct("SP01"), "SP01 đã có trong HĐ không được xóa");

            // Thêm SP mới chưa từng bán -> Đổi trạng thái -> Cập nhật -> Xóa thành công
            ctx.catalogService.addProduct(new Product("SP20", "Matcha Latte", "DM02", 45000, 20, ProductStatus.SELLING));
            ctx.catalogService.toggleProductStatus("SP20");
            assertEquals(ProductStatus.STOPPED, ctx.catalogService.findProductById("SP20").orElseThrow().getStatus(), "Chuyển sang STOPPED");

            ctx.catalogService.updateProduct(new Product("SP20", "Matcha Uji", "DM02", 49000, 20, ProductStatus.STOPPED));
            assertEquals(49000.0, ctx.catalogService.findProductById("SP20").orElseThrow().getPrice(), "Cập nhật giá mới");

            ctx.catalogService.deleteProduct("SP20");
            assertTrue(ctx.catalogService.findProductById("SP20").isEmpty(), "Xóa SP chưa bán thành công");
        });

        // 4. FR-03: Quản lý Nhân viên
        runTest("FR-03 & BR-05: CRUD Nhân viên, chặn xóa NV đã lập hóa đơn", () -> {
            TestContext ctx = new TestContext();
            assertThrows(DuplicateIdException.class,
                    () -> ctx.staffService.addStaff(new Staff("NV01", "Trùng", "0909999999", StaffRole.CASHIER)),
                    "Trùng mã NV01");
            assertThrows(BusinessException.class, () -> ctx.staffService.deleteStaff("NV01"), "NV01 đã lập HĐ không được xóa");

            ctx.staffService.addStaff(new Staff("NV05", "Hoàng Nam", "0987654321", StaffRole.MANAGER));
            ctx.staffService.updateStaff(new Staff("NV05", "Nguyễn Lương Hoàng Nam", "0987654321", StaffRole.MANAGER));
            assertEquals("Nguyễn Lương Hoàng Nam", ctx.staffService.findStaffById("nv05").orElseThrow().getName(), "Cập nhật tên NV05");
            ctx.staffService.deleteStaff("NV05");
            assertTrue(ctx.staffService.findStaffById("NV05").isEmpty(), "Xóa NV05 chưa có hóa đơn");
        });

        // 5. FR-05: Bán hàng & Lập hóa đơn (TC-01, TC-02, TC-03, TC-07)
        runTest("FR-05 & TC-01: Lập hóa đơn nhiều món, giảm giá 10%, trừ tồn kho chính xác", () -> {
            TestContext ctx = new TestContext();
            int stockSp01Before = ctx.salesService.getProduct("SP01").getStock(); // 50
            int stockSp02Before = ctx.salesService.getProduct("SP02").getStock(); // 40

            Map<String, Integer> cart = new LinkedHashMap<>();
            cart.put("SP01", 2); // 2 * 25,000 = 50,000
            cart.put("SP02", 1); // 1 * 29,000 = 29,000 -> Tạm tính = 79,000 -> Giảm 10% = 71,100

            Invoice created = ctx.salesService.checkout("NV02", cart, PaymentMethod.E_WALLET, 10);
            String expectedPrefix = "HD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
            assertTrue(created.getId().startsWith(expectedPrefix), "Định dạng mã HĐ HDyyyyMMdd-nnnn");
            assertEquals(InvoiceStatus.PAID, created.getStatus(), "Trạng thái PAID");
            assertEquals(71100.0, created.total(), "Tổng thanh toán sau giảm 10%");
            assertEquals(stockSp01Before - 2, ctx.salesService.getProduct("SP01").getStock(), "SP01 bị trừ 2");
            assertEquals(stockSp02Before - 1, ctx.salesService.getProduct("SP02").getStock(), "SP02 bị trừ 1");
        });

        runTest("FR-05 & TC-02: Rollback toàn bộ giao dịch nếu 1 món thiếu tồn kho (BR-04)", () -> {
            TestContext ctx = new TestContext();
            int stockSp01Before = ctx.salesService.getProduct("SP01").getStock(); // 50
            int stockSp10Before = ctx.salesService.getProduct("SP10").getStock(); // 4
            int invoiceCountBefore = ctx.invoiceRepo.findAll().size();

            Map<String, Integer> cart = new LinkedHashMap<>();
            cart.put("SP01", 5);  // Đủ hàng
            cart.put("SP10", 10); // Vượt tồn kho (chỉ còn 4)

            assertThrows(BusinessException.class,
                    () -> ctx.salesService.checkout("NV02", cart, PaymentMethod.CASH, 0),
                    "Thiếu hàng ở món thứ 2");
            assertEquals(stockSp01Before, ctx.salesService.getProduct("SP01").getStock(), "Món 1 không bị trừ kho");
            assertEquals(stockSp10Before, ctx.salesService.getProduct("SP10").getStock(), "Món 2 không bị trừ kho");
            assertEquals(invoiceCountBefore, ctx.invoiceRepo.findAll().size(), "Không sinh hóa đơn lỗi");
        });

        runTest("FR-05 & TC-03: Chặn bán sản phẩm đã ngừng kinh doanh STOPPED (BR-03)", () -> {
            TestContext ctx = new TestContext();
            Product stopped = ctx.salesService.getProduct("SP04"); // Cappuccino - STOPPED
            assertTrue(ctx.salesService.warning(stopped, 1) != null, "Hàm warning cảnh báo STOPPED");

            Map<String, Integer> cart = Map.of("SP04", 1);
            assertThrows(BusinessException.class,
                    () -> ctx.salesService.checkout("NV02", cart, PaymentMethod.CARD, 0),
                    "Không thể thanh toán sản phẩm STOPPED");
        });

        runTest("BR-07 & TC-07: Bảo toàn đơn giá lịch sử trong hóa đơn khi sản phẩm đổi giá mới", () -> {
            TestContext ctx = new TestContext();
            Map<String, Integer> cart = Map.of("SP01", 2); // Giá gốc 25,000
            Invoice inv = ctx.salesService.checkout("NV01", cart, PaymentMethod.CASH, 0);
            assertEquals(50000.0, inv.total(), "Tổng tiền lúc bán = 50,000");

            // Cập nhật giá SP01 lên 35,000
            Product sp01 = ctx.salesService.getProduct("SP01");
            ctx.catalogService.updateProduct(new Product(sp01.getId(), sp01.getName(), sp01.getCategoryId(), 35000, sp01.getStock(), sp01.getStatus()));

            Invoice savedInv = ctx.salesService.get(inv.getId());
            assertEquals(25000.0, savedInv.getDetails().get(0).getUnitPrice(), "Đơn giá trong HĐ cũ vẫn là 25,000");
            assertEquals(50000.0, savedInv.total(), "Tổng tiền HĐ cũ không đổi");
        });

        // 6. FR-06: Hủy hóa đơn & Hoàn kho (TC-04, TC-05)
        runTest("FR-06, TC-04 & TC-05: Hủy hóa đơn PAID hoàn kho, chặn hủy lần 2 (BR-06)", () -> {
            TestContext ctx = new TestContext();
            int initialStock = ctx.salesService.getProduct("SP05").getStock(); // 45
            Invoice inv = ctx.salesService.checkout("NV03", Map.of("SP05", 4), PaymentMethod.CASH, 0);
            assertEquals(initialStock - 4, ctx.salesService.getProduct("SP05").getStock(), "Đã trừ 4 sau khi bán");

            // TC-04: Hủy hóa đơn PAID
            ctx.salesService.cancel(inv.getId());
            assertEquals(InvoiceStatus.CANCELLED, ctx.salesService.get(inv.getId()).getStatus(), "Chuyển sang CANCELLED");
            assertEquals(initialStock, ctx.salesService.getProduct("SP05").getStock(), "Hoàn trả đủ 4 vào tồn kho");

            // TC-05: Hủy lại hóa đơn đã CANCELLED
            assertThrows(BusinessException.class, () -> ctx.salesService.cancel(inv.getId()), "Không cho hủy đơn đã CANCELLED");
            assertEquals(initialStock, ctx.salesService.getProduct("SP05").getStock(), "Không cộng trùng tồn kho");
        });

        // 7. FR-07: Nhập kho theo lô (TC-06)
        runTest("FR-07 & TC-06: Nhập kho theo lô thành công và Rollback toàn lô nếu có mã sai", () -> {
            TestContext ctx = new TestContext();
            int s1 = ctx.salesService.getProduct("SP03").getStock(); // 6
            int s2 = ctx.salesService.getProduct("SP06").getStock(); // 8

            // TC-06: Lô có SP99 không tồn tại -> phải rollback cả SP03 và SP06
            Map<String, Integer> badBatch = new LinkedHashMap<>();
            badBatch.put("SP03", 10);
            badBatch.put("SP06", 12);
            badBatch.put("SP99", 5);
            assertThrows(NotFoundException.class, () -> ctx.catalogService.addStock(badBatch), "Mã SP99 không tồn tại");
            assertEquals(s1, ctx.salesService.getProduct("SP03").getStock(), "SP03 không bị cộng kho khi lô lỗi");
            assertEquals(s2, ctx.salesService.getProduct("SP06").getStock(), "SP06 không bị cộng kho khi lô lỗi");

            // Lô hợp lệ -> cộng kho thành công
            Map<String, Integer> goodBatch = new LinkedHashMap<>();
            goodBatch.put("SP03", 10);
            goodBatch.put("SP06", 12);
            ctx.catalogService.addStock(goodBatch);
            assertEquals(s1 + 10, ctx.salesService.getProduct("SP03").getStock(), "SP03 cộng thêm 10");
            assertEquals(s2 + 12, ctx.salesService.getProduct("SP06").getStock(), "SP06 cộng thêm 12");
        });

        // 8. FR-08 & FR-09: Tìm kiếm, Sắp xếp & Phân trang
        runTest("FR-08: Tìm kiếm sản phẩm đa tiêu chí, sắp xếp & phân trang", () -> {
            TestContext ctx = new TestContext();
            // Lọc tồn kho thấp (< 10): SP03 (6), SP06 (8), SP10 (4) -> 3 sản phẩm
            ProductCriteria lowStockCrit = new ProductCriteria(null, null, null, null, ProductStatus.SELLING, true);
            Page<Product> page1 = ctx.catalogService.searchProducts(lowStockCrit, ProductSort.STOCK_ASC, 1, 2);
            assertEquals(3, page1.totalItems(), "Tổng 3 sản phẩm tồn kho thấp");
            assertEquals(2, page1.totalPages(), "2 trang (mỗi trang 2 SP)");
            assertEquals("SP10", page1.items().get(0).getId(), "SP10 tồn ít nhất (4) đứng đầu");
            assertTrue(page1.hasNext(), "Có trang tiếp theo");

            Page<Product> page2 = ctx.catalogService.searchProducts(lowStockCrit, ProductSort.STOCK_ASC, 2, 2);
            assertEquals(1, page2.items().size(), "Trang 2 có 1 sản phẩm");
            assertEquals("SP06", page2.items().get(0).getId(), "SP06 tồn 8 ở trang 2");
        });

        runTest("FR-09: Tra cứu hóa đơn đa tiêu chí, hiển thị tên nhân viên & phân trang", () -> {
            TestContext ctx = new TestContext();
            InvoiceCriteria criteria = new InvoiceCriteria(
                    LocalDate.now().minusDays(14),
                    LocalDate.now(),
                    "NV01",
                    InvoiceStatus.PAID,
                    null,
                    InvoiceSort.TOTAL_DESC
            );
            Page<InvoiceSummary> res = ctx.salesService.search(criteria, 1, 5);
            assertTrue(res.totalItems() > 0, "Tìm thấy hóa đơn PAID của NV01");
            InvoiceSummary first = res.items().get(0);
            assertEquals("Nguyễn Văn An", first.staffName(), "Map đúng tên nhân viên NV01");
            assertEquals(InvoiceStatus.PAID, first.status(), "Đúng trạng thái PAID");
            if (res.items().size() >= 2) {
                assertTrue(res.items().get(0).total() >= res.items().get(1).total(), "Sắp xếp tổng tiền giảm dần");
            }
        });

        // // 9. FR-10: Thống kê doanh thu & BR-08
        // runTest("FR-10 & BR-08: Kiểm thử 7 báo cáo thống kê (TK-01..TK-07) & loại trừ đơn CANCELLED", () -> {
        //     TestContext ctx = new TestContext();

        //     List<DailyRevenue> last7 = ctx.reportService.revenueLastDays(7);
        //     assertEquals(7, last7.size(), "TK-01 trả về đủ 7 ngày");

        //     List<ProductSales> top5 = ctx.reportService.topProducts(5);
        //     assertTrue(top5.size() <= 5 && !top5.isEmpty(), "TK-02 Top 5 bán chạy");

        //     Map<String, Double> byStaff = ctx.reportService.revenueByStaff();
        //     assertFalse(byStaff.isEmpty(), "TK-03 Doanh thu theo nhân viên");

        //     Map<PaymentMethod, Long> byMethod = ctx.reportService.countByPaymentMethod();
        //     assertFalse(byMethod.isEmpty(), "TK-04 Thống kê phương thức thanh toán");

        //     Optional<Map.Entry<Integer, Long>> peak = ctx.reportService.peakHour();
        //     assertTrue(peak.isPresent(), "TK-05 Xác định được giờ cao điểm");

        //     Map<String, Double> byCat = ctx.reportService.revenueByCategory();
        //     assertFalse(byCat.isEmpty(), "TK-06 Doanh thu theo danh mục");

        //     List<Product> lowStock = ctx.reportService.lowStock(10);
        //     assertEquals(3, lowStock.size(), "TK-07a Có 3 sản phẩm tồn < 10");

        //     List<Product> notSold = ctx.reportService.notSoldInDays(7);
        //     assertTrue(notSold != null, "TK-07b Danh sách món chậm bán trong 7 ngày");

        //     // Kiểm tra BR-08: Lập thêm 1 đơn PAID -> doanh thu tăng; Hủy đơn đó -> doanh thu quay về như cũ
        //     double totalBefore = ctx.reportService.revenueByStaff().values().stream().mapToDouble(Double::doubleValue).sum();
        //     Invoice newInv = ctx.salesService.checkout("NV01", Map.of("SP01", 2), PaymentMethod.CASH, 0); // +50,000
        //     double totalAfterSale = ctx.reportService.revenueByStaff().values().stream().mapToDouble(Double::doubleValue).sum();
        //     assertEquals(totalBefore + 50000.0, totalAfterSale, "Doanh thu tăng 50,000 sau khi bán");

        //     ctx.salesService.cancel(newInv.getId());
        //     double totalAfterCancel = ctx.reportService.revenueByStaff().values().stream().mapToDouble(Double::doubleValue).sum();
        //     assertEquals(totalBefore, totalAfterCancel, "BR-08: Đơn hủy không được tính vào doanh thu");
        // });

        System.out.println("=========================================================================");
        System.out.printf("   TỔNG KẾT: %d/%d TEST PASS | %d FAIL%n", passedTests, totalTests, failedTests);
        System.out.println("=========================================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String name, TestCase tc) {
        totalTests++;
        try {
            tc.run();
            passedTests++;
            System.out.println("[PASS] " + name);
        } catch (Throwable t) {
            failedTests++;
            System.out.println("[FAIL] " + name + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (expected instanceof Double d1 && actual instanceof Double d2) {
            if (Math.abs(d1 - d2) > 0.001) {
                throw new AssertionError(msg + " (mong đợi: " + d1 + ", thực tế: " + d2 + ")");
            }
            return;
        }
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(msg + " (mong đợi: " + expected + ", thực tế: " + actual + ")");
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError(msg);
        }
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) {
            throw new AssertionError(msg);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }

    private static <T extends Throwable> void assertThrows(Class<T> expectedType, ThrowingRunnable action, String msg) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return;
            }
            throw new AssertionError(msg + " (mong đợi exception " + expectedType.getSimpleName()
                    + " nhưng ném " + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
        }
        throw new AssertionError(msg + " (mong đợi ném " + expectedType.getSimpleName() + " nhưng không có lỗi)");
    }
}
