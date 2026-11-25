package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Locale;

public final class Criteria {

    private Criteria() {
    }

    // ==================== TIÊU CHÍ TÌM KIẾM ====================

    public record ProductCriteria(
            String keyword,
            String categoryId,
            Double minPrice,
            Double maxPrice,
            ProductStatus status,
            boolean lowStockOnly
    ) {
        public boolean matchesKeyword(String productName, String productId) {
            if (keyword == null || keyword.isBlank()) {
                return true;
            }
            String key = keyword.trim().toLowerCase(Locale.ROOT);
            boolean matchName = productName != null && productName.toLowerCase(Locale.ROOT).contains(key);
            boolean matchId = productId != null && productId.toLowerCase(Locale.ROOT).contains(key);
            return matchName || matchId;
        }
    }

    public record InvoiceCriteria(
            LocalDate from,
            LocalDate to,
            String staffId,
            InvoiceStatus status,
            PaymentMethod method,
            InvoiceSort sort
    ) {}

    // ==================== DTO TÓM TẮT & THỐNG KÊ ====================

    public record InvoiceSummary(
            String id,
            LocalDateTime createdAt,
            String staffName,
            PaymentMethod method,
            InvoiceStatus status,
            double total
    ) {}

    public record DailyRevenue(
            LocalDate date,
            long invoiceCount,
            double revenue
    ) {}

    public record ProductSales(
            String productId,
            String productName,
            long quantitySold,
            double revenue
    ) {}

    // ==================== ENUM SẮP XẾP ====================

    public enum ProductSort {
        ID_ASC("Mã SP (A-Z)", Comparator.comparing(Product::getId, String.CASE_INSENSITIVE_ORDER)),
        NAME_ASC("Tên sản phẩm (A-Z)", Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER)),
        PRICE_ASC("Giá tăng dần", Comparator.comparingDouble(Product::getPrice)),
        PRICE_DESC("Giá giảm dần", Comparator.comparingDouble(Product::getPrice).reversed()),
        STOCK_ASC("Tồn kho tăng dần", Comparator.comparingInt(Product::getStock));

        private final String label;
        private final Comparator<Product> comparator;

        ProductSort(String label, Comparator<Product> comparator) {
            this.label = label;
            this.comparator = comparator;
        }

        public String getLabel() {
            return label;
        }

        public Comparator<Product> comparator() {
            return comparator;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum InvoiceSort {
        DATE_DESC("Mới nhất trước"),
        DATE_ASC("Cũ nhất trước"),
        TOTAL_DESC("Tổng tiền cao đến thấp"),
        TOTAL_ASC("Tổng tiền thấp đến cao");

        private final String label;

        InvoiceSort(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
