package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.ReportBookingRow;
import com.fu.cinema.desktop.model.ReportData;
import com.fu.cinema.desktop.model.RevenueRow;
import com.fu.cinema.dto.MovieRevenueDTO;
import com.fu.cinema.dto.ReportResult;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.entity.Customer;
import com.fu.cinema.entity.Movie;
import com.fu.cinema.entity.Showtime;
import com.fu.cinema.enums.BookingStatus;
import com.fu.cinema.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportFacadeTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportFacade facade;

    @Test
    @DisplayName("generate() anh xa ReportResult thanh ReportData day du")
    void generate_mapsReportResult() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 5);

        MovieRevenueDTO movieRev = new MovieRevenueDTO("Dune 2", 10L, BigDecimal.valueOf(1000000));

        Customer customer = new Customer();
        customer.setFullName("Nguyen Van A");

        Movie movie = new Movie();
        movie.setTitle("Dune 2");

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);

        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setBookingDate(LocalDateTime.of(2026, 11, 2, 19, 0));
        booking.setCustomer(customer);
        booking.setShowtime(showtime);
        booking.setTotalAmount(BigDecimal.valueOf(200000));
        booking.setStatus(BookingStatus.CONFIRMED);

        ReportResult result = new ReportResult(
                start,
                end,
                List.of(movieRev),
                List.of(booking),
                1L,
                10L,
                BigDecimal.valueOf(1000000)
        );

        when(reportService.generate(start, end)).thenReturn(result);

        ReportData data = facade.generate(start, end);

        assertNotNull(data);
        assertEquals(start, data.start());
        assertEquals(end, data.end());
        assertEquals(1, data.revenue().size());
        assertEquals("Dune 2", data.revenue().get(0).title());
        assertEquals(10L, data.revenue().get(0).tickets());
        assertEquals(BigDecimal.valueOf(1000000), data.revenue().get(0).revenue());

        assertEquals(1, data.bookings().size());
        assertEquals(1L, data.bookings().get(0).id());
        assertEquals("Nguyen Van A", data.bookings().get(0).customerName());
        assertEquals("Dune 2", data.bookings().get(0).movieTitle());
        assertEquals(BigDecimal.valueOf(200000), data.bookings().get(0).total());

        assertEquals(1L, data.totalBookings());
        assertEquals(10L, data.totalTickets());
        assertEquals(BigDecimal.valueOf(1000000), data.totalRevenue());
        assertFalse(data.isEmpty());
    }

    @Test
    @DisplayName("generate() xu ly bao cao rong isEmpty = true")
    void generate_emptyReport() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 5);

        ReportResult emptyResult = new ReportResult(
                start,
                end,
                List.of(),
                List.of(),
                0L,
                0L,
                BigDecimal.ZERO
        );

        when(reportService.generate(start, end)).thenReturn(emptyResult);

        ReportData data = facade.generate(start, end);

        assertNotNull(data);
        assertTrue(data.isEmpty());
        assertEquals(0, data.totalBookings());
        assertEquals(0, data.totalTickets());
    }
}
