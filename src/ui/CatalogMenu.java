
package ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import exception.BusinessException;
import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Category;
import model.Criteria.ProductCriteria;
import model.Criteria.ProductSort;
import model.Page;
import model.Product;
import model.ProductStatus;
import service.CatalogService;

public class CatalogMenu {

    private final CatalogService service;

    private static final int PAGE_SIZE = 5;

    public CatalogMenu(CatalogService service) {
        this.service = service;
    }

    // =====================================
    // MAIN CATALOG MENU
    // =====================================

    public void show() {
        while (true) {
            System.out.println("\n========== QUẢN LÝ DANH MỤC & SẢN PHẨM ==========");
            System.out.println("1. Quản lý danh mục");
            System.out.println("2. Quản lý sản phẩm");
            System.out.println("3. Nhập kho");
            System.out.println("4. Tìm kiếm sản phẩm");
            System.out.println("0. Quay lại");

            int choice = Input.integer("Lựa chọn: ");

            switch (choice) {
                case 1 -> categoryMenu();
                case 2 -> productMenu();
                case 3 -> stockMenu();
                case 4 -> searchMenu();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Lựa chọn không hợp lệ.");
            }
        }
    }

    // =====================================
    // CATEGORY MENU - FR01
    // =====================================

    private void categoryMenu() {
        while (true) {
            System.out.println("\n========== QUẢN LÝ DANH MỤC ==========");
            System.out.println("1. Xem danh sách danh mục");
            System.out.println("2. Thêm danh mục");
            System.out.println("3. Cập nhật danh mục");
            System.out.println("4. Xóa danh mục");
            System.out.println("0. Quay lại");

            int choice = Input.integer("Lựa chọn: ");

            try {
                switch (choice) {
                    case 1 -> showCategories();
                    case 2 -> addCategory();
                    case 3 -> updateCategory();
                    case 4 -> deleteCategory();
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ.");
                }
            } catch (DuplicateIdException | NotFoundException
                     | BusinessException | IllegalArgumentException e) {
                System.out.println("Lỗi: " + e.getMessage());
            }
        }
    }

    private void showCategories() {
        List<Category> categories = service.getAllCategories();

        System.out.println("\n========== DANH SÁCH DANH MỤC ==========");

        if (categories.isEmpty()) {
            System.out.println("Chưa có danh mục.");
            return;
        }

        for (Category category : categories) {
            System.out.println(category);
        }
    }

    private void addCategory() throws DuplicateIdException {
        System.out.println("\n========== THÊM DANH MỤC ==========");

        String id = Input.required("Mã danh mục: ");
        String name = Input.required("Tên danh mục: ");

        Category category = new Category(id, name);

        service.addCategory(category);

        System.out.println("Thêm danh mục thành công.");
    }

    private void updateCategory() throws NotFoundException {
        System.out.println("\n========== CẬP NHẬT DANH MỤC ==========");

        String id = Input.required("Nhập mã danh mục cần sửa: ");

        Category oldCategory = service.findCategoryById(id)
                .orElseThrow(() -> new NotFoundException("danh mục", id));

        System.out.println("Danh mục hiện tại: " + oldCategory);

        String name = Input.required("Tên mới: ");

        Category updated = new Category(id, name);

        service.updateCategory(updated);

        System.out.println("Cập nhật danh mục thành công.");
    }

    private void deleteCategory()
            throws NotFoundException, BusinessException {

        System.out.println("\n========== XÓA DANH MỤC ==========");

        String id = Input.required("Nhập mã danh mục cần xóa: ");

        Category category = service.findCategoryById(id)
                .orElseThrow(() -> new NotFoundException("danh mục", id));

        System.out.println("Bạn đang chọn: " + category);

        if (Input.confirm("Bạn có chắc muốn xóa?")) {
            service.deleteCategory(id);
            System.out.println("Xóa danh mục thành công.");
        } else {
            System.out.println("Đã hủy thao tác.");
        }
    }

