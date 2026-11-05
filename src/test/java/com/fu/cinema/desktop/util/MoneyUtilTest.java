package com.fu.cinema.desktop.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyUtilTest {

    @Test
    @DisplayName("Định dạng số tiền VNĐ chính xác")
    void format_validAmount() {
        String formatted = MoneyUtil.format(new BigDecimal("75000"));
        // Locale vi-VN formats 75000 as 75.000 đ
        assertTrue(formatted.contains("75.000"));
        assertTrue(formatted.endsWith("đ"));
    }

    @Test
    @DisplayName("Định dạng tiền với giá trị null trả về chuỗi rỗng")
    void format_nullReturnsEmpty() {
        assertEquals("", MoneyUtil.format((BigDecimal) null));
        assertEquals("", MoneyUtil.format((Number) null));
    }

    @Test
    @DisplayName("Định dạng với số nguyên long hoặc double")
    void format_primitiveNumbers() {
        String formattedLong = MoneyUtil.format(150000L);
        assertTrue(formattedLong.contains("150.000"));
        assertTrue(formattedLong.endsWith("đ"));

        String formattedDouble = MoneyUtil.format(120000.0);
        assertTrue(formattedDouble.contains("120.000"));
        assertTrue(formattedDouble.endsWith("đ"));
    }
}
