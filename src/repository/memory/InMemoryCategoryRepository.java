package repository.memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Category;
import repository.Repositories;
import repository.Repositories.CategoryRepository;

public class InMemoryCategoryRepository implements Repositories.CategoryRepository {
    private Map<String, Category> categories = new LinkedHashMap<>();

    //them category
    @Override 
    public void insert(Category category) throws DuplicateIdException{
        String id = category.getId();

        if (categories.containsKey(id)){
            throw new DuplicateIdException("danh mục", id);
        }

        categories.put(id, category);
    }

    //  tim danh muc theo ma
    @Override 
    public Optional<Category> findById(String id){
        return Optional.ofNullable(categories.get(id));
    }

    //Kiểm tra mã có tồn tại không
    @Override 
    public boolean existsById(String id){
        return categories.containsKey(id);
    }

    // tim tat ca category
    @Override 
    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }

    //cap nhat category
    @Override 
    public boolean update(Category category) throws NotFoundException {
        String id = category.getId();

        if (!categories.containsKey(id)) {
            throw new NotFoundException("danh mục", id);
        }

        categories.put(id, category);
        return true;
    }

    //Xoa category
    @Override 
    public boolean deleteById(String id) throws NotFoundException {
        if (!categories.containsKey(id)) {
            throw new NotFoundException("danh mục", id);
        }

        categories.remove(id);
        return true;
    }
}
