package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.CustomerItem;
import com.fu.cinema.desktop.model.MovieItem;
import com.fu.cinema.desktop.model.SaleResult;
import com.fu.cinema.desktop.model.SeatMapData;
import com.fu.cinema.desktop.model.ShowtimeItem;
import com.fu.cinema.desktop.session.DesktopSession;
import com.fu.cinema.dto.BookingRequest;
import com.fu.cinema.dto.SeatMap;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.entity.Customer;
import com.fu.cinema.entity.Movie;
import com.fu.cinema.entity.Room;
import com.fu.cinema.entity.Showtime;
import com.fu.cinema.enums.AgeRating;
import com.fu.cinema.enums.BookingStatus;
import com.fu.cinema.enums.MovieStatus;
import com.fu.cinema.service.BookingService;
import com.fu.cinema.service.CustomerService;
import com.fu.cinema.service.MovieService;
import com.fu.cinema.service.ShowtimeService;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CounterSaleFacadeTest {

    @Mock private CustomerService customerService;
    @Mock private MovieService movieService;
    @Mock private ShowtimeService showtimeService;
    @Mock private BookingService bookingService;
    @Mock private DesktopSession session;

    @InjectMocks private CounterSaleFacade facade;

    @Test
    @DisplayName("Tìm kiếm khách hàng trả về danh sách CustomerItem")
    void searchCustomers() {
        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setFullName("Nguyễn Văn A");
        customer.setMobile("0987654321");
        customer.setBirthday(LocalDate.now().minusYears(20));

        when(customerService.search("Nguyễn", 0, 20)).thenReturn(new PageImpl<>(List.of(customer)));

        List<CustomerItem> items = facade.searchCustomers("Nguyễn");

        assertEquals(1, items.size());
        assertEquals("Nguyễn Văn A", items.get(0).fullName());
        assertEquals("0987654321", items.get(0).mobile());
        assertEquals(20, items.get(0).age());
    }

    @Test
    @DisplayName("Lấy danh sách phim có thể lên lịch chiếu")
    void movies() {
        Movie movie = new Movie();
        movie.setMovieId(10L);
        movie.setTitle("Mai");
        movie.setDuration(120);
        movie.setAgeRating(AgeRating.T18);
        movie.setStatus(MovieStatus.SHOWING);

        when(movieService.findSchedulable()).thenReturn(List.of(movie));

        List<MovieItem> items = facade.movies();

        assertEquals(1, items.size());
        assertEquals("Mai", items.get(0).title());
        assertEquals(120, items.get(0).duration());
        assertEquals(AgeRating.T18, items.get(0).ageRating());
    }

    @Test
    @DisplayName("Lấy danh sách suất chiếu sắp tới của phim")
    void showtimes() {
        Room room = new Room();
        room.setRoomName("Cinema 1");

        Showtime showtime = new Showtime();
        showtime.setShowtimeId(100L);
        showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.of(2026, 12, 1, 19, 30));
        showtime.setTicketPrice(BigDecimal.valueOf(85000));

        when(showtimeService.findUpcomingByMovie(10L)).thenReturn(List.of(showtime));

        List<ShowtimeItem> items = facade.showtimes(10L);

        assertEquals(1, items.size());
        assertEquals(100L, items.get(0).id());
        assertEquals("Cinema 1", items.get(0).roomName());
        assertEquals(BigDecimal.valueOf(85000), items.get(0).price());
    }

    @Test
    @DisplayName("Lấy dữ liệu sơ đồ ghế SeatMapData")
    void seatMap() {
        Movie movie = new Movie();
        movie.setTitle("Dune 2");

        Room room = new Room();
        room.setRoomName("IMAX");

        Showtime showtime = new Showtime();
        showtime.setShowtimeId(200L);
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.now().plusDays(1));
        showtime.setTicketPrice(BigDecimal.valueOf(110000));

        SeatMap seatMap = new SeatMap(showtime, List.of("A", "B"), 10, Set.of("A1", "A2"));
        when(showtimeService.getSeatMap(200L)).thenReturn(seatMap);

        SeatMapData data = facade.seatMap(200L);

        assertNotNull(data);
        assertEquals("Dune 2", data.movieTitle());
        assertEquals("IMAX", data.roomName());
        assertEquals(2, data.rowLabels().size());
        assertEquals(10, data.seatsPerRow());
        assertTrue(data.soldSeats().contains("A1"));
    }

    @Test
    @DisplayName("Bán vé đã thanh toán (paid = true) gọi confirm sang CONFIRMED theo BR-D2")
    void sell_paidTrue_confirmsBooking() {
        Booking booking = new Booking();
        booking.setBookingId(555L);
        booking.setTotalAmount(BigDecimal.valueOf(160000));
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingService.create(eq(1L), any(BookingRequest.class))).thenReturn(booking);
        when(bookingService.getDetail(555L)).thenReturn(booking);

        SaleResult result = facade.sell(1L, 200L, List.of("B2", "B1"), true);

        verify(session).requireAdmin();
        verify(bookingService).create(eq(1L), any(BookingRequest.class));
        verify(bookingService).confirm(555L); // BR-D2
        assertEquals(555L, result.bookingId());
        assertEquals(BookingStatus.CONFIRMED, result.status());
        assertEquals(List.of("B1", "B2"), result.seats()); // Sắp xếp theo thứ tự ghế
    }

    @Test
    @DisplayName("Bán vé chưa thanh toán (paid = false) giữ trạng thái PENDING")
    void sell_paidFalse_remainsPending() {
        Booking booking = new Booking();
        booking.setBookingId(666L);
        booking.setTotalAmount(BigDecimal.valueOf(80000));
        booking.setStatus(BookingStatus.PENDING);

        when(bookingService.create(eq(2L), any(BookingRequest.class))).thenReturn(booking);
        when(bookingService.getDetail(666L)).thenReturn(booking);

        SaleResult result = facade.sell(2L, 200L, List.of("A1"), false);

        verify(session).requireAdmin();
        verify(bookingService).create(eq(2L), any(BookingRequest.class));
        verify(bookingService, never()).confirm(anyLong());
        assertEquals(BookingStatus.PENDING, result.status());
    }

    @Test
    @DisplayName("So sánh thứ tự ghế SEAT_ORDER tự nhiên (A2 < A10 < B1)")
    void seatOrderComparator() {
        List<String> raw = List.of("B1", "A10", "A2", "A1");
        List<String> sorted = raw.stream().sorted(CounterSaleFacade.SEAT_ORDER).toList();

        assertEquals(List.of("A1", "A2", "A10", "B1"), sorted);
    }
}
