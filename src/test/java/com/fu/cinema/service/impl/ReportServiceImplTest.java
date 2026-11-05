package com.fu.cinema.service.impl;

import com.fu.cinema.dto.MovieRevenueDTO;
import com.fu.cinema.dto.ReportResult;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    @DisplayName("generate() ném BusinessException nếu endDate < startDate (BR-15)")
    void generate_throwsExceptionWhenEndDateBeforeStartDate() {
        LocalDate start = LocalDate.of(2026, 11, 10);
        LocalDate end = LocalDate.of(2026, 11, 5);

        BusinessException ex = assertThrows(BusinessException.class, () -> reportService.generate(start, end));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("trước hoặc bằng") || ex.getMessage().contains("Từ ngày"));
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("generate() ném BusinessException nếu startDate hoặc endDate null")
    void generate_throwsExceptionWhenNullDates() {
        assertThrows(BusinessException.class, () -> reportService.generate(null, LocalDate.now()));
        assertThrows(BusinessException.class, () -> reportService.generate(LocalDate.now(), null));
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("generate() tính tổng đơn, tổng vé, tổng doanh thu chính xác")
    void generate_calculatesTotalsCorrectly() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 5);

        List<MovieRevenueDTO> revenues = List.of(
                new MovieRevenueDTO("Phim A", 3L, BigDecimal.valueOf(300000)),
                new MovieRevenueDTO("Phim B", 2L, BigDecimal.valueOf(200000))
        );

        Booking b1 = new Booking();
        Booking b2 = new Booking();
        List<Booking> bookings = List.of(b1, b2);

        when(bookingRepository.revenueByMovie(any(), any())).thenReturn(revenues);
        when(bookingRepository.findConfirmedInPeriod(any(), any())).thenReturn(bookings);

        ReportResult result = reportService.generate(start, end);

        assertNotNull(result);
        assertEquals(start, result.getStartDate());
        assertEquals(end, result.getEndDate());
        assertEquals(2, result.getTotalBookings());
        assertEquals(5, result.getTotalTickets());
        assertEquals(BigDecimal.valueOf(500000), result.getTotalRevenue());
        assertEquals(2, result.getRevenueByMovie().size());
        assertEquals(2, result.getBookings().size());
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("generate() trả về kết quả rỗng isEmpty = true khi không có đơn hàng")
    void generate_emptyResult() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 5);

        when(bookingRepository.revenueByMovie(any(), any())).thenReturn(List.of());
        when(bookingRepository.findConfirmedInPeriod(any(), any())).thenReturn(List.of());

        ReportResult result = reportService.generate(start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalBookings());
        assertEquals(0, result.getTotalTickets());
        assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
    }
}
