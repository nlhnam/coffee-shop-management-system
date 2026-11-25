package repository.memory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Staff;
import repository.Repositories.StaffRepository;

public class InMemoryStaffRepository implements StaffRepository {

    private final Map<String, Staff> data = new LinkedHashMap<>();

    @Override
    public List<Staff> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Optional<Staff> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.get(id.trim().toUpperCase()));
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) {
            return false;
        }
        return data.containsKey(id.trim().toUpperCase());
    }

    @Override
    public void insert(Staff s) throws DuplicateIdException {
        if (data.containsKey(s.getId())) {
            throw new DuplicateIdException("nhân viên", s.getId());
        }
        data.put(s.getId(), s);
    }

    @Override
    public boolean update(Staff s) throws NotFoundException {
        if (!data.containsKey(s.getId())) {
            throw new NotFoundException("nhân viên", s.getId());
        }
        data.put(s.getId(), s);
        return true;
    }

    @Override
    public boolean deleteById(String id) throws NotFoundException {
        if (id == null) {
            throw new NotFoundException("nhân viên", id);
        }
        String key = id.trim().toUpperCase();
        if (data.remove(key) == null) {
            throw new NotFoundException("nhân viên", id);
        }
        return true;
    }
}
