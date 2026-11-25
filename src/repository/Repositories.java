package repository;

import exception.BusinessException;
import exception.NotFoundException;
import model.Category;
import model.Invoice;
import model.Product;
import model.Staff;


import model.Criteria.InvoiceCriteria;
import model.Criteria.InvoiceSummary;
import model.Criteria.ProductCriteria;
import model.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class Repositories {
    private Repositories() {
    }

    public interface CategoryRepository extends GenericRepository<Category, String> {
    }

    public interface StaffRepository extends GenericRepository<Staff, String> {
    }

    public interface ProductRepository extends GenericRepository<Product, String> {
        
        Page<Product> search(ProductCriteria criteria, int page, int size);
        List<Product> findByCategory(String categoryId);
        void addStock(Map<String, Integer> quantities) throws NotFoundException;
    }

    public interface InvoiceRepository extends GenericRepository<Invoice, String> {
        void create(Invoice invoice) throws BusinessException, NotFoundException;
        void cancel(String id) throws BusinessException, NotFoundException;
    
        Page<InvoiceSummary> search(InvoiceCriteria criteria, int page, int size);
        boolean existsByProduct(String productId);
        boolean existsByStaff(String staffId);
        String nextId(LocalDate date);
    }
}