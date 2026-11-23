package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Invoice implements Identifiable<String>, Serializable {
    private String id;
    private String staffId;
    private LocalDateTime createdAt;
    private PaymentMethod paymentMethod;
    private int discountPercent;
    private InvoiceStatus status;
    private List<InvoiceDetail> details = new ArrayList<>();

    public Invoice(String id, String staffId, LocalDateTime createdAt,
            PaymentMethod paymentMethod, int discountPercent, InvoiceStatus status) {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("Mã hóa đơn rỗng");
        if (staffId == null || staffId.trim().isEmpty())
            throw new IllegalArgumentException("Chưa chọn nhân viên");
        if (createdAt == null)
            throw new IllegalArgumentException("Thiếu thời gian lập");
        if (paymentMethod == null)
            throw new IllegalArgumentException("Chưa chọn phương thức thanh toán");
        if (status == null)
            throw new IllegalArgumentException("Thiếu trạng thái");

        this.id = id.trim();
        this.staffId = staffId.trim();
        this.createdAt = createdAt;
        this.paymentMethod = paymentMethod;
        setDiscountPercent(discountPercent);
        this.status = status;
    }

    public void setDiscountPercent(int discountPercent) {
        if (discountPercent < 0 || discountPercent > 50) {
            throw new IllegalArgumentException("Giảm giá phải từ 0 đến 50%");
        }
        this.discountPercent = discountPercent;
    }

    // Thêm món vào hóa đơn, trùng mã thì cộng dồn số lượng
    public void addItem(String productId, int quantity, double unitPrice) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã sản phẩm");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1");
        }
        String cleanId = productId.trim().toUpperCase();
        for (InvoiceDetail item : details) {
            if (item.getProductId().equals(cleanId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        details.add(new InvoiceDetail(cleanId, quantity, unitPrice));
    }

    public double subtotal() {
        double sum = 0;
        for (InvoiceDetail item : details) {
            sum += item.amount();
        }
        return sum;
    }

    public double total() {
        return subtotal() * (100 - discountPercent) / 100.0;
    }

    public String getId() {
        return id;
    }

    public String getStaffId() {
        return staffId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public List<InvoiceDetail> getDetails() {
        return details;
    }
}