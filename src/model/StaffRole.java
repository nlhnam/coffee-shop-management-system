// src/model/StaffRole.java
package model;

public enum StaffRole {
    CASHIER("Thu ngân"),
    MANAGER("Quản lý");

    private final String label;
    StaffRole(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
