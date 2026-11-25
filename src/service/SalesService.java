package service;

import exception.BusinessException;
import exception.NotFoundException;
import model.Invoice;
import model.InvoiceStatus;
import model.PaymentMethod;
import model.Product;
import model.ProductStatus;
import repository.Repositories.InvoiceRepository;
import repository.Repositories.ProductRepository;
import repository.Repositories.StaffRepository;


import model.Criteria.InvoiceCriteria;
import model.Criteria.InvoiceSummary;
import model.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public class SalesService {
    private final InvoiceRepository invoiceRepo;
    private final ProductRepository productRepo;
    private final StaffRepository staffRepo;

    public SalesService(InvoiceRepository invoiceRepo, ProductRepository productRepo, StaffRepository staffRepo) {
        this.invoiceRepo = invoiceRepo;
        this.productRepo = productRepo;
        this.staffRepo = staffRepo;
    }

    public Product getProduct(String productId) throws NotFoundException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new NotFoundException("sản phẩm", productId);
        }
        Optional<Product> opt = productRepo.findById(productId.trim().toUpperCase());
        if (opt.isEmpty()) {
            throw new NotFoundException("sản phẩm", productId);
        }
        return opt.get();
    }

    public String warning(Product p, int quantityInCart) {
        if (p.getStatus() == ProductStatus.STOPPED) {
            return "Sản phẩm '" + p.getName() + "' đã ngừng bán!";
        }
        if (quantityInCart > p.getStock()) {
            return "Không đủ hàng (" + p.getName() + " chỉ còn " + p.getStock() + ")!";
        }
        return null;
    }

    public Invoice checkout(String staffId, Map<String, Integer> cart, PaymentMethod method, int discount)
            throws BusinessException, NotFoundException {
        if (cart == null || cart.isEmpty()) {
            throw new BusinessException("Giỏ hàng đang trống, không thể lập hóa đơn");
        }
        if (!staffRepo.existsById(staffId)) {
            throw new NotFoundException("nhân viên", staffId);
        }

        String invoiceId = invoiceRepo.nextId(LocalDate.now());
        Invoice invoice = new Invoice(invoiceId, staffId, LocalDateTime.now(), method, discount, InvoiceStatus.PAID);

        // Chốt giá sản phẩm tại thời điểm bán vào từng dòng chi tiết
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String productId = entry.getKey();
            int qty = entry.getValue();
            Product p = getProduct(productId);
            invoice.addItem(p.getId(), qty, p.getPrice());
        }

        invoiceRepo.create(invoice);
        return invoice;
    }

    public void cancel(String invoiceId) throws BusinessException, NotFoundException {
        invoiceRepo.cancel(invoiceId);
    }

    public Invoice get(String id) throws NotFoundException {
        if (id == null || id.trim().isEmpty()) {
            throw new NotFoundException("hóa đơn", id);
        }
        Optional<Invoice> opt = invoiceRepo.findById(id.trim().toUpperCase());
        if (opt.isEmpty()) {
            throw new NotFoundException("hóa đơn", id);
        }
        return opt.get();
    }

  
 
    public Page<InvoiceSummary> search(InvoiceCriteria criteria, int page, int size) {
        return invoiceRepo.search(criteria, page, size);
    }
   
}