package com.fu.cinema.dto;

import com.fu.cinema.enums.AgeRating;
import com.fu.cinema.enums.MovieStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MovieFormValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private MovieForm createValidForm() {
        MovieForm form = new MovieForm();
        form.setTitle("Dune: Hành Tinh Cát");
        form.setDirector("Denis Villeneuve");
        form.setDuration(155);
        form.setReleaseDate(LocalDate.now());
        form.setAgeRating(AgeRating.T16);
        form.setDescription("Một tác phẩm điện ảnh khoa học viễn tưởng hoành tráng.");
        form.setPosterUrl("https://example.com/poster.jpg");
        form.setGenreId(1L);
        form.setStatus(MovieStatus.SHOWING);
        return form;
    }

    @Test
    @DisplayName("Thời lượng 10 phút -> vi phạm validate 'Thời lượng tối thiểu 30 phút'")
    void testDurationLessThan30() {
        MovieForm form = createValidForm();
        form.setDuration(10);

        Set<ConstraintViolation<MovieForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Thời lượng tối thiểu 30 phút")));
    }

    @Test
    @DisplayName("Không chọn thể loại -> vi phạm validate 'Vui lòng chọn thể loại'")
    void testMissingGenreId() {
        MovieForm form = createValidForm();
        form.setGenreId(null);

        Set<ConstraintViolation<MovieForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Vui lòng chọn thể loại")));
    }

    @Test
    @DisplayName("Dữ liệu hợp lệ -> không có lỗi validate")
    void testValidForm() {
        MovieForm form = createValidForm();
        Set<ConstraintViolation<MovieForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }
}
