package com.fu.cinema.service.impl;

import com.fu.cinema.dto.BookingRequest;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.enums.BookingStatus;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.exception.ResourceNotFoundException;
import com.fu.cinema.repository.BookingRepository;
import com.fu.cinema.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    @Override
    public Booking create(Long customerId, BookingRequest request) {
        return null;
    }

    @Override
    public Page<Booking> findByCustomer(Long customerId, BookingStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return bookingRepository.findByCustomer_CustomerIdAndStatusOrderByBookingDateDesc(customerId, status, pageable);
        }
        return bookingRepository.findByCustomer_CustomerIdOrderByBookingDateDesc(customerId, pageable);
    }

    @Override
    public Booking getDetailForCustomer(Long bookingId, Long customerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt vé"));

        // Chống dò ID (IDOR): Nếu không đúng chủ sở hữu, trả về 404 (ResourceNotFoundException)
        if (!booking.getCustomer().getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Không tìm thấy đơn đặt vé");
        }
        return booking;
    }

    @Override
    @Transactional
    public void cancelByCustomer(Long bookingId, Long customerId) {
        Booking booking = getDetailForCustomer(bookingId, customerId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Đơn đặt vé đã bị huỷ trước đó");
        }

        // BR-14: Chỉ được huỷ đơn trước giờ chiếu ít nhất 2 giờ
        LocalDateTime cancelDeadline = booking.getShowtime().getStartTime().minusHours(2);
        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new BusinessException("Chỉ được huỷ đơn trước giờ chiếu ít nhất 2 giờ");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Override
    public Page<Booking> search(BookingStatus status, LocalDate fromDate, LocalDate toDate, String keyword, int page, int size) {
        return null;
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
