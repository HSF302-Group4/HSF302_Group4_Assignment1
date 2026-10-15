package com.fu.cinema.service.impl;

import com.fu.cinema.dto.BookingRequest;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.enums.BookingStatus;
import com.fu.cinema.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BookingServiceImpl implements BookingService {

    @Override
    public Booking create(Long customerId, BookingRequest request) {
        return new Booking(); // Trả về tạm để không lỗi compile
    }

    @Override
    public Page<Booking> findByCustomer(Long customerId, BookingStatus status, int page, int size) {
        return Page.empty();
    }

    @Override
    public Booking getDetailForCustomer(Long bookingId, Long customerId) {
        return new Booking();
    }

    @Override
    public void cancelByCustomer(Long bookingId, Long customerId) {
    }

    @Override
    public Page<Booking> search(BookingStatus status, LocalDate fromDate, LocalDate toDate, String keyword, int page, int size) {
        return Page.empty();
    }

    @Override
    public Booking getDetail(Long bookingId) {
        return null;
    }

    @Override
    public void confirm(Long bookingId) {
    }

    @Override
    public void cancelByAdmin(Long bookingId) {
    }

    @Override
    public long countPending() {
        return 0;
    }
}