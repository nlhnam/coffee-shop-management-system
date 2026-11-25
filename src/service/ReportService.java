package service;

import model.Category;
import model.Criteria.DailyRevenue;
import model.Criteria.ProductSales;
import model.Invoice;
import model.InvoiceDetail;
import model.InvoiceStatus;
import model.PaymentMethod;
import model.Product;
import model.Staff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReportService {

    private final List<Invoice> invoices;
    private final Map<String, Product> productsById;
    private final Map<String, Category> categoriesById;
    private final Map<String, Staff> staffById;

    public record PaymentMethodStat(
            PaymentMethod method,
            long invoiceCount,
            double percentage
    ) {}

    public record PeakHourStat(
            int hour,
            long invoiceCount
    ) {}

    public ReportService(
            List<Invoice> invoices,
            Map<String, Product> productsById,
            Map<String, Category> categoriesById,
            Map<String, Staff> staffById
    ) {
        this.invoices = invoices == null ? List.of() : List.copyOf(invoices);
        this.productsById = productsById == null ? Map.of() : Map.copyOf(productsById);
        this.categoriesById = categoriesById == null ? Map.of() : Map.copyOf(categoriesById);
        this.staffById = staffById == null ? Map.of() : Map.copyOf(staffById);
    }

    private List<Invoice> paidInvoices() {
        return invoices.stream()
                .filter(inv -> inv != null && inv.getStatus() == InvoiceStatus.PAID)
                .toList();
    }

    // TK-01: Doanh thu từng ngày trong n ngày gần nhất (ngày không có đơn vẫn = 0)
    public List<DailyRevenue> revenueLastDays(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Số ngày phải >= 1");
        }
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(n - 1L);

        Map<LocalDate, List<Invoice>> paidByDate = paidInvoices().stream()
                .filter(inv -> inv.getCreatedAt() != null)
                .map(inv -> Map.entry(inv.getCreatedAt().toLocalDate(), inv))
                .filter(entry -> !entry.getKey().isBefore(startDate) && !entry.getKey().isAfter(today))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        return IntStream.range(0, n)
                .mapToObj(i -> startDate.plusDays(i))
                .map(date -> {
                    List<Invoice> dayInvoices = paidByDate.getOrDefault(date, List.of());
                    double revenue = dayInvoices.stream()
                            .mapToDouble(Invoice::total)
                            .sum();
                    return new DailyRevenue(date, dayInvoices.size(), revenue);
                })
                .toList();
    }

    // TK-02: Top n món bán chạy nhất theo tổng số lượng (dùng flatMap + groupingBy)
    public List<ProductSales> topProducts(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Số lượng top phải >= 1");
        }

        Map<String, Long> quantityByProductId = paidInvoices().stream()
                .filter(inv -> inv.getDetails() != null)
                .flatMap(inv -> inv.getDetails().stream())
                .filter(d -> d != null)
                .collect(Collectors.groupingBy(
                        InvoiceDetail::getProductId,
                        Collectors.summingLong(InvoiceDetail::getQuantity)
                ));

        Map<String, Double> revenueByProductId = paidInvoices().stream()
                .filter(inv -> inv.getDetails() != null)
                .flatMap(inv -> inv.getDetails().stream())
                .filter(d -> d != null)
                .collect(Collectors.groupingBy(
                        InvoiceDetail::getProductId,
                        Collectors.summingDouble(InvoiceDetail::amount)
                ));

        return quantityByProductId.entrySet().stream()
                .map(entry -> {
                    String prodId = entry.getKey();
                    long qty = entry.getValue();
                    double rev = revenueByProductId.getOrDefault(prodId, 0.0);
                    Product prod = productsById.get(prodId);
                    String name = (prod != null) ? prod.getName() : prodId;
                    return new ProductSales(prodId, name, qty, rev);
                })
                .sorted(Comparator.comparingLong(ProductSales::quantitySold).reversed()
                        .thenComparing(Comparator.comparingDouble(ProductSales::revenue).reversed()))
                .limit(n)
                .toList();
    }

    // TK-03: Map tên nhân viên -> tổng doanh thu
    public Map<String, Double> revenueByStaff() {
        return paidInvoices().stream()
                .collect(Collectors.groupingBy(
                        inv -> {
                            Staff s = staffById.get(inv.getStaffId());
                            return (s != null) ? s.getName() : inv.getStaffId();
                        },
                        Collectors.summingDouble(Invoice::total)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // TK-04: Số lượng và tỉ lệ (%) hóa đơn theo từng phương thức thanh toán
    public Map<PaymentMethod, PaymentMethodStat> countByPaymentMethod() {
        List<Invoice> paid = paidInvoices();
        long totalCount = paid.size();

        Map<PaymentMethod, Long> counts = paid.stream()
                .filter(inv -> inv.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(
                        Invoice::getPaymentMethod,
                        Collectors.counting()
                ));

        return java.util.Arrays.stream(PaymentMethod.values())
                .collect(Collectors.toMap(
                        method -> method,
                        method -> {
                            long count = counts.getOrDefault(method, 0L);
                            double pct = (totalCount == 0) ? 0.0 : (count * 100.0) / totalCount;
                            return new PaymentMethodStat(method, count, pct);
                        },
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // TK-05: Khung giờ (0-23) có nhiều hóa đơn nhất
    public Optional<PeakHourStat> peakHour() {
        Map<Integer, Long> countByHour = paidInvoices().stream()
                .filter(inv -> inv.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        inv -> inv.getCreatedAt().getHour(),
                        Collectors.counting()
                ));

        return countByHour.entrySet().stream()
                .max(Map.Entry.<Integer, Long>comparingByValue()
                        .thenComparing(Map.Entry.<Integer, Long>comparingByKey().reversed()))
                .map(e -> new PeakHourStat(e.getKey(), e.getValue()));
    }

    // TK-06: Doanh thu trước giảm giá theo tên danh mục
    public Map<String, Double> revenueByCategory() {
        return paidInvoices().stream()
                .filter(inv -> inv.getDetails() != null)
                .flatMap(inv -> inv.getDetails().stream())
                .filter(d -> d != null)
                .collect(Collectors.groupingBy(
                        d -> {
                            Product p = productsById.get(d.getProductId());
                            if (p == null) {
                                return "Không xác định";
                            }
                            Category c = categoriesById.get(p.getCategoryId());
                            return (c != null) ? c.getName() : p.getCategoryId();
                        },
                        Collectors.summingDouble(InvoiceDetail::amount)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    // TK-07a: Danh sách sản phẩm sắp hết hàng (stock < threshold)
    public List<Product> lowStock(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn kho không được âm");
        }
        return productsById.values().stream()
                .filter(p -> p != null && p.getStock() < threshold)
                .sorted(Comparator.comparingInt(Product::getStock)
                        .thenComparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // TK-07b: Danh sách sản phẩm không bán được trong n ngày qua
    public List<Product> notSoldInDays(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Số ngày phải >= 1");
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(n);

        Set<String> soldProductIds = paidInvoices().stream()
                .filter(inv -> inv.getCreatedAt() != null && !inv.getCreatedAt().isBefore(cutoff))
                .filter(inv -> inv.getDetails() != null)
                .flatMap(inv -> inv.getDetails().stream())
                .filter(d -> d != null)
                .map(InvoiceDetail::getProductId)
                .collect(Collectors.toSet());

        return productsById.values().stream()
                .filter(p -> p != null && !soldProductIds.contains(p.getId()))
                .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}