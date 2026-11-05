package com.fu.cinema.desktop.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Tiện ích tích hợp Bean Validation với giao diện JavaFX. */
@Component
@RequiredArgsConstructor
public class FxValidator {

    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private final Validator validator; // jakarta.validation.Validator do Spring cấu hình

    /**
     * Kiểm tra dữ liệu đối tượng form theo Bean Validation.
     *
     * @param form đối tượng DTO chứa các annotation validation
     * @return map tên thuộc tính (field) -> thông báo lỗi (rỗng nếu hợp lệ)
     */
    public Map<String, String> validate(Object form) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (form == null || validator == null) {
            return errors;
        }
        Set<ConstraintViolation<Object>> violations = validator.validate(form);
        for (ConstraintViolation<Object> v : violations) {
            errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage());
        }
        return errors;
    }

    /**
     * Đánh dấu ô nhập liệu: tô đỏ (pseudo-class error) và thêm tooltip hiển thị message lỗi.
     * Xóa trạng thái lỗi nếu ô nhập liệu hợp lệ.
     *
     * @param errors map lỗi trả về từ {@link #validate(Object)}
     * @param fields map tên field -> Control giao diện tương ứng
     * @return true nếu không có lỗi, false nếu có ít nhất 1 trường không hợp lệ
     */
    public boolean mark(Map<String, String> errors, Map<String, Control> fields) {
        if (fields != null) {
            fields.forEach((name, control) -> {
                if (control != null) {
                    String msg = (errors != null) ? errors.get(name) : null;
                    control.pseudoClassStateChanged(ERROR, msg != null);
                    control.setTooltip(msg == null ? null : new Tooltip(msg));
                }
            });
        }
        return errors == null || errors.isEmpty();
    }
}