    // =====================================
    // PRODUCT MENU - FR02
    // =====================================

    private void productMenu() {
        while (true) {
            System.out.println("\n========== QUẢN LÝ SẢN PHẨM ==========");
            System.out.println("1. Xem danh sách sản phẩm");
            System.out.println("2. Thêm sản phẩm");
            System.out.println("3. Cập nhật sản phẩm");
            System.out.println("4. Xóa sản phẩm");
            System.out.println("5. Chuyển trạng thái sản phẩm");
            System.out.println("0. Quay lại");

            int choice = Input.integer("Lựa chọn: ");

            try {
                switch (choice) {
                    case 1 -> showProducts();
                    case 2 -> addProduct();
                    case 3 -> updateProduct();
                    case 4 -> deleteProduct();
                    case 5 -> toggleProductStatus();
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ.");
                }
            } catch (DuplicateIdException | NotFoundException
                     | BusinessException | IllegalArgumentException e) {
                System.out.println("Lỗi: " + e.getMessage());
            }
        }
    }

    private void showProducts() {
        List<Product> products = service.getAllProducts();

        System.out.println("\n========== DANH SÁCH SẢN PHẨM ==========");

        if (products.isEmpty()) {
            System.out.println("Chưa có sản phẩm.");
            return;
        }

        for (Product product : products) {
            System.out.println(product);
        }
    }

    private void addProduct()
            throws DuplicateIdException, NotFoundException {

        System.out.println("\n========== THÊM SẢN PHẨM ==========");

        showCategories();

        String id = Input.required("Mã sản phẩm: ");
        String name = Input.required("Tên sản phẩm: ");
        String categoryId = Input.required("Mã danh mục: ");
        double price = Input.decimal("Giá bán: ");
        int stock = Input.integer("Tồn kho ban đầu: ");

        ProductStatus status = Input.choose(
                "Chọn trạng thái:",
                ProductStatus.values()
        );

        Product product = new Product(
                id, name, categoryId, price, stock, status
        );

        service.addProduct(product);

        System.out.println("Thêm sản phẩm thành công.");
    }

    private void updateProduct() throws NotFoundException {
        System.out.println("\n========== CẬP NHẬT SẢN PHẨM ==========");

        String id = Input.required("Mã sản phẩm cần sửa: ");

        Product old = service.findProductById(id)
                .orElseThrow(() -> new NotFoundException("sản phẩm", id));

        System.out.println("Thông tin hiện tại:");
        System.out.println(old);

        String name = Input.required("Tên mới: ");

        showCategories();
        String categoryId = Input.required("Mã danh mục mới: ");

        double price = Input.decimal("Giá mới: ");

        Product updated = new Product(
                id,
                name,
                categoryId,
                price,
                old.getStock(),
                old.getStatus()
        );

        service.updateProduct(updated);

        System.out.println("Cập nhật sản phẩm thành công.");
    }

    private void deleteProduct()
            throws NotFoundException, BusinessException {

        System.out.println("\n========== XÓA SẢN PHẨM ==========");

        String id = Input.required("Mã sản phẩm cần xóa: ");

        Product product = service.findProductById(id)
                .orElseThrow(() -> new NotFoundException("sản phẩm", id));

        System.out.println(product);

        if (Input.confirm("Bạn có chắc muốn xóa?")) {
            service.deleteProduct(id);
            System.out.println("Xóa sản phẩm thành công.");
        } else {
            System.out.println("Đã hủy thao tác.");
        }
    }

    private void toggleProductStatus() throws NotFoundException {
        System.out.println("\n========== CHUYỂN TRẠNG THÁI ==========");

        String id = Input.required("Mã sản phẩm: ");

        service.toggleProductStatus(id);

        System.out.println("Chuyển trạng thái thành công.");
    }

    // =====================================
    // STOCK MENU - FR07
    // =====================================

