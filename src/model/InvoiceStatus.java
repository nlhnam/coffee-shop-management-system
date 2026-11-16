// src/model/InvoiceStatus.java
package model;

public enum InvoiceStatus {
    PAID("Đã thanh toán"),
    CANCELLED("Đã hủy");

    private final String label;
    InvoiceStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}