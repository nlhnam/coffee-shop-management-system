package service;

import java.util.List;
import java.util.Optional;

import exception.BusinessException;
import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Staff;
import repository.Repositories.InvoiceRepository;
import repository.Repositories.StaffRepository;

public class StaffService {
    private final StaffRepository staffRepo;
    private final InvoiceRepository invoiceRepo;

    public StaffService(StaffRepository staffRepo, InvoiceRepository invoiceRepo) {
        this.staffRepo = staffRepo;
        this.invoiceRepo = invoiceRepo;
    }

    public List<Staff> getAllStaff() {
        return staffRepo.findAll();
    }

    public Optional<Staff> findStaffById(String id) {
        return staffRepo.findById(id);
    }

    public void addStaff(Staff s) throws DuplicateIdException {
        staffRepo.insert(s);
    }

    public void updateStaff(Staff s) throws NotFoundException {
        staffRepo.update(s);
    }

    public void deleteStaff(String id) throws NotFoundException, BusinessException {
        if (invoiceRepo.existsByStaff(id)) {
            throw new BusinessException("Không thể xóa nhân viên đã lập hóa đơn!");
        }
        staffRepo.deleteById(id);
    }
}
