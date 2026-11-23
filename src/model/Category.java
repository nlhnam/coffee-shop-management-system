// src/model/Category.java
package model;

import java.io.Serializable;

public class Category implements Identifiable<String>, Serializable {
    private String id;
    private String name;

    public Category(String id, String name) {
        setId(id);
        setName(name);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        if (id == null || !id.trim().toUpperCase().startsWith("DM") || id.trim().length() < 4) {
            throw new IllegalArgumentException("Mã danh mục phải dạng DM01");
        }
        this.id = id.trim().toUpperCase();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được rỗng");
        }
        this.name = name.trim();
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
