package service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import exception.BusinessException;
import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Category;
import model.Criteria.ProductCriteria;
import model.Criteria.ProductSort;
import model.Page;
import model.Product;
import model.ProductStatus;
import repository.Repositories.CategoryRepository;
import repository.Repositories.InvoiceRepository;
import repository.Repositories.ProductRepository;

public class CatalogService {
    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final InvoiceRepository invoiceRepo;

    public CatalogService(CategoryRepository categoryRepo, ProductRepository productRepo,
                          InvoiceRepository invoiceRepo) {
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
        this.invoiceRepo = invoiceRepo;
    }

    // =====================================
    // FR-01: QUẢN LÝ DANH MỤC
    // =====================================

    public void addCategory(Category category) throws DuplicateIdException {
        categoryRepo.insert(category);
    }

    public void updateCategory(Category category) throws NotFoundException {
        categoryRepo.update(category);
    }

    public void deleteCategory(String id) throws NotFoundException, BusinessException {
        if (!productRepo.findByCategory(id).isEmpty()) {
            throw new BusinessException("Không thể xóa danh mục đang có sản phẩm.");
        }
        categoryRepo.deleteById(id);
    }

    // =====================================
    // FR-02: QUẢN LÝ SẢN PHẨM
    // =====================================

    public void addProduct(Product product) throws DuplicateIdException, NotFoundException {
        if (!categoryRepo.existsById(product.getCategoryId())) {
            throw new NotFoundException("danh mục", product.getCategoryId());
        }
        productRepo.insert(product);
    }

    public void updateProduct(Product product) throws NotFoundException {
        if (!categoryRepo.existsById(product.getCategoryId())) {
            throw new NotFoundException("danh mục", product.getCategoryId());
        }
        productRepo.update(product);
    }

    public void deleteProduct(String id) throws NotFoundException, BusinessException {
        // Sửa lỗi logic: chỉ chặn xóa KHI sản phẩm ĐÃ CÓ trong hóa đơn
        if (invoiceRepo.existsByProduct(id)) {
            throw new BusinessException(
                    "Không thể xóa sản phẩm đã có trong hóa đơn. Hãy chuyển trạng thái sang Ngừng bán.");
        }
        productRepo.deleteById(id);
    }

    public void toggleProductStatus(String id) throws NotFoundException {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("sản phẩm", id));

        if (product.getStatus() == ProductStatus.SELLING) {
            product.setStatus(ProductStatus.STOPPED);
        } else {
            product.setStatus(ProductStatus.SELLING);
        }
        productRepo.update(product);
    }

    // =====================================
    // FR-07: NHẬP KHO
    // =====================================

    public void addStock(Map<String, Integer> quantities) throws NotFoundException {
        productRepo.addStock(quantities);
    }

    // =====================================
    // FR-08: TÌM KIẾM SẢN PHẨM
    // =====================================

    public Page<Product> searchProducts(
            ProductCriteria criteria,
            ProductSort sort,
            int page,
            int size) {

        List<Product> products = productRepo.findAll();

        List<Product> filteredProducts = products.stream()
                .filter(p -> criteria == null || criteria.matchesKeyword(p.getName(), p.getId()))
                .filter(p -> criteria == null || criteria.categoryId() == null || criteria.categoryId().isBlank()
                        || p.getCategoryId().equalsIgnoreCase(criteria.categoryId().trim()))
                .filter(p -> criteria == null || criteria.minPrice() == null || p.getPrice() >= criteria.minPrice())
                .filter(p -> criteria == null || criteria.maxPrice() == null || p.getPrice() <= criteria.maxPrice())
                .filter(p -> criteria == null || criteria.status() == null || p.getStatus() == criteria.status())
                .filter(p -> criteria == null || !criteria.lowStockOnly() || p.getStock() < 10)
                .collect(Collectors.toList());

        if (sort != null) {
            filteredProducts.sort(sort.comparator());
        }

        return Page.of(filteredProducts, page, size);
    }

    // CÁC HÀM ĐỌC DỮ LIỆU CHO CATALOG MENU

    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    public Optional<Category> findCategoryById(String id) {
        return categoryRepo.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Optional<Product> findProductById(String id) {
        return productRepo.findById(id);
    }
}
