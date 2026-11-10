package com.fu.cinema.dto;

import com.fu.cinema.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportResult {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<MovieRevenueDTO> revenueByMovie;
    private List<Booking> bookings;
    private long totalBookings;
    private long totalTickets;
    private BigDecimal totalRevenue;

    public boolean isEmpty() {
        return bookings == null || bookings.isEmpty();
    }
}
