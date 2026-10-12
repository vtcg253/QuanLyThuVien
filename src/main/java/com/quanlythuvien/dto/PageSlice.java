package com.quanlythuvien.dto;

import java.util.ArrayList;
import java.util.List;

public record PageSlice<T>(
        List<T> items,
        int currentPage,
        int totalPages,
        int totalItems,
        int startItem,
        int endItem,
        List<Integer> pages
) {
    public static final int PAGE_SIZE = 10;

    public static <T> PageSlice<T> of(List<T> allItems, int requestedPage) {
        int totalItems = allItems.size();
        int totalPages = (int) Math.ceil(totalItems / (double) PAGE_SIZE);
        int currentPage = totalPages == 0 ? 1 : Math.max(1, Math.min(requestedPage, totalPages));
        int startIndex = (currentPage - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalItems);
        int firstPage = Math.max(1, currentPage - 2);
        int lastPage = Math.min(totalPages, firstPage + 4);
        firstPage = Math.max(1, lastPage - 4);
        List<Integer> pages = new ArrayList<>();
        for (int page = firstPage; page <= lastPage; page++) {
            pages.add(page);
        }

        return new PageSlice<>(
                List.copyOf(allItems.subList(startIndex, endIndex)),
                currentPage,
                totalPages,
                totalItems,
                totalItems == 0 ? 0 : startIndex + 1,
                endIndex,
                List.copyOf(pages)
        );
    }
}
