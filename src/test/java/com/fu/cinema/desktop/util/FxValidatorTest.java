package com.fu.cinema.desktop.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FxValidatorTest {

    private FxValidator fxValidator;

    static class SampleForm {
        @NotBlank(message = "Tên không được để trống")
        @Size(min = 2, max = 50, message = "Tên phải từ 2 đến 50 ký tự")
        private String name;

        public SampleForm(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        fxValidator = new FxValidator(validator);
    }

    @Test
    @DisplayName("Validate phát hiện lỗi và trả về map lỗi theo tên field")
    void validate_hasErrors() {
        SampleForm invalidForm = new SampleForm("");

        Map<String, String> errors = fxValidator.validate(invalidForm);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("name"));
        assertNotNull(errors.get("name"));
    }

    @Test
    @DisplayName("Validate trả về map rỗng khi dữ liệu hợp lệ")
    void validate_validForm() {
        SampleForm validForm = new SampleForm("Hành động");

        Map<String, String> errors = fxValidator.validate(validForm);

        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("mark trả về true khi không có lỗi")
    void mark_noErrorsReturnsTrue() {
        boolean ok = fxValidator.mark(Map.of(), Map.of());
        assertTrue(ok);
    }
}
