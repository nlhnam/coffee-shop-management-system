package ui;

import exception.BusinessException;
import exception.NotFoundException;
import model.Criteria.InvoiceCriteria;
import model.Criteria.InvoiceSort;
import model.Criteria.InvoiceSummary;
import model.Invoice;
import model.InvoiceDetail;
import model.InvoiceStatus;
import model.Page;
import model.PaymentMethod;
import model.Product;
import model.ProductStatus;
import model.Staff;
import repository.Repositories.StaffRepository;
import service.SalesService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConsoleUI {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final StaffRepository staffRepo;
    private final SalesService sales;
    private final CatalogMenu catalogMenu;
    private final StaffMenu staffMenu;
    private Staff onShift;

    public ConsoleUI(StaffRepository staffRepo, SalesService sales,
                     CatalogMenu catalogMenu, StaffMenu staffMenu) {
        this.staffRepo = staffRepo;
        this.sales = sales;
        this.catalogMenu = catalogMenu;
        this.staffMenu = staffMenu;
    }

    public void start() {
        selectShiftStaff();
        while (true) {
            System.out.println("\n================ QUẦY CÀ PHÊ POS ================");
            System.out.println("Nhân viên trực ca: " + onShift.getName() + " (" + onShift.getRole() + ")");
            System.out.println("1. Bán hàng / Lập hóa đơn");
            System.out.println("2. Quản lý & Hủy hóa đơn");
            System.out.println("3. Quản lý Danh mục & Sản phẩm");
            System.out.println("4. Quản lý Nhân viên & Đổi ca trực");
            System.out.println("5. Thống kê doanh thu");
            System.out.println("0. Thoát");

            int choice = Input.integer("Chọn chức năng: ", 0, 5);
            switch (choice) {
                case 1 -> createInvoiceFlow();
                case 2 -> invoiceMenu();
                case 3 -> {
                    if (catalogMenu != null) {
                        catalogMenu.show();
                    }
                }
                case 4 -> {
                    if (staffMenu != null) {
                        staffMenu.show(this::selectShiftStaff);
                    } else {
                        selectShiftStaff();
                    }
                }
                case 5 -> System.out.println("Chức năng Thống kê doanh thu (FR-10) đang chờ Bình hoàn thiện và ghép nối.");
                case 0 -> {
                    System.out.println("Đã thoát chương trình.");
                    return;
                }
            }
        }
    }

    public void selectShiftStaff() {
        List<Staff> staffs = staffRepo.findAll();
        if (staffs.isEmpty()) {
            System.out.println("Chưa có dữ liệu nhân viên!");
            return;
        }
        System.out.println("\n=== CHỌN NHÂN VIÊN TRỰC CA ===");
        for (int i = 0; i < staffs.size(); i++) {
            System.out.println((i + 1) + ". " + staffs.get(i));
        }
        int idx = Input.integer("Chọn nhân viên (1-" + staffs.size() + "): ", 1, staffs.size());
        onShift = staffs.get(idx - 1);
    }

    private void createInvoiceFlow() {
        try {
            Map<String, Integer> cart = new LinkedHashMap<>();
            System.out.println("\n=== LẬP HÓA ĐƠN MỚI ===");

            while (true) {
                String code = Input.optional("Nhập mã sản phẩm (Enter trống để kết thúc chọn món): ");
                if (code == null) {
                    break;
                }

                Product p;
                try {
                    p = sales.getProduct(code);
                } catch (NotFoundException e) {
                    System.out.println("Lỗi: " + e.getMessage());
                    continue;
                }

                if (p.getStatus() == ProductStatus.STOPPED) {
                    System.out.println("Lỗi: Sản phẩm này đã ngừng bán!");
                    continue;
                }

                int qty = Input.integer("Nhập số lượng: ", 1, 1000);
                int oldQty = cart.getOrDefault(p.getId(), 0);
                int newQty = oldQty + qty;

                String warn = sales.warning(p, newQty);
                if (warn != null) {
                    System.out.println("Lỗi: " + warn);
                    continue;
                }

                cart.put(p.getId(), newQty);
                printCartPreview(cart);
            }

            if (cart.isEmpty()) {
                System.out.println("Giỏ hàng trống, đã hủy lập hóa đơn.");
                return;
            }

            PaymentMethod method = Input.choose("Chọn phương thức thanh toán:", PaymentMethod.values());
            int discount = Input.integer("Nhập phần trăm giảm giá (0-50%): ", 0, 50);

            double subtotal = calculateSubtotal(cart);
            double finalTotal = subtotal * (100 - discount) / 100.0;
            System.out.printf("Tạm tính: %,.0f đ | Giảm giá: %d%% | TỔNG THANH TOÁN: %,.0f đ%n",
                    subtotal, discount, finalTotal);

            if (!Input.confirm("Xác nhận lập hóa đơn và thu tiền?")) {
                System.out.println("Đã hủy giao dịch.");
                return;
            }

            Invoice created = sales.checkout(onShift.getId(), cart, method, discount);
            System.out.printf("Lập hóa đơn %s thành công! Tổng tiền: %,.0f đ%n",
                    created.getId(), created.total());

        } catch (BusinessException | NotFoundException | IllegalArgumentException e) {
            System.out.println("LỖI: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("LỖI HỆ THỐNG: " + e.getMessage());
        }
    }

    private void printCartPreview(Map<String, Integer> cart) throws NotFoundException {
        System.out.println("--- Giỏ hàng hiện tại ---");
        double subtotal = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product p = sales.getProduct(entry.getKey());
            int qty = entry.getValue();
            double amount = qty * p.getPrice();
            subtotal += amount;
            System.out.printf("  %s | %-20s | SL: %d | Đơn giá: %,.0f đ | Thành tiền: %,.0f đ%n",
                    p.getId(), p.getName(), qty, p.getPrice(), amount);
        }
        System.out.printf("Tạm tính: %,.0f đ%n", subtotal);
    }

    private double calculateSubtotal(Map<String, Integer> cart) throws NotFoundException {
        double sum = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product p = sales.getProduct(entry.getKey());
            sum += entry.getValue() * p.getPrice();
        }
        return sum;
    }

    private void invoiceMenu() {
        while (true) {
            System.out.println("\n--- QUẢN LÝ HÓA ĐƠN ---");
            System.out.println("1. Xem chi tiết hóa đơn theo mã");
            System.out.println("2. Hủy hóa đơn đã thanh toán");
            System.out.println("3. Tra cứu & lọc hóa đơn phân trang");
            System.out.println("0. Quay lại");

            int choice = Input.integer("Chọn: ", 0, 3);
            switch (choice) {
                case 1 -> viewInvoiceByIdFlow();
                case 2 -> cancelInvoiceFlow();
                case 3 -> searchInvoicesFlow();
                case 0 -> {
                    return;
                }
            }
        }
    }

    private void viewInvoiceByIdFlow() {
        try {
            String id = Input.required("Nhập mã hóa đơn (VD: HD20261125-0001): ");
            Invoice inv = sales.get(id);
            printInvoiceDetails(inv);
        } catch (NotFoundException e) {
            System.out.println("LỖI: " + e.getMessage());
        }
    }

    private void cancelInvoiceFlow() {
        try {
            String id = Input.required("Nhập mã hóa đơn cần hủy (VD: HD20261125-0001): ");
            Invoice inv = sales.get(id);
            printInvoiceDetails(inv);

            if (Input.confirm("Xác nhận hủy hóa đơn này và cộng trả tồn kho?")) {
                sales.cancel(inv.getId());
                System.out.println("Đã hủy hóa đơn " + inv.getId() + " và hoàn trả tồn kho thành công!");
            }
        } catch (BusinessException | NotFoundException e) {
            System.out.println("LỖI: " + e.getMessage());
        }
    }

    private void searchInvoicesFlow() {
        try {
            System.out.println("\n=== TRA CỨU HÓA ĐƠN (Enter để bỏ qua tiêu chí) ===");
            LocalDate from = Input.optionalDate("Từ ngày (dd/MM/yyyy): ");
            LocalDate to = Input.optionalDate("Đến ngày (dd/MM/yyyy): ");
            String staffId = Input.optional("Mã nhân viên (VD: NV01): ");
            InvoiceStatus status = Input.chooseOptional("Lọc theo trạng thái:", InvoiceStatus.values());
            PaymentMethod method = Input.chooseOptional("Lọc theo phương thức TT:", PaymentMethod.values());
            InvoiceSort sort = Input.choose("Sắp xếp theo:", InvoiceSort.values());

            InvoiceCriteria criteria = new InvoiceCriteria(from, to, staffId, status, method, sort);
            int page = 1;
            int size = 5;

            while (true) {
                Page<InvoiceSummary> result = sales.search(criteria, page, size);
                int totalPages = Math.max(1, result.totalPages());
                System.out.printf("\n--- KẾT QUẢ HÓA ĐƠN (Trang %d/%d - Tổng: %d đơn) ---%n",
                        result.page(), totalPages, result.totalItems());

                if (result.items().isEmpty()) {
                    System.out.println("(Không có hóa đơn nào khớp điều kiện)");
                    return;
                }

                for (InvoiceSummary s : result.items()) {
                    System.out.printf("%s | %s | NV: %-16s | %-12s | %-14s | %,.0f đ%n",
                            s.id(), s.createdAt().format(DT_FMT), s.staffName(),
                            s.method(), s.status(), s.total());
                }

                String cmd = Input.line("Lệnh [n: trang sau | p: trang trước | nhập mã HĐ xem chi tiết | Enter: thoát]: ");
                if (cmd.isEmpty()) {
                    break;
                } else if (cmd.equalsIgnoreCase("n")) {
                    if (page < result.totalPages()) {
                        page++;
                    } else {
                        System.out.println("Đang ở trang cuối cùng.");
                    }
                } else if (cmd.equalsIgnoreCase("p")) {
                    if (page > 1) {
                        page--;
                    } else {
                        System.out.println("Đang ở trang đầu tiên.");
                    }
                } else {
                    Invoice detailInv = sales.get(cmd);
                    printInvoiceDetails(detailInv);
                }
            }
        } catch (NotFoundException e) {
            System.out.println("LỖI: " + e.getMessage());
        }
    }

    private void printInvoiceDetails(Invoice inv) throws NotFoundException {
        System.out.println("\n==========================================");
        System.out.println("HÓA ĐƠN: " + inv.getId() + " | Ngày: " + inv.getCreatedAt().format(DT_FMT));
        System.out.println("Nhân viên: " + inv.getStaffId()
                + " | Thanh toán: " + inv.getPaymentMethod()
                + " | Trạng thái: " + inv.getStatus());
        System.out.println("------------------------------------------");
        for (InvoiceDetail d : inv.getDetails()) {
            Product p = sales.getProduct(d.getProductId());
            System.out.printf("  %-6s | %-20s | SL: %d | Giá lúc bán: %,.0f đ | Thành tiền: %,.0f đ%n",
                    d.getProductId(), p.getName(), d.getQuantity(), d.getUnitPrice(), d.amount());
        }
        System.out.println("------------------------------------------");
        System.out.printf("Tạm tính: %,.0f đ | Giảm giá: %d%% | TỔNG CỘNG: %,.0f đ%n",
                inv.subtotal(), inv.getDiscountPercent(), inv.total());
        System.out.println("==========================================");
    }
}
