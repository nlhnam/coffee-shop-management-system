package repository.memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Category;
import repository.Repositories.CategoryRepository;

public class InMemoryCategoryRepository implements CategoryRepository {
    private final Map<String, Category> categories = new LinkedHashMap<>();

    @Override
    public void insert(Category category) throws DuplicateIdException {
        String id = category.getId();
        if (categories.containsKey(id)) {
            throw new DuplicateIdException("danh mục", id);
        }
        categories.put(id, category);
    }

    @Override
    public Optional<Category> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(categories.get(id.trim().toUpperCase()));
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return categories.containsKey(id.trim().toUpperCase());
    }

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }

    @Override
    public boolean update(Category category) throws NotFoundException {
        String id = category.getId();
        if (!categories.containsKey(id)) {
            throw new NotFoundException("danh mục", id);
        }
        categories.put(id, category);
        return true;
    }

    @Override
    public boolean deleteById(String id) throws NotFoundException {
        if (id == null) {
            throw new NotFoundException("danh mục", id);
        }
        String key = id.trim().toUpperCase();
        if (!categories.containsKey(key)) {
            throw new NotFoundException("danh mục", id);
        }
        categories.remove(key);
        return true;
    }
}
