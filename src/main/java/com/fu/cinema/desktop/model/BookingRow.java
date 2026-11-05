package com.fu.cinema.desktop.model;

import com.fu.cinema.entity.Booking;
import com.fu.cinema.entity.Showtime;
import com.fu.cinema.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingRow(
        Long id,
        String customerName,
        String movieTitle,
        String roomName,
        LocalDateTime startTime,
        int seatCount,
        BigDecimal total,
        BookingStatus status,
        LocalDateTime bookingDate
) {
    public static BookingRow from(Booking b) {
        Showtime s = b.getShowtime();
        int seats = b.getTickets() != null ? b.getTickets().size() : 0;
        String custName = b.getCustomer() != null ? b.getCustomer().getFullName() : "";
        String movie = s != null && s.getMovie() != null ? s.getMovie().getTitle() : "";
        String room = s != null && s.getRoom() != null ? s.getRoom().getRoomName() : "";
        LocalDateTime start = s != null ? s.getStartTime() : null;

        return new BookingRow(
                b.getBookingId(),
                custName,
                movie,
                room,
                start,
                seats,
                b.getTotalAmount(),
                b.getStatus(),
                b.getBookingDate()
        );
    }
}
