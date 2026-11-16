// src/model/PaymentMethod.java
package model;

public enum PaymentMethod {
    CASH("Tiền mặt"),
    CARD("Thẻ"),
    E_WALLET("Ví điện tử");

    private final String label;
    PaymentMethod(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}