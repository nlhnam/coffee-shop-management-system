package model;

import java.io.Serializable;

public class InvoiceDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String productId;
    private int quantity;
    private final double unitPrice;

    public InvoiceDetail(String productId, int quantity, double unitPrice) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã sản phẩm");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải >= 1");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Đơn giá phải > 0");
        }

        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }


    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }


    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải >= 1");
        }
        this.quantity = quantity;
    }

    public double amount() {
        return this.quantity * this.unitPrice;
    }
}