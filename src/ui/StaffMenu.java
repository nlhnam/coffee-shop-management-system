package ui;

import java.util.List;

import exception.BusinessException;
import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Staff;
import model.StaffRole;
import service.StaffService;

public class StaffMenu {
    private final StaffService staffService;

    public StaffMenu(StaffService staffService) {
        this.staffService = staffService;
    }

    public void show(Runnable onChangeShift) {
        while (true) {
            System.out.println("\n========== QUẢN LÝ NHÂN VIÊN ==========");
            System.out.println("1. Xem danh sách nhân viên");
            System.out.println("2. Thêm nhân viên mới");
            System.out.println("3. Cập nhật thông tin nhân viên");
            System.out.println("4. Xóa nhân viên");
            System.out.println("5. Đổi nhân viên trực ca");
            System.out.println("0. Quay lại");

            int choice = Input.integer("Chọn chức năng: ", 0, 5);
            try {
                switch (choice) {
                    case 1 -> showAll();
                    case 2 -> addStaff();
                    case 3 -> updateStaff();
                    case 4 -> deleteStaff();
                    case 5 -> {
                        if (onChangeShift != null) {
                            onChangeShift.run();
                        }
                    }
                    case 0 -> {
                        return;
                    }
                }
            } catch (DuplicateIdException | NotFoundException | BusinessException | IllegalArgumentException e) {
                System.out.println("Lỗi: " + e.getMessage());
            }
        }
    }

    private void showAll() {
        List<Staff> list = staffService.getAllStaff();
        System.out.println("\n--- DANH SÁCH NHÂN VIÊN ---");
        if (list.isEmpty()) {
            System.out.println("Chưa có nhân viên.");
            return;
        }
        for (Staff s : list) {
            System.out.println(s);
        }
    }

    private void addStaff() throws DuplicateIdException {
        System.out.println("\n--- THÊM NHÂN VIÊN ---");
        String id = Input.required("Mã nhân viên (VD: NV05): ");
        String name = Input.required("Họ tên: ");
        String phone = Input.required("Số điện thoại (10 số, bắt đầu bằng 0): ");
        StaffRole role = Input.choose("Chọn vai trò:", StaffRole.values());

        Staff s = new Staff(id, name, phone, role);
        staffService.addStaff(s);
        System.out.println("Thêm nhân viên thành công.");
    }

    private void updateStaff() throws NotFoundException {
        System.out.println("\n--- CẬP NHẬT NHÂN VIÊN ---");
        String id = Input.required("Nhập mã nhân viên cần sửa: ");
        Staff old = staffService.findStaffById(id)
                .orElseThrow(() -> new NotFoundException("nhân viên", id));

        System.out.println("Thông tin hiện tại: " + old);
        String name = Input.required("Họ tên mới: ");
        String phone = Input.required("Số điện thoại mới: ");
        StaffRole role = Input.choose("Chọn vai trò mới:", StaffRole.values());

        Staff updated = new Staff(old.getId(), name, phone, role);
        staffService.updateStaff(updated);
        System.out.println("Cập nhật nhân viên thành công.");
    }

    private void deleteStaff() throws NotFoundException, BusinessException {
        System.out.println("\n--- XÓA NHÂN VIÊN ---");
        String id = Input.required("Nhập mã nhân viên cần xóa: ");
        Staff s = staffService.findStaffById(id)
                .orElseThrow(() -> new NotFoundException("nhân viên", id));

        System.out.println("Bạn đang chọn: " + s);
        if (Input.confirm("Xác nhận xóa nhân viên này?")) {
            staffService.deleteStaff(s.getId());
            System.out.println("Xóa nhân viên thành công.");
        }
    }
}
