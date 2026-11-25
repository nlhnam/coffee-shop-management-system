package ui;

import model.Criteria.DailyRevenue;
import model.Criteria.ProductSales;
import model.Page;
import model.Product;
import service.ReportService;
import service.ReportService.PaymentMethodStat;
import service.ReportService.PeakHourStat;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReportMenu {

    private static final NumberFormat CURRENCY_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReportService reportService;
    private final Scanner scanner;

    public ReportMenu(ReportService reportService, Scanner scanner) {
        this.reportService = reportService;
        this.scanner = scanner;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=== BÁO CÁO & THỐNG KÊ (STREAM API) ===");
            System.out.println("1. TK-01: Doanh thu n ngày gần nhất");
            System.out.println("2. TK-02: Top n sản phẩm bán chạy nhất");
            System.out.println("3. TK-03: Doanh thu theo nhân viên");
            System.out.println("4. TK-04: Thống kê theo phương thức thanh toán");
            System.out.println("5. TK-05: Khung giờ cao điểm");
            System.out.println("6. TK-06: Doanh thu trước giảm giá theo danh mục");
            System.out.println("7. TK-07: Sản phẩm sắp hết hàng & không bán được trong n ngày");
            System.out.println("0. Quay lại");
            System.out.print("Chọn chức năng: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> printRevenueLastDays();
                case "2" -> printTopProducts();
                case "3" -> printRevenueByStaff();
                case "4" -> printPaymentMethodStats();
                case "5" -> printPeakHour();
                case "6" -> printRevenueByCategory();
                case "7" -> printLowStockAndNotSold();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Lựa chọn không hợp lệ, vui lòng nhập lại.");
            }
        }
    }

    /**
     * Tiện ích phân trang dùng chung:
     * Hiển thị "Trang x/y", hỗ trợ nhập 'n' (next), 'p' (prev), số trang bất kỳ, hoặc 'q' (thoát).
     */
    public static <T> void pager(
            Scanner scanner,
            String title,
            Function<Integer, Page<T>> pageLoader,
            Function<T, String> rowFormatter
    ) {
        int currentPage = 1;
        while (true) {
            Page<T> page = pageLoader.apply(currentPage);
            currentPage = page.page();

            System.out.println("\n--- " + title + " (Trang " + currentPage + "/" + page.totalPages()
                    + " | Tổng: " + page.totalItems() + " mục) ---");
            if (page.items().isEmpty()) {
                System.out.println("(Không có dữ liệu)");
            } else {
                page.items().forEach(item -> System.out.println(rowFormatter.apply(item)));
            }

            System.out.print("[n] Trang sau | [p] Trang trước | [số trang] Nhảy trang | [q] Thoát: ");
            String cmd = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

            if (cmd.equals("q")) {
                return;
            } else if (cmd.equals("n")) {
                if (page.hasNext()) {
                    currentPage++;
                } else {
                    System.out.println("Đang ở trang cuối cùng.");
                }
            } else if (cmd.equals("p")) {
                if (page.hasPrevious()) {
                    currentPage--;
                } else {
                    System.out.println("Đang ở trang đầu tiên.");
                }
            } else {
                try {
                    int target = Integer.parseInt(cmd);
                    if (target < 1 || target > page.totalPages()) {
                        System.out.println("Số trang phải từ 1 đến " + page.totalPages() + ".");
                    } else {
                        currentPage = target;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Lệnh không hợp lệ, vui lòng nhập n, p, số trang hoặc q.");
                }
            }
        }
    }

    private int readPositiveInt(String prompt, int defaultValue) {
        System.out.print(prompt + " (mặc định " + defaultValue + "): ");
        String raw = scanner.nextLine().trim();
        if (raw.isEmpty()) {
            return defaultValue;
        }
        try {
            int val = Integer.parseInt(raw);
            if (val < 1) {
                System.out.println("Giá trị phải >= 1, dùng mặc định " + defaultValue + ".");
                return defaultValue;
            }
            return val;
        } catch (NumberFormatException e) {
            System.out.println("Định dạng số không hợp lệ, dùng mặc định " + defaultValue + ".");
            return defaultValue;
        }
    }

    private void printRevenueLastDays() {
        int days = readPositiveInt("Nhập số ngày gần nhất", 7);
        List<DailyRevenue> list = reportService.revenueLastDays(days);
        pager(
                scanner,
                "TK-01: DOANH THU " + days + " NGÀY GẦN NHẤT",
                p -> Page.of(list, p, 7),
                r -> String.format("Ngày: %s | Số đơn PAID: %d | Doanh thu: %s đ",
                        r.date().format(DATE_FMT), r.invoiceCount(), CURRENCY_FMT.format(r.revenue()))
        );
    }

    private void printTopProducts() {
        int topN = readPositiveInt("Nhập số lượng top sản phẩm", 5);
        List<ProductSales> list = reportService.topProducts(topN);
        System.out.println("\n--- TK-02: TOP " + topN + " SẢN PHẨM BÁN CHẠY ---");
        if (list.isEmpty()) {
            System.out.println("(Chưa có dữ liệu bán hàng)");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            ProductSales item = list.get(i);
            System.out.printf("#%d [%s] %s - SL bán: %d - Doanh thu: %s đ%n",
                    i + 1, item.productId(), item.productName(), item.quantitySold(),
                    CURRENCY_FMT.format(item.revenue()));
        }
    }

    private void printRevenueByStaff() {
        Map<String, Double> map = reportService.revenueByStaff();
        List<Map.Entry<String, Double>> rows = List.copyOf(map.entrySet());
        pager(
                scanner,
                "TK-03: DOANH THU THEO NHÂN VIÊN",
                p -> Page.of(rows, p, 5),
                e -> String.format("- %s: %s đ", e.getKey(), CURRENCY_FMT.format(e.getValue()))
        );
    }

    private void printPaymentMethodStats() {
        List<PaymentMethodStat> rows = List.copyOf(reportService.countByPaymentMethod().values());
        System.out.println("\n--- TK-04: PHƯƠNG THỨC THANH TOÁN ---");
        rows.forEach(s -> System.out.printf("- %s: %d hóa đơn (%.1f%%)%n",
                s.method(), s.invoiceCount(), s.percentage()));
    }

    private void printPeakHour() {
        System.out.println("\n--- TK-05: KHUNG GIỜ CAO ĐIỂM ---");
        reportService.peakHour().ifPresentOrElse(
                (PeakHourStat p) -> System.out.printf("Khung giờ cao điểm: %02d:00 - %02d:59 (%d hóa đơn PAID)%n",
                        p.hour(), p.hour(), p.invoiceCount()),
                () -> System.out.println("(Chưa có hóa đơn PAID)")
        );
    }

    private void printRevenueByCategory() {
        List<Map.Entry<String, Double>> rows = List.copyOf(reportService.revenueByCategory().entrySet());
        pager(
                scanner,
                "TK-06: DOANH THU TRƯỚC GIẢM GIÁ THEO DANH MỤC",
                p -> Page.of(rows, p, 5),
                e -> String.format("- %s: %s đ", e.getKey(), CURRENCY_FMT.format(e.getValue()))
        );
    }

    private void printLowStockAndNotSold() {
        int threshold = readPositiveInt("Nhập ngưỡng tồn kho cảnh báo", 10);
        int days = readPositiveInt("Nhập số ngày không bán được", 7);

        List<Product> low = reportService.lowStock(threshold);
        List<Product> notSold = reportService.notSoldInDays(days);

        pager(
                scanner,
                "TK-07A: SẢN PHẨM TỒN KHO < " + threshold,
                p -> Page.of(low, p, 5),
                prod -> String.format("[%s] %s - Tồn kho: %d",
                        prod.getId(), prod.getName(), prod.getStock())
        );

        pager(
                scanner,
                "TK-07B: SẢN PHẨM KHÔNG BÁN ĐƯỢC TRONG " + days + " NGÀY",
                p -> Page.of(notSold, p, 5),
                prod -> String.format("[%s] %s - Tồn kho: %d",
                        prod.getId(), prod.getName(), prod.getStock())
        );
    }
}