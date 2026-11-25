package repository.memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Product;
import repository.Repositories;
import repository.Repositories.ProductRepository;

import java.util.Optional;

public class InMemoryProductRepository implements Repositories.ProductRepository {
        private Map<String, Product> products = new LinkedHashMap<>();

        //them san pham
        @Override 
        public void insert(Product product) throws DuplicateIdException {
            String id = product.getId();

             // Kiem tra san pham đã tồn tại chưa
            if (products.containsKey(id)) {
                throw new DuplicateIdException("sản phẩm", id);
            }

            // Nếu chưa thì thêm vào map
            products.put(id, product);
        }       

        // Tìm sản phẩm theo mã
        @Override
        public Optional<Product> findById(String id) {
            return Optional.ofNullable(products.get(id));
        }
        
        //Kiểm tra mã có tồn tại không
        @Override 
        public boolean existsById(String id) {
            return products.containsKey(id);
        }

        //Lấy ds tất cả sp
        @Override 
        public List<Product> findAll(){
            return new ArrayList<>(products.values());
        }

        //Cập nhật sản phẩm
        @Override 
        public boolean update(Product product) throws NotFoundException{
            String id = product.getId();

            //Chỉ cập nhật nếu mã sản phẩm đã tồn tại
            if (!products.containsKey(id)) {
            throw new NotFoundException("sản phẩm", id);
        }

            products.put(id, product);
            return true;
        }

        //Xóa sp theo mã
        @Override 
        public boolean deleteById(String id) throws NotFoundException{

            //Kiểm tra sp có tồn tại không
            if (!products.containsKey(id)) {
            throw new NotFoundException("sản phẩm", id);
        }
            products.remove(id);
            return true;
        }

        //Tìm tất cả sp thuộc 1 danh mục
        @Override 
        public List<Product> findByCategory(String categoryId){
            return products.values()
                    .stream()
                    .filter(p -> p.getCategoryId().equals(categoryId))
                    .collect(Collectors.toList());
        }

     // Nhập kho nhiều sản phẩm
    @Override
    public void addStock(Map<String, Integer> quantities)
            throws NotFoundException {

        // Bước 1: Kiểm tra tất cả sản phẩm và số lượng
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {

            String id = entry.getKey();
            Integer quantity = entry.getValue();

            if (!products.containsKey(id)) {
                throw new NotFoundException("sản phẩm", id);
            }

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException(
                        "Số lượng nhập phải lớn hơn 0: " + id
                );
            }
        }

        // Bước 2: Chỉ cập nhật khi tất cả dữ liệu hợp lệ
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {

            String id = entry.getKey();
            int quantity = entry.getValue();

            Product product = products.get(id);

            product.setStock(product.getStock() + quantity);
        }
    }
}
