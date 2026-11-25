package repository.memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Criteria.ProductCriteria;
import model.Page;
import model.Product;
import repository.Repositories.ProductRepository;

public class InMemoryProductRepository implements ProductRepository {
    private final Map<String, Product> products = new LinkedHashMap<>();

    @Override
    public void insert(Product product) throws DuplicateIdException {
        String id = product.getId();
        if (products.containsKey(id)) {
            throw new DuplicateIdException("sản phẩm", id);
        }
        products.put(id, product);
    }

    @Override
    public Optional<Product> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(products.get(id.trim().toUpperCase()));
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return products.containsKey(id.trim().toUpperCase());
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public boolean update(Product product) throws NotFoundException {
        String id = product.getId();
        if (!products.containsKey(id)) {
            throw new NotFoundException("sản phẩm", id);
        }
        products.put(id, product);
        return true;
    }

    @Override
    public boolean deleteById(String id) throws NotFoundException {
        if (id == null) {
            throw new NotFoundException("sản phẩm", id);
        }
        String key = id.trim().toUpperCase();
        if (!products.containsKey(key)) {
            throw new NotFoundException("sản phẩm", id);
        }
        products.remove(key);
        return true;
    }

    @Override
    public Page<Product> search(ProductCriteria criteria, int page, int size) {
        List<Product> filtered = products.values().stream()
                .filter(p -> criteria == null || criteria.matchesKeyword(p.getName(), p.getId()))
                .filter(p -> criteria == null || criteria.categoryId() == null || criteria.categoryId().isBlank()
                        || p.getCategoryId().equalsIgnoreCase(criteria.categoryId().trim()))
                .filter(p -> criteria == null || criteria.minPrice() == null || p.getPrice() >= criteria.minPrice())
                .filter(p -> criteria == null || criteria.maxPrice() == null || p.getPrice() <= criteria.maxPrice())
                .filter(p -> criteria == null || criteria.status() == null || p.getStatus() == criteria.status())
                .filter(p -> criteria == null || !criteria.lowStockOnly() || p.getStock() < 10)
                .collect(Collectors.toList());
        return Page.of(filtered, page, size);
    }

    @Override
    public List<Product> findByCategory(String categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        String cleanCatId = categoryId.trim().toUpperCase();
        return products.values().stream()
                .filter(p -> p.getCategoryId().equalsIgnoreCase(cleanCatId))
                .collect(Collectors.toList());
    }

    @Override
    public void addStock(Map<String, Integer> quantities) throws NotFoundException {
        // Bước 1: Kiểm tra tất cả sản phẩm và số lượng trước
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String id = entry.getKey() == null ? null : entry.getKey().trim().toUpperCase();
            Integer quantity = entry.getValue();

            if (id == null || !products.containsKey(id)) {
                throw new NotFoundException("sản phẩm", entry.getKey());
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0: " + id);
            }
        }

        // Bước 2: Chỉ cộng kho khi tất cả dữ liệu hợp lệ
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String id = entry.getKey().trim().toUpperCase();
            int quantity = entry.getValue();
            Product product = products.get(id);
            product.setStock(product.getStock() + quantity);
        }
    }
}
