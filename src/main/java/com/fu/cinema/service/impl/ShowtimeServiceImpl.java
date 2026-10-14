package com.fu.cinema.service.impl;

import com.fu.cinema.dto.RoomShowtimeCount;
import com.fu.cinema.dto.SeatMap;
import com.fu.cinema.dto.ShowtimeForm;
import com.fu.cinema.entity.Showtime;
import com.fu.cinema.enums.ShowtimeStatus;
import com.fu.cinema.repository.ShowtimeRepository;
import com.fu.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    @Override
    public Page<Showtime> search(Long movieId, Long roomId, LocalDate date, int page, int size) {
        return Page.empty();
    }

    @Override
    public ShowtimeForm getForm(Long showtimeId) {
        return new ShowtimeForm();
    }

    @Override
    @Transactional
    public void create(ShowtimeForm form) {
    }

    @Override
    @Transactional
    public void update(Long showtimeId, ShowtimeForm form) {
    }

    @Override
    @Transactional
    public boolean delete(Long showtimeId) {
        return false;
    }

    @Override
    public List<Showtime> findUpcomingByMovie(Long movieId) {
        return showtimeRepository.findByMovie_MovieIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                movieId, ShowtimeStatus.OPEN, LocalDateTime.now());
    }

    @Override
    public SeatMap getSeatMap(Long showtimeId) {
        return null;
    }

    @Override
    public List<RoomShowtimeCount> countTodayByRoom() {
        return showtimeRepository.countTodayShowtimesByRoom();
    }
}
