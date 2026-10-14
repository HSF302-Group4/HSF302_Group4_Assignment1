package com.fu.cinema.service;

import com.fu.cinema.dto.RatingSummary;
import com.fu.cinema.dto.ReviewForm;
import com.fu.cinema.entity.Customer;
import com.fu.cinema.entity.Movie;
import com.fu.cinema.entity.Review;
import com.fu.cinema.entity.ReviewId;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.repository.BookingRepository;
import com.fu.cinema.repository.CustomerRepository;
import com.fu.cinema.repository.MovieRepository;
import com.fu.cinema.repository.ReviewRepository;
import com.fu.cinema.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    @DisplayName("Khách chưa đăng nhập (customerId == null) -> canReview trả về false")
    void testCanReview_NullCustomer_ReturnsFalse() {
        assertFalse(reviewService.canReview(null, 1L));
        verify(bookingRepository, never()).countWatched(any(), any(), any());
    }

    @Test
    @DisplayName("Khách đã xem phim (countWatched > 0) -> canReview trả về true")
    void testCanReview_Watched_ReturnsTrue() {
        when(bookingRepository.countWatched(eq(1L), eq(1L), any(LocalDateTime.class))).thenReturn(1L);

        assertTrue(reviewService.canReview(1L, 1L));
    }

    @Test
    @DisplayName("Khách chưa xem phim (countWatched == 0) -> canReview trả về false")
    void testCanReview_NotWatched_ReturnsFalse() {
        when(bookingRepository.countWatched(eq(2L), eq(1L), any(LocalDateTime.class))).thenReturn(0L);

        assertFalse(reviewService.canReview(2L, 1L));
    }

    @Test
    @DisplayName("Chặn đánh giá nếu chưa xem phim (BR-16) -> throw BusinessException")
    void testSave_NotWatched_ThrowsBusinessException() {
        when(bookingRepository.countWatched(eq(2L), eq(1L), any(LocalDateTime.class))).thenReturn(0L);

        ReviewForm form = new ReviewForm();
        form.setRating(5);
        form.setComment("Phim rất hay!");

        BusinessException ex = assertThrows(BusinessException.class, () -> reviewService.save(2L, 1L, form));
        assertEquals("Bạn chỉ có thể đánh giá phim đã xem", ex.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lưu đánh giá mới khi khách đã xem (Upsert tạo mới với getReferenceById)")
    void testSave_NewReview_UpsertCreates() {
        when(bookingRepository.countWatched(eq(1L), eq(1L), any(LocalDateTime.class))).thenReturn(1L);
        when(reviewRepository.findById(new ReviewId(1L, 1L))).thenReturn(Optional.empty());

        Customer dummyCustomer = new Customer();
        dummyCustomer.setCustomerId(1L);
        Movie dummyMovie = new Movie();
        dummyMovie.setMovieId(1L);

        when(customerRepository.getReferenceById(1L)).thenReturn(dummyCustomer);
        when(movieRepository.getReferenceById(1L)).thenReturn(dummyMovie);

        ReviewForm form = new ReviewForm();
        form.setRating(5);
        form.setComment("  Phim siêu đỉnh!  ");

        reviewService.save(1L, 1L, form);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review saved = captor.getValue();

        assertEquals(new ReviewId(1L, 1L), saved.getId());
        assertEquals(5, saved.getRating());
        assertEquals("Phim siêu đỉnh!", saved.getComment(), "Comment phải được trim()");
        assertNotNull(saved.getReviewDate());
        assertEquals(dummyCustomer, saved.getCustomer());
        assertEquals(dummyMovie, saved.getMovie());
    }

    @Test
    @DisplayName("Cập nhật đánh giá đã tồn tại (Upsert sửa, không tạo mới)")
    void testSave_ExistingReview_UpsertUpdates() {
        when(bookingRepository.countWatched(eq(1L), eq(1L), any(LocalDateTime.class))).thenReturn(1L);

        Review existingReview = new Review();
        existingReview.setId(new ReviewId(1L, 1L));
        existingReview.setRating(5);
        existingReview.setComment("Cũ");

        when(reviewRepository.findById(new ReviewId(1L, 1L))).thenReturn(Optional.of(existingReview));

        ReviewForm form = new ReviewForm();
        form.setRating(4);
        form.setComment("Sửa lại 4 sao");

        reviewService.save(1L, 1L, form);

        verify(customerRepository, never()).getReferenceById(any());
        verify(movieRepository, never()).getReferenceById(any());
        verify(reviewRepository).save(existingReview);

        assertEquals(4, existingReview.getRating());
        assertEquals("Sửa lại 4 sao", existingReview.getComment());
    }

    @Test
    @DisplayName("Lấy điểm tổng hợp từ repository (summarize)")
    void testSummarize() {
        RatingSummary mockSummary = mock(RatingSummary.class);
        when(reviewRepository.summarize(1L)).thenReturn(mockSummary);

        RatingSummary summary = reviewService.summarize(1L);
        assertSame(mockSummary, summary);
        verify(reviewRepository).summarize(1L);
    }
}
