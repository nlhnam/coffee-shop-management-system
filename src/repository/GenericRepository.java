package repository;

import java.util.List;
import java.util.Optional;

import exception.DuplicateIdException;
import exception.NotFoundException;

public interface GenericRepository<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    boolean existsById(ID id);
    void insert(T entity) throws DuplicateIdException;
    boolean update(T entity) throws NotFoundException;
    boolean deleteById(ID id) throws NotFoundException;
}