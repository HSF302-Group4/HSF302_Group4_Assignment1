package com.fu.cinema.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Mot dong don hang CONFIRMED trong bao cao. */
public record ReportBookingRow(
        Long id,
        LocalDateTime bookingDate,
        String customerName,
        String movieTitle,
        BigDecimal total
) {}
