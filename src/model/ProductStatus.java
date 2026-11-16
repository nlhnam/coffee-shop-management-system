// src/model/ProductStatus.java
package model;

public enum ProductStatus {
    SELLING("Đang bán"),
    STOPPED("Ngừng bán");

    private final String label;
    ProductStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}