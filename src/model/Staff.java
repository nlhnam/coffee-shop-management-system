package model;

import java.io.Serializable;

public class Staff implements Identifiable<String>, Serializable {
    private String id;
    private String name;
    private String phone;
    private StaffRole role;

    public Staff(String id, String name, String phone, StaffRole role) {
        setId(id);
        setName(name);
        setPhone(phone);
        setRole(role);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public StaffRole getRole() {
        return role;
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma nhan vien khong duoc de trong");
        }
        this.id = id.trim();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ho ten khong duoc de trong");
        }
        this.name = name.trim();
    }

    // BR-02: đúng 10 chữ số và bắt đầu bằng số 0
    public void setPhone(String phone) {
        if (phone == null || !phone.trim().matches("0\\d{9}")) {
            throw new IllegalArgumentException("So dien thoai phai gom dung 10 chu so va bat dau bang 0");
        }
        this.phone = phone.trim();
    }

    public void setRole(StaffRole role) {
        if (role == null) {
        throw new IllegalArgumentException("Vai tro khong duoc de trong");
        }
        this.role = role;
    }

    @Override
    public String toString() {
        return "Staff{id='" + id + "', name='" + name + "', phone='" + phone + "', role=" + role + "}";
    }
       
}
