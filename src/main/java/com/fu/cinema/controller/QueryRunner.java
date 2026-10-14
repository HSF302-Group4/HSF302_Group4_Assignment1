package com.fu.cinema.controller;

import com.fu.cinema.enums.MovieStatus;
import com.fu.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryRunner implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public void run(String... args) {
        log.info("\n==================================================================");
        log.info("=== BAT DAU KIEM THU CHUC NANG REPOSITORY (ISSUE #3) ===");
        log.info("==================================================================");

        try {
            log.info("\n---------------- [NHOM 1] Search & Pagination ----------------");

            String keywordInput = "An";
            var customers = customerRepository.search(keywordInput, PageRequest.of(0, 5));
            log.info("[INPUT: keyword='{}', page=0, size=5] ---> KET QUA (Tong {} khach hang):", keywordInput, customers.getTotalElements());
            customers.getContent().forEach(c ->
                    log.info("   + ID: {} | Ten: {} | Email: {} | SDT: {}", c.getCustomerId(), c.getFullName(), c.getEmail(), c.getMobile())
            );

            String movieKwInput = null;
            Long genreIdInput = null;
            List<MovieStatus> statusesInput = List.of(MovieStatus.SHOWING);
            var movies = movieRepository.search(movieKwInput, genreIdInput, statusesInput, PageRequest.of(0, 5));
            log.info("[INPUT: keyword={}, genreId={}, statuses={}] ---> KET QUA (Tong {} phim):", movieKwInput, genreIdInput, statusesInput, movies.getTotalElements());
            movies.getContent().forEach(m ->
                    log.info("   + ID: {} | Ten phim: {} | Trang thai: {}", m.getMovieId(), m.getTitle(), m.getStatus())
            );


            log.info("\n---------------- [NHOM 2] Overlap & Sold Seats ----------------");

            Long roomIdInput = 1L;
            LocalDateTime startInput = LocalDate.now().plusDays(1).atTime(9, 30);
            LocalDateTime endInput = LocalDate.now().plusDays(1).atTime(12, 0);
            Long excludeIdInput = null;
            var overlaps = showtimeRepository.findOverlapping(roomIdInput, startInput, endInput, excludeIdInput);
            log.info("[INPUT: roomId={}, start={}, end={}, excludeId={}] ---> KET QUA (So suat chieu chong cheo: {}):", roomIdInput, startInput, endInput, excludeIdInput, overlaps.size());
            overlaps.forEach(s ->
                    log.info("   + Showtime ID: {} | Phim: {} | Bat dau: {} | Ket thuc: {}", s.getShowtimeId(), s.getMovie().getTitle(), s.getStartTime(), s.getEndTime())
            );

            Long showtimeIdInput = 3L;
            var soldSeats = ticketRepository.findSoldSeatCodes(showtimeIdInput);
            log.info("[INPUT: showtimeId={}] ---> KET QUA (Cac ghe da ban): {}", showtimeIdInput, soldSeats);


            log.info("\n---------------- [NHOM 3] Revenue & DTO Projection ----------------");

            LocalDateTime fromInput = LocalDateTime.now().minusDays(10);
            LocalDateTime toInput = LocalDateTime.now().plusDays(5);
            var revenues = bookingRepository.revenueByMovie(fromInput, toInput);
            log.info("[INPUT: from={}, to={}] ---> KET QUA (Doanh thu theo phim):", fromInput, toInput);
            if (revenues.isEmpty()) {
                log.info("   + Khong co doanh thu nao trong khoang thoi gian nay.");
            } else {
                revenues.forEach(dto ->
                        log.info("   + Phim: '{}' | So ve da ban: {} | Doanh thu: {} VND", dto.getTitle(), dto.getTicketCount(), dto.getRevenue())
                );
            }

            Long movieIdInput = 1L;
            var ratingSummary = reviewRepository.summarize(movieIdInput);
            if (ratingSummary != null && ratingSummary.getTotal() > 0) {
                log.info("[INPUT: movieId={}] ---> KET QUA: Diem TB = {} | Tong so danh gia = {}", movieIdInput, ratingSummary.getAverage(), ratingSummary.getTotal());
            } else {
                log.info("[INPUT: movieId={}] ---> KET QUA: Phim nay chua co luot danh gia nao.", movieIdInput);
            }


            log.info("\n---------------- [NHOM 4] Bulk Cancel & Native Dashboard ----------------");

            Long cancelShowtimeIdInput = 3L;
            log.info("[INPUT: showtimeId={}] ---> KET QUA: (Da bo qua update thuc thi de an toan du lieu seed)", cancelShowtimeIdInput);

            var roomShowtimes = showtimeRepository.countTodayShowtimesByRoom();
            log.info("[INPUT: Native Query GETDATE()] ---> KET QUA (Suat chieu hom nay theo phong):");
            roomShowtimes.forEach(item ->
                    log.info("   + Ten phong: '{}' | So suat chieu trong ngay: {}", item.getRoomName(), item.getShowtimeCount())
            );

            log.info("\n==================================================================");
            log.info("=== HOAN TAT KIEM THU THANH CONG TAT CA CAC CHUC NANG! ===");
            log.info("==================================================================");

        } catch (Exception e) {
            log.error("❌ Loi khi chay kiem thu: ", e);
        }
    }
}