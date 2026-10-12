package com.quanlythuvien.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageSliceTest {

    @Test
    void returnsRequestedPageAndVisiblePageNumbers() {
        List<Integer> values = IntStream.rangeClosed(1, 35).boxed().toList();

        PageSlice<Integer> page = PageSlice.of(values, 2);

        assertEquals(List.of(11, 12, 13, 14, 15, 16, 17, 18, 19, 20), page.items());
        assertEquals(2, page.currentPage());
        assertEquals(4, page.totalPages());
        assertEquals(35, page.totalItems());
        assertEquals(11, page.startItem());
        assertEquals(20, page.endItem());
        assertEquals(List.of(1, 2, 3, 4), page.pages());
    }

    @Test
    void clampsPageOutsideAvailableRange() {
        List<Integer> values = IntStream.rangeClosed(1, 11).boxed().toList();

        PageSlice<Integer> first = PageSlice.of(values, 0);
        PageSlice<Integer> last = PageSlice.of(values, 9);

        assertEquals(1, first.currentPage());
        assertEquals(2, last.currentPage());
        assertEquals(List.of(11), last.items());
    }

    @Test
    void supportsEmptyLists() {
        PageSlice<Integer> page = PageSlice.of(List.of(), 1);

        assertEquals(List.of(), page.items());
        assertEquals(0, page.totalPages());
        assertEquals(0, page.totalItems());
        assertEquals(0, page.startItem());
        assertEquals(0, page.endItem());
    }
}
