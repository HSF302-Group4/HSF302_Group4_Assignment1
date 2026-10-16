package com.fu.cinema.service;

import com.fu.cinema.dto.GenreForm;
import com.fu.cinema.entity.Genre;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.repository.GenreRepository;
import com.fu.cinema.repository.MovieRepository;
import com.fu.cinema.service.impl.GenreServiceImpl;
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
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    @Test
    @DisplayName("Thêm thể loại trùng tên không phân biệt hoa thường -> báo lỗi 'Tên thể loại đã tồn tại'")
    void testCreateDuplicateGenreIgnoreCase() {
        GenreForm form = new GenreForm();
        form.setGenreName("hành động");
        form.setDescription("Phim hành động kịch tính");

        when(genreRepository.existsByGenreNameIgnoreCase("hành động")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> genreService.create(form));
        assertEquals("Tên thể loại đã tồn tại", exception.getMessage());
        verify(genreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xóa thể loại vẫn còn phim (BR-05) -> ném BusinessException kèm tên thể loại")
    void testDeleteGenreHasMovies_BR05() {
        Long genreId = 1L;
        Genre genre = new Genre("Hành động", "Mô tả");
        genre.setGenreId(genreId);

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(movieRepository.existsByGenre_GenreId(genreId)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> genreService.delete(genreId));
        assertTrue(exception.getMessage().contains("Không thể xoá: vẫn còn phim thuộc thể loại \"Hành động\""));
        verify(genreRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Xóa thể loại không còn phim -> xóa thành công")
    void testDeleteGenreNoMoviesSuccess() {
        Long genreId = 5L;
        Genre genre = new Genre("Khoa học viễn tưởng", "Mô tả");
        genre.setGenreId(genreId);

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(movieRepository.existsByGenre_GenreId(genreId)).thenReturn(false);

        assertDoesNotThrow(() -> genreService.delete(genreId));
        verify(genreRepository, times(1)).delete(genre);
    }
}
