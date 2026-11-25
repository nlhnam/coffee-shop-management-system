package service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import exception.DuplicateIdException;
import exception.NotFoundException;
import model.Staff;


public class InMemoryStaffRepository {

    private final Map<String, Staff> data = new LinkedHashMap<>();
    
    public List<Staff> findAll() {
        return new ArrayList<>(data.values());
    }

    public Optional<Staff> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    public boolean existById(String id) {
        return data.containsKey(id);
    }

    public void  insert(Staff s) throws DuplicateIdException {
        if (data.containsKey(s.getId())) {
            throw new DuplicateIdException("Nhân viên", s.getId());
        }
        data.put(s.getId(), s);
    }

    public void update (Staff s) throws NotFoundException {
        if (!data.containsKey(s.getId())) {
            throw new NotFoundException("Nhân viên", s.getId());
        }
        data.put(s.getId(), s);
    }

    public void deleteById(String id) throws NotFoundException {
        if (data.remove(id) == null) {
            throw new NotFoundException("Nhân viên", id);
        }     
    }
}
