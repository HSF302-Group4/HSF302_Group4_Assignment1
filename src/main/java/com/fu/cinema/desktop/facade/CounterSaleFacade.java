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
import com.fu.cinema.entity.Showtime;
import com.fu.cinema.service.BookingService;
import com.fu.cinema.service.CustomerService;
import com.fu.cinema.service.MovieService;
import com.fu.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CounterSaleFacade {

    private final CustomerService customerService;
    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final BookingService bookingService;
    private final DesktopSession session;

    public List<CustomerItem> searchCustomers(String keyword) {
        return customerService.search(keyword, 0, 20).map(CustomerItem::from).getContent();
    }

    public List<MovieItem> movies() {
        return movieService.findSchedulable().stream()
                .map(m -> new MovieItem(m.getMovieId(), m.getTitle(), m.getDuration(), m.getAgeRating()))
                .toList();
    }

    public List<ShowtimeItem> showtimes(Long movieId) {
        return showtimeService.findUpcomingByMovie(movieId).stream()
                .map(s -> new ShowtimeItem(s.getShowtimeId(), s.getStartTime(), s.getRoom().getRoomName(), s.getTicketPrice()))
                .toList();
    }

    public SeatMapData seatMap(Long showtimeId) {
        SeatMap m = showtimeService.getSeatMap(showtimeId); // BR-10: nếu suất không OPEN hoặc đã chiếu -> BusinessException
        Showtime s = m.getShowtime();
        List<String> rowLabels = m.getRows() != null ? List.copyOf(m.getRows()) : List.of();
        int seatsPerRow = m.getSeatsPerRow() != null ? m.getSeatsPerRow() : 0;
        Set<String> sold = m.getSoldSeats() != null ? Set.copyOf(m.getSoldSeats()) : Set.of();
        return new SeatMapData(
                showtimeId,
                s.getMovie().getTitle(),
                s.getRoom().getRoomName(),
                s.getStartTime(),
                s.getTicketPrice(),
                rowLabels,
                seatsPerRow,
                sold
        );
    }

    /**
     * Bán vé tại quầy (BR-10, BR-11, BR-12, BR-D2).
     * Gọi BookingService.create (đã có khóa PESSIMISTIC_WRITE chống tranh chấp ghế).
     * Nếu paid = true thì xác nhận ngay sang CONFIRMED theo BR-D2.
     */
    @Transactional // ⚠ Ghi đè readOnly (mục 0.4)
    public SaleResult sell(Long customerId, Long showtimeId, List<String> seats, boolean paid) {
        session.requireAdmin();
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(showtimeId);
        request.setSeatCodes(new ArrayList<>(seats));
        Booking booking = bookingService.create(customerId, request); // PENDING
        if (paid) {
            bookingService.confirm(booking.getBookingId()); // BR-D2 -> CONFIRMED (cùng transaction)
        }
        Booking current = bookingService.getDetail(booking.getBookingId());
        return new SaleResult(
                current.getBookingId(),
                seats.stream().sorted(SEAT_ORDER).toList(),
                current.getTotalAmount(),
                current.getStatus()
        );
    }

    /** So sánh thứ tự ghế tự nhiên: A2 < A10 < B1 */
    public static final Comparator<String> SEAT_ORDER = Comparator
            .comparing((String s) -> s.charAt(0))
            .thenComparingInt(s -> Integer.parseInt(s.substring(1)));


    public SeatMapData seatMap(Long showtimeId) {
        SeatMap m = showtimeService.getSeatMap(showtimeId);          // BR-10: suất không OPEN/đã bắt đầu → BusinessException
        Showtime s = m.getShowtime();
        return new SeatMapData(showtimeId, s.getMovie().getTitle(), s.getRoom().getRoomName(), s.getStartTime(),
                s.getTicketPrice(), List.copyOf(m.getRowLabels()), m.getSeatsPerRow(), Set.copyOf(m.getSoldSeats()));
    }
}
