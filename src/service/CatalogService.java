package service;



import java.util.Map;

import exception.BusinessException;
import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Category;
import model.Product;
import model.ProductStatus;
import repository.Repositories;
import repository.Repositories.CategoryRepository;
import repository.Repositories.InvoiceRepository;
import repository.Repositories.ProductRepository;

public class CatalogService {
    private final Repositories.CategoryRepository categoryRepo;
    private final Repositories.ProductRepository productRepo;
    private final Repositories.InvoiceRepository invoiceRepo;

    //Constructors: nhận các repo từ bên ngoài
    public CatalogService(CategoryRepository categoryRepo, ProductRepository productRepo,
            InvoiceRepository invoiceRepo) {
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;
        this.invoiceRepo = invoiceRepo;
    }

    // =====================================
    // FR-01: QUẢN LÝ DANH MỤC
    // =====================================
    
    // Thêm danh mục
    public void addCategory(Category category) throws DuplicateIdException{
            categoryRepo.insert(category);
        }

    // Cập nhật danh mục
    public void UpdateCategory(Category category) throws NotFoundException{
        categoryRepo.update(category);
    }

     // Xóa danh mục
    public void deleteCategory(String id)
            throws NotFoundException, BusinessException {

        // Kiểm tra danh mục có sản phẩm không
        if (!productRepo.findByCategory(id).isEmpty()) {
            throw new BusinessException(
                    "Không thể xóa danh mục đang có sản phẩm."
            );
        }

        categoryRepo.deleteById(id);
    }


    // =====================================
    // FR-02: QUẢN LÝ SẢN PHẨM
    // =====================================

    // Thêm sản phẩm
    public void addProduct(Product product) throws DuplicateIdException, NotFoundException{

        //Kiểm tra danh mục của sp có tồn tại không
        if(!categoryRepo.existsById(product.getCategoryId())){
            throw new NotFoundException("danh mục", product.getCategoryId());
        }

        productRepo.insert(product);
    }

    // Cập nhật sản phẩm
    public void updateProduct (Product product) throws NotFoundException {

         //Kiểm tra danh mục của sp có tồn tại không
        if(!categoryRepo.existsById(product.getCategoryId())){
            throw new NotFoundException("danh mục", product.getCategoryId());
        }

        productRepo.update(product);
    }

    // Xoá sản phẩm
    public void deleteProduct(String id) throws NotFoundException, BusinessException{

        // Kiểm tra sản phẩm có tồn tại không
        if (!productRepo.existsById(id)) {
            throw new NotFoundException("sản phẩm", id);
        }

        // Ktra sản phẩm đã xuất hiện trong hóa đơn chưa
        if(!invoiceRepo.existsByProduct(id)) {
            throw new BusinessException(
                "Không thể xóa sản phẩm đã có trong hóa đơn. "
                + "Hãy chuyển trạng thái sang Ngừng bán."
            );
        }
        productRepo.deleteById(id);
    }

    // Chuyển trạng thái sản phẩm: SELLING <=> STOPPED
    public void toggleProductStatus(String id) throws NotFoundException {

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("sản phẩm", id)
                );

        if(product.getStatus() == ProductStatus.SELLING) {
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
}
