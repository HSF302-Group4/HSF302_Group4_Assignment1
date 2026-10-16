package com.fu.cinema.integration;

import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.entity.Customer;
import com.fu.cinema.entity.Movie;
import com.fu.cinema.entity.Review;
import com.fu.cinema.entity.ReviewId;
import com.fu.cinema.enums.Role;
import com.fu.cinema.repository.CustomerRepository;
import com.fu.cinema.repository.MovieRepository;
import com.fu.cinema.repository.ReviewRepository;
import com.fu.cinema.util.SessionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class MovieReviewIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("Kiểm thử trọn vẹn luồng Đánh giá phim (TODO 31 / Issue #18): An xem rồi được đánh giá & sửa; Bình chưa xem không được đánh giá")
    void testMovieReviewFlow() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Customer an = customerRepository.findAll().stream()
                .filter(c -> c.getEmail().equals("an.nv@gmail.com"))
                .findFirst().orElseThrow();
        Customer binh = customerRepository.findAll().stream()
                .filter(c -> c.getEmail().equals("binh.tt@gmail.com"))
                .findFirst().orElseThrow();
        Movie bietDoi = movieRepository.findAll().stream()
                .filter(m -> m.getTitle().equals("Biệt Đội Siêu Tốc"))
                .findFirst().orElseThrow();

        Long movieId = bietDoi.getMovieId();

        // 1. An mở Biệt Đội Siêu Tốc -> Có form đánh giá (canReview = true)
        MockHttpSession sessionAn = new MockHttpSession();
        sessionAn.setAttribute(SessionUtils.CURRENT_USER,
                new SessionUser(an.getAccount().getAccountId(), an.getCustomerId(), an.getFullName(), Role.CUSTOMER));

        mockMvc.perform(get("/movies/" + movieId).session(sessionAn))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canReview", true))
                .andExpect(model().attributeExists("reviewForm"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Đánh giá của khán giả")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Gửi đánh giá")));

        // 2. An gửi 5 sao -> Thành công, điểm 5.0 (1 lượt)
        mockMvc.perform(post("/customer/movies/" + movieId + "/review")
                        .session(sessionAn)
                        .param("rating", "5")
                        .param("comment", "Phim xem cực kỳ mãn nhãn!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/" + movieId))
                .andExpect(flash().attribute("success", "Cảm ơn bạn đã đánh giá!"));

        Review reviewAfterStep2 = reviewRepository.findById(new ReviewId(an.getCustomerId(), movieId)).orElseThrow();
        assertEquals(5, reviewAfterStep2.getRating());
        assertEquals("Phim xem cực kỳ mãn nhãn!", reviewAfterStep2.getComment());
        assertEquals(1, reviewRepository.findByMovie_MovieIdOrderByReviewDateDesc(movieId).size());

        mockMvc.perform(get("/movies/" + movieId).session(sessionAn))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("5.0/5 ⭐ (1 lượt)")));

        // 3. An gửi lại 4 sao -> Vẫn 1 lượt, điểm 4.0 (sửa, không thêm mới do khoá kép)
        mockMvc.perform(post("/customer/movies/" + movieId + "/review")
                        .session(sessionAn)
                        .param("rating", "4")
                        .param("comment", "Xem lại đoạn kết chưa ưng lắm, cho 4 sao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/" + movieId))
                .andExpect(flash().attribute("success", "Cảm ơn bạn đã đánh giá!"));

        Review reviewAfterStep3 = reviewRepository.findById(new ReviewId(an.getCustomerId(), movieId)).orElseThrow();
        assertEquals(4, reviewAfterStep3.getRating());
        assertEquals("Xem lại đoạn kết chưa ưng lắm, cho 4 sao", reviewAfterStep3.getComment());
        assertEquals(1, reviewRepository.findByMovie_MovieIdOrderByReviewDateDesc(movieId).size());

        mockMvc.perform(get("/movies/" + movieId).session(sessionAn))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("4.0/5 ⭐ (1 lượt)")));

        // 4. Bình mở Biệt Đội Siêu Tốc -> Không có form (canReview = false)
        MockHttpSession sessionBinh = new MockHttpSession();
        sessionBinh.setAttribute(SessionUtils.CURRENT_USER,
                new SessionUser(binh.getAccount().getAccountId(), binh.getCustomerId(), binh.getFullName(), Role.CUSTOMER));

        mockMvc.perform(get("/movies/" + movieId).session(sessionBinh))
                .andExpect(status().isOk())
                .andExpect(model().attribute("canReview", false));

        // 5. Bình cố tình gửi đánh giá -> Chặn lại bởi BR-16 (BusinessException)
        mockMvc.perform(post("/customer/movies/" + movieId + "/review")
                        .session(sessionBinh)
                        .param("rating", "5")
                        .param("comment", "Tôi chưa xem nhưng muốn vote"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/" + movieId))
                .andExpect(flash().attribute("error", "Bạn chỉ có thể đánh giá phim đã xem"));
    }
}
