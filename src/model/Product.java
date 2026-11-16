// src/model/Product.java
package model;

public class Product {
    private String id;
    private String name;
    private String categoryId;
    private double price;
    private int stock;
    private ProductStatus status;

    public Product(String id, String name, String categoryId, double price, int stock, ProductStatus status) {
        setId(id);
        setName(name);
        setCategoryId(categoryId);
        setPrice(price);
        setStock(stock);
        setStatus(status);
    }

    public String getId() { return id; }
    public void setId(String id) {
        if (id == null || !id.trim().toUpperCase().startsWith("SP") || id.trim().length() < 4) {
            throw new IllegalArgumentException("Mã sản phẩm phải dạng SP01");
        }
        this.id = id.trim().toUpperCase();
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được rỗng");
        }
        this.name = name.trim();
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn danh mục");
        }
        this.categoryId = categoryId.trim().toUpperCase();
    }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0");
        }
        this.price = price;
    }

    public int getStock() { return stock; }
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Tồn kho không được âm");
        }
        this.stock = stock;
    }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Chưa chọn trạng thái");
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%s | %-22s | %s | %,.0f đ | Tồn: %d | %s",
                id, name, categoryId, price, stock, status);
    }
}