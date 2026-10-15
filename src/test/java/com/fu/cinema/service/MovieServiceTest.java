package com.fu.cinema.service;

import com.fu.cinema.entity.Movie;
import com.fu.cinema.enums.MovieStatus;
import com.fu.cinema.repository.GenreRepository;
import com.fu.cinema.repository.MovieRepository;
import com.fu.cinema.repository.ShowtimeRepository;
import com.fu.cinema.service.impl.MovieServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    @Test
    @DisplayName("Xóa phim đã có suất chiếu (BR-06) -> chuyển status = STOPPED và trả về false (soft delete)")
    void testDeleteMovieWithShowtimes_SoftDelete() {
        Long movieId = 1L;
        Movie movie = new Movie();
        movie.setMovieId(movieId);
        movie.setTitle("Biệt Đội Siêu Tốc");
        movie.setStatus(MovieStatus.SHOWING);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(showtimeRepository.existsByMovie_MovieId(movieId)).thenReturn(true);

        boolean result = movieService.delete(movieId);

        assertFalse(result, "Phim có suất chiếu phải trả về false");
        assertEquals(MovieStatus.STOPPED, movie.getStatus(), "Trạng thái phim phải chuyển thành STOPPED");
        verify(movieRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Xóa phim chưa có suất chiếu -> xóa vật lý khỏi database và trả về true")
    void testDeleteMovieWithoutShowtimes_HardDelete() {
        Long movieId = 4L;
        Movie movie = new Movie();
        movie.setMovieId(movieId);
        movie.setTitle("Mùa Hè Năm Ấy");
        movie.setStatus(MovieStatus.COMING_SOON);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(showtimeRepository.existsByMovie_MovieId(movieId)).thenReturn(false);

        boolean result = movieService.delete(movieId);

        assertTrue(result, "Phim chưa có suất chiếu phải trả về true");
        verify(movieRepository, times(1)).delete(movie);
    }
}
