package com.fu.cinema.desktop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    record DummyItem(String name) {}

    @Test
    @DisplayName("Chuyển đổi Page của Spring Data thành PageResult thành công")
    void of_validPage() {
        List<String> rawContent = List.of("Phim A", "Phim B");
        Page<String> springPage = new PageImpl<>(rawContent, PageRequest.of(0, 10), 25);

        PageResult<DummyItem> result = PageResult.of(springPage, DummyItem::new);

        assertNotNull(result);
        assertEquals(2, result.items().size());
        assertEquals("Phim A", result.items().get(0).name());
        assertEquals("Phim B", result.items().get(1).name());
        assertEquals(0, result.page());
        assertEquals(3, result.totalPages());
        assertEquals(25L, result.totalElements());
    }

    @Test
    @DisplayName("Xử lý trang rỗng với totalPages tối thiểu là 1")
    void of_emptyPage_hasAtLeastOneTotalPage() {
        Page<String> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        PageResult<DummyItem> result = PageResult.of(emptyPage, DummyItem::new);

        assertTrue(result.items().isEmpty());
        assertEquals(1, result.totalPages());
        assertEquals(0L, result.totalElements());
    }
}
