package repository.memory;

import exception.BusinessException;
import exception.NotFoundException;
import model.Invoice;
import model.InvoiceDetail;
import model.InvoiceStatus;
import model.Product;
import model.ProductStatus;
import repository.Repositories.InvoiceRepository;
import repository.Repositories.ProductRepository;
import repository.Repositories.StaffRepository;

// [CHỜ BÌNH] Mở lại các dòng dưới đây khi mở hàm search():
// import model.Criteria.InvoiceCriteria;
// import model.Criteria.InvoiceSort;
// import model.Criteria.InvoiceSummary;
// import model.Page;
// import model.Staff;
// import java.util.Comparator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryInvoiceRepository implements InvoiceRepository {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final Map<String, Invoice> data = new LinkedHashMap<>();
    private final ProductRepository productRepo;
    private final StaffRepository staffRepo;

    public InMemoryInvoiceRepository(ProductRepository productRepo, StaffRepository staffRepo) {
        this.productRepo = productRepo;
        this.staffRepo = staffRepo;
    }

    @Override
    public List<Invoice> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Optional<Invoice> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        Invoice inv = data.get(id.trim().toUpperCase());
        return Optional.ofNullable(inv);
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return data.containsKey(id.trim().toUpperCase());
    }

    @Override
    public void insert(Invoice entity) {
        data.put(entity.getId(), entity);
    }

    @Override
    public boolean update(Invoice entity) {
        if (!data.containsKey(entity.getId())) {
            return false;
        }
        data.put(entity.getId(), entity);
        return true;
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }
        return data.remove(id.trim().toUpperCase()) != null;
    }

    @Override
    public void create(Invoice invoice) throws BusinessException, NotFoundException {
        if (invoice.getDetails().isEmpty()) {
            throw new BusinessException("Hóa đơn chưa có món nào");
        }
        if (existsById(invoice.getId())) {
            throw new BusinessException("Mã hóa đơn " + invoice.getId() + " đã tồn tại");
        }

        // Bước 1: Kiểm tra tất cả món trước, nếu có 1 món lỗi thì dừng ngay (chưa trừ kho)
        for (InvoiceDetail item : invoice.getDetails()) {
            Optional<Product> opt = productRepo.findById(item.getProductId());
            if (opt.isEmpty()) {
                throw new NotFoundException("sản phẩm", item.getProductId());
            }

            Product p = opt.get();
            if (p.getStatus() == ProductStatus.STOPPED) {
                throw new BusinessException("Sản phẩm " + p.getName() + " đã ngừng bán");
            }
            if (p.getStock() < item.getQuantity()) {
                throw new BusinessException("Không đủ hàng: " + p.getName() + " chỉ còn " + p.getStock());
            }
        }

        // Bước 2: Tất cả đã hợp lệ -> trừ tồn kho từng món và lưu hóa đơn
        for (InvoiceDetail item : invoice.getDetails()) {
            Product p = productRepo.findById(item.getProductId()).get();
            p.setStock(p.getStock() - item.getQuantity());
            productRepo.update(p);
        }
        data.put(invoice.getId(), invoice);
    }

    @Override
    public void cancel(String id) throws BusinessException, NotFoundException {
        Optional<Invoice> opt = findById(id);
        if (opt.isEmpty()) {
            throw new NotFoundException("hóa đơn", id);
        }

        Invoice invoice = opt.get();
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Hóa đơn " + invoice.getId() + " đã hủy trước đó");
        }

        // Cộng trả lại tồn kho cho từng món
        for (InvoiceDetail item : invoice.getDetails()) {
            Optional<Product> pOpt = productRepo.findById(item.getProductId());
            if (pOpt.isPresent()) {
                Product p = pOpt.get();
                p.setStock(p.getStock() + item.getQuantity());
                productRepo.update(p);
            }
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        update(invoice);
    }

    // [CHỜ BÌNH] Mở lại hàm search() sau khi Bình merge Page & Criteria:
    /*
    @Override
    public Page<InvoiceSummary> search(InvoiceCriteria criteria, int page, int size) {
        List<Invoice> matched = new ArrayList<>();

        for (Invoice inv : data.values()) {
            if (criteria != null) {
                if (criteria.from() != null && inv.getCreatedAt().toLocalDate().isBefore(criteria.from())) {
                    continue;
                }
                if (criteria.to() != null && inv.getCreatedAt().toLocalDate().isAfter(criteria.to())) {
                    continue;
                }
                if (criteria.staffId() != null && !inv.getStaffId().equalsIgnoreCase(criteria.staffId())) {
                    continue;
                }
                if (criteria.status() != null && inv.getStatus() != criteria.status()) {
                    continue;
                }
                if (criteria.method() != null && inv.getPaymentMethod() != criteria.method()) {
                    continue;
                }
            }
            matched.add(inv);
        }

        // Sắp xếp kết quả
        if (criteria != null && criteria.sort() == InvoiceSort.TOTAL_DESC) {
            matched.sort((a, b) -> Double.compare(b.total(), a.total()));
        } else {
            matched.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }

        // Chuyển sang InvoiceSummary để hiển thị
        List<InvoiceSummary> summaries = new ArrayList<>();
        for (Invoice inv : matched) {
            String staffName = inv.getStaffId();
            Optional<Staff> staffOpt = staffRepo.findById(inv.getStaffId());
            if (staffOpt.isPresent()) {
                staffName = staffOpt.get().getName();
            }
            summaries.add(new InvoiceSummary(
                    inv.getId(),
                    inv.getCreatedAt(),
                    staffName,
                    inv.getPaymentMethod(),
                    inv.getStatus(),
                    inv.total()
            ));
        }

        return Page.of(summaries, page, size);
    }
    */

    @Override
    public boolean existsByProduct(String productId) {
        if (productId == null) {
            return false;
        }
        String code = productId.trim().toUpperCase();
        for (Invoice inv : data.values()) {
            for (InvoiceDetail item : inv.getDetails()) {
                if (item.getProductId().equalsIgnoreCase(code)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean existsByStaff(String staffId) {
        if (staffId == null) {
            return false;
        }
        String code = staffId.trim().toUpperCase();
        for (Invoice inv : data.values()) {
            if (inv.getStaffId().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String nextId(LocalDate date) {
        String prefix = "HD" + date.format(DATE_FMT) + "-";
        int count = 0;
        for (String id : data.keySet()) {
            if (id.startsWith(prefix)) {
                count++;
            }
        }
        int nextNumber = count + 1;
        String newId = String.format("%s%04d", prefix, nextNumber);
        while (data.containsKey(newId)) {
            nextNumber++;
            newId = String.format("%s%04d", prefix, nextNumber);
        }
        return newId;
    }
}