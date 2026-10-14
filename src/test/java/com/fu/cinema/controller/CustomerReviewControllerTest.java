package com.fu.cinema.controller;

import com.fu.cinema.controller.customer.CustomerReviewController;
import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.enums.Role;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.service.ReviewService;
import com.fu.cinema.util.SessionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private CustomerReviewController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Khách đã xem gửi đánh giá hợp lệ -> lưu thành công và flash success")
    void testReview_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        SessionUser user = new SessionUser(1L, 1L, "Nguyen Van An", Role.CUSTOMER);
        session.setAttribute(SessionUtils.CURRENT_USER, user);

        mockMvc.perform(post("/customer/movies/1/review")
                        .session(session)
                        .param("rating", "5")
                        .param("comment", "Phim rất hay!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/1"))
                .andExpect(flash().attribute("success", "Cảm ơn bạn đã đánh giá!"));

        verify(reviewService).save(eq(1L), eq(1L), any());
    }

    @Test
    @DisplayName("Form đánh giá thiếu rating -> flash error")
    void testReview_ValidationFailed() throws Exception {
        MockHttpSession session = new MockHttpSession();
        SessionUser user = new SessionUser(1L, 1L, "Nguyen Van An", Role.CUSTOMER);
        session.setAttribute(SessionUtils.CURRENT_USER, user);

        mockMvc.perform(post("/customer/movies/1/review")
                        .session(session)
                        .param("comment", "Phim rất hay!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/1"))
                .andExpect(flash().attributeExists("error"));

        verify(reviewService, never()).save(any(), any(), any());
    }

    @Test
    @DisplayName("Khách chưa xem gửi đánh giá -> service ném BusinessException -> flash error")
    void testReview_NotWatched_BusinessException() throws Exception {
        MockHttpSession session = new MockHttpSession();
        SessionUser user = new SessionUser(2L, 2L, "Tran Thi Binh", Role.CUSTOMER);
        session.setAttribute(SessionUtils.CURRENT_USER, user);

        doThrow(new BusinessException("Bạn chỉ có thể đánh giá phim đã xem"))
                .when(reviewService).save(eq(2L), eq(1L), any());

        mockMvc.perform(post("/customer/movies/1/review")
                        .session(session)
                        .param("rating", "5")
                        .param("comment", "Chưa xem nhưng thích"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/1"))
                .andExpect(flash().attribute("error", "Bạn chỉ có thể đánh giá phim đã xem"));
    }
}
