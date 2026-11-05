package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.BookingDetail;
import com.fu.cinema.desktop.model.BookingRow;
import com.fu.cinema.desktop.model.PageResult;
import com.fu.cinema.desktop.session.DesktopSession;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.entity.Customer;
import com.fu.cinema.entity.Movie;
import com.fu.cinema.entity.Room;
import com.fu.cinema.entity.Showtime;
import com.fu.cinema.entity.Ticket;
import com.fu.cinema.enums.BookingStatus;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingFacadeTest {

    @Mock private BookingService bookingService;
    @Mock private DesktopSession session;

    @InjectMocks private BookingFacade facade;

    // ==================== Helpers ====================

    private Booking makeBooking(Long id, BookingStatus status) {
        Customer customer = new Customer();
        customer.setFullName("Nguyen Van A");
        customer.setMobile("0901234567");
        customer.setBirthday(LocalDate.now().minusYears(25));

        Movie movie = new Movie();
        movie.setTitle("Avengers");

        Room room = new Room();
        room.setRoomName("Phong 1");

        Showtime showtime = new Showtime();
        showtime.setStartTime(LocalDateTime.of(2026, 12, 20, 19, 30));
        showtime.setMovie(movie);
        showtime.setRoom(room);

        Ticket ticket = new Ticket();
        ticket.setSeatCode("A1");

        Booking booking = new Booking();
        booking.setBookingId(id);
        booking.setStatus(status);
        booking.setTotalAmount(BigDecimal.valueOf(85000));
        booking.setBookingDate(LocalDateTime.now());
        booking.setCustomer(customer);
        booking.setShowtime(showtime);
        booking.addTicket(ticket);
        return booking;
    }

    // ==================== search() ====================

    @Test
    @DisplayName("search() tra ve PageResult<BookingRow> tu BookingService")
    void search_returnsMappedPageResult() {
        Booking booking = makeBooking(1L, BookingStatus.PENDING);
        when(bookingService.search(null, null, null, "Nguyen", 0, 15))
                .thenReturn(new PageImpl<>(List.of(booking)));

        PageResult<BookingRow> result = facade.search(null, null, null, "Nguyen", 0);

        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(1L, result.items().get(0).id());
        assertEquals("Nguyen Van A", result.items().get(0).customerName());
        assertEquals("Avengers", result.items().get(0).movieTitle());
        assertEquals("Phong 1", result.items().get(0).roomName());
        assertEquals(BookingStatus.PENDING, result.items().get(0).status());
        assertEquals(1, result.items().get(0).seatCount());
        assertEquals(0, result.page());
        assertEquals(1, result.totalPages());
        assertEquals(1L, result.totalElements());
    }

    @Test
    @DisplayName("search() loc theo trang thai CONFIRMED")
    void search_filterByStatus() {
        Booking booking = makeBooking(2L, BookingStatus.CONFIRMED);
        when(bookingService.search(eq(BookingStatus.CONFIRMED), any(), any(), isNull(), eq(0), eq(15)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        PageResult<BookingRow> result = facade.search(BookingStatus.CONFIRMED, null, null, null, 0);

        assertEquals(1, result.items().size());
        assertEquals(BookingStatus.CONFIRMED, result.items().get(0).status());
        verify(bookingService).search(BookingStatus.CONFIRMED, null, null, null, 0, 15);
    }

    @Test
    @DisplayName("search() tra ve PageResult rong khi khong co ket qua")
    void search_emptyResult() {
        when(bookingService.search(null, null, null, "Khong ton tai", 0, 15))
                .thenReturn(new PageImpl<>(List.of()));

        PageResult<BookingRow> result = facade.search(null, null, null, "Khong ton tai", 0);

        assertTrue(result.items().isEmpty());
        assertEquals(0, result.totalElements());
        assertEquals(1, result.totalPages()); // Math.max(1, 0) = 1
    }

    // ==================== detail() ====================

    @Test
    @DisplayName("detail() anh xa Booking thanh BookingDetail chinh xac")
    void detail_mapsBookingToDetail() {
        Booking booking = makeBooking(10L, BookingStatus.PENDING);
        when(bookingService.getDetail(10L)).thenReturn(booking);

        BookingDetail detail = facade.detail(10L);

        assertNotNull(detail);
        assertEquals(10L, detail.id());
        assertEquals("Nguyen Van A", detail.customerName());
        assertEquals("0901234567", detail.mobile());
        assertEquals("Avengers", detail.movieTitle());
        assertEquals("Phong 1", detail.roomName());
        assertEquals(LocalDateTime.of(2026, 12, 20, 19, 30), detail.startTime());
        assertEquals(List.of("A1"), detail.seats());
        assertEquals(BigDecimal.valueOf(85000), detail.total());
        assertEquals(BookingStatus.PENDING, detail.status());
    }

    @Test
    @DisplayName("detail() xu ly booking khong co tickets (tra ve danh sach rong)")
    void detail_nullTickets_returnsEmptySeats() {
        Booking booking = makeBooking(11L, BookingStatus.PENDING);
        booking.getTickets().clear(); // xoa tat ca tickets
        when(bookingService.getDetail(11L)).thenReturn(booking);

        BookingDetail detail = facade.detail(11L);

        assertNotNull(detail);
        assertTrue(detail.seats().isEmpty());
    }

    @Test
    @DisplayName("detail() xu ly booking khong co customer")
    void detail_nullCustomer_returnsEmptyStrings() {
        Booking booking = makeBooking(12L, BookingStatus.PENDING);
        booking.setCustomer(null);
        when(bookingService.getDetail(12L)).thenReturn(booking);

        BookingDetail detail = facade.detail(12L);

        assertEquals("", detail.customerName());
        assertEquals("", detail.mobile());
    }

    // ==================== confirm() ====================

    @Test
    @DisplayName("confirm() goi requireAdmin() va bookingService.confirm(id)")
    void confirm_requiresAdminAndDelegates() {
        doNothing().when(session).requireAdmin();
        doNothing().when(bookingService).confirm(42L);

        facade.confirm(42L);

        verify(session).requireAdmin();
        verify(bookingService).confirm(42L);
    }

    @Test
    @DisplayName("confirm() nem BusinessException khi khong phai Admin (BR-13)")
    void confirm_throwsWhenNotAdmin() {
        doThrow(new BusinessException("Ban khong co quyen"))
                .when(session).requireAdmin();

        assertThrows(BusinessException.class, () -> facade.confirm(99L));
        verify(bookingService, never()).confirm(anyLong());
    }

    // ==================== cancel() ====================

    @Test
    @DisplayName("cancel() goi requireAdmin() va bookingService.cancelByAdmin(id)")
    void cancel_requiresAdminAndDelegates() {
        doNothing().when(session).requireAdmin();
        doNothing().when(bookingService).cancelByAdmin(55L);

        facade.cancel(55L);

        verify(session).requireAdmin();
        verify(bookingService).cancelByAdmin(55L);
    }

    @Test
    @DisplayName("cancel() nem BusinessException khi khong phai Admin (BR-13)")
    void cancel_throwsWhenNotAdmin() {
        doThrow(new BusinessException("Ban khong co quyen"))
                .when(session).requireAdmin();

        assertThrows(BusinessException.class, () -> facade.cancel(77L));
        verify(bookingService, never()).cancelByAdmin(anyLong());
    }
}