    private void stockMenu() {
        System.out.println("\n========== NHẬP KHO ==========");
        System.out.println("Nhập mã sản phẩm và số lượng.");
        System.out.println("Để trống mã sản phẩm rồi Enter để kết thúc.");

        Map<String, Integer> quantities = new LinkedHashMap<>();

        while (true) {
            String id = Input.optional("Mã sản phẩm: ");

            if (id == null) {
                break;
            }

            int quantity = Input.integer("Số lượng nhập: ");

            if (quantity <= 0) {
                System.out.println("Số lượng phải lớn hơn 0.");
                continue;
            }

            // Nếu mã đã nhập trước đó, cộng dồn số lượng
            quantities.merge(id.toUpperCase(), quantity, Integer::sum);

            System.out.println("Đã ghi nhận " + id + ": " + quantity);
        }

        if (quantities.isEmpty()) {
            System.out.println("Không có dữ liệu nhập kho.");
            return;
        }

        System.out.println("\n========== XÁC NHẬN NHẬP KHO ==========");

        quantities.forEach((id, quantity) ->
                System.out.println(id + " : +" + quantity)
        );

        if (!Input.confirm("Xác nhận nhập kho?")) {
            System.out.println("Đã hủy nhập kho.");
            return;
        }

        try {
            service.addStock(quantities);
            System.out.println("Nhập kho thành công.");
        } catch (NotFoundException | IllegalArgumentException e) {
            System.out.println("Nhập kho thất bại: " + e.getMessage());
        }
    }

    // =====================================
    // SEARCH MENU - FR08
    // =====================================

    private void searchMenu() {
        System.out.println("\n========== TÌM KIẾM SẢN PHẨM ==========");

        String keyword = Input.optional("Từ khóa (mã hoặc tên): ");

        showCategories();
        String categoryId = Input.optional(
                "Mã danh mục (Enter để bỏ qua): "
        );

        Double minPrice = Input.optionalDecimal(
                "Giá tối thiểu (Enter để bỏ qua): "
        );

        Double maxPrice = Input.optionalDecimal(
                "Giá tối đa (Enter để bỏ qua): "
        );

        ProductStatus status = Input.chooseOptional(
                "Lọc theo trạng thái:",
                ProductStatus.values()
        );

        boolean lowStockOnly = Input.confirm(
                "Chỉ hiển thị sản phẩm tồn kho dưới 10?"
        );

        ProductCriteria criteria = new ProductCriteria(
                keyword,
                categoryId,
                minPrice,
                maxPrice,
                status,
                lowStockOnly
        );

        ProductSort sort = Input.choose(
                "Chọn cách sắp xếp:",
                ProductSort.values()
        );

        browseProducts(criteria, sort);
    }

    private void browseProducts(
            ProductCriteria criteria,
            ProductSort sort) {

        int currentPage = 1;

        while (true) {
            Page<Product> result = service.searchProducts(
                    criteria,
                    sort,
                    currentPage,
                    PAGE_SIZE
            );

            System.out.println("\n========== KẾT QUẢ TÌM KIẾM ==========");

            if (result.items().isEmpty()) {
                System.out.println("Không tìm thấy sản phẩm phù hợp.");
            } else {
                for (Product product : result.items()) {
                    System.out.println(product);
                }
            }

            System.out.println("--------------------------------------");
            System.out.println("Trang: " + result.page()
                    + "/" + result.totalPages());
            System.out.println("Tổng sản phẩm: " + result.totalItems());

            System.out.println("\n1. Trang trước");
            System.out.println("2. Trang tiếp");
            System.out.println("0. Thoát tìm kiếm");

            int choice = Input.integer("Lựa chọn: ");

            switch (choice) {
                case 1 -> {
                    if (result.hasPrevious()) {
                        currentPage--;
                    } else {
                        System.out.println("Đây là trang đầu tiên.");
                    }
                }
                case 2 -> {
                    if (result.hasNext()) {
                        currentPage++;
                    } else {
                        System.out.println("Đây là trang cuối cùng.");
                    }
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("Lựa chọn không hợp lệ.");
            }
        }
    }
}
