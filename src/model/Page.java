package model;

import java.util.Collections;
import java.util.List;

public record Page<T>(
        List<T> items,
        int page,
        int size,
        long totalItems
) {
    public Page {
        items = (items == null) ? List.of() : List.copyOf(items);
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            throw new IllegalArgumentException("Kích thước trang phải >= 1");
        }
        if (totalItems < 0) {
            throw new IllegalArgumentException("Tổng số phần tử không được âm");
        }
    }


    public int totalPages() {
        if (totalItems == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalItems / size);
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages();
    }

    
    public static <T> Page<T> of(List<T> all, int page, int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Kích thước trang phải >= 1");
        }
        List<T> source = (all == null) ? Collections.emptyList() : all;
        long total = source.size();
        int totalPages = (total == 0) ? 1 : (int) Math.ceil((double) total / size);

        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = (currentPage - 1) * size;
        int toIndex = (int) Math.min(fromIndex + (long) size, total);

        List<T> pageItems = (fromIndex >= toIndex)
                ? List.of()
                : List.copyOf(source.subList(fromIndex, toIndex));

        return new Page<>(pageItems, currentPage, size, total);
    }
}