package com.fu.cinema.desktop.util;

import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class AlertUtilTest {

    @Test
    @DisplayName("Switch pattern matching phân loại đúng BusinessException")
    void resolveErrorMessage_businessException() {
        BusinessException be = new BusinessException("Vé xem phim đã được đặt bởi người khác.");
        String msg = AlertUtil.resolveErrorMessage(be);
        assertEquals("Vé xem phim đã được đặt bởi người khác.", msg);
    }

    @Test
    @DisplayName("Switch pattern matching phân loại đúng ResourceNotFoundException")
    void resolveErrorMessage_resourceNotFoundException() {
        ResourceNotFoundException nf = new ResourceNotFoundException("Phòng chiếu", 99L);
        String msg = AlertUtil.resolveErrorMessage(nf);
        assertEquals("Phòng chiếu không tồn tại (id = 99)", msg);
    }

    @Test
    @DisplayName("Switch pattern matching phân loại đúng lỗi kết nối DB")
    void resolveErrorMessage_databaseConnectionError() {
        CannotCreateTransactionException cc = new CannotCreateTransactionException("Connection refused");
        String msg = AlertUtil.resolveErrorMessage(cc);
        assertEquals("Không kết nối được cơ sở dữ liệu. Kiểm tra SQL Server.", msg);
    }

    @Test
    @DisplayName("Bóc tách nguyên nhân từ CompletionException / ExecutionException")
    void resolveErrorMessage_wrappedException() {
        CompletionException ce = new CompletionException(new BusinessException("Lỗi bọc trong async"));
        String msg = AlertUtil.resolveErrorMessage(ce);
        assertEquals("Lỗi bọc trong async", msg);

        ExecutionException ee = new ExecutionException(new ResourceNotFoundException("Phim", 10L));
        String msg2 = AlertUtil.resolveErrorMessage(ee);
        assertEquals("Phim không tồn tại (id = 10)", msg2);
    }

    @Test
    @DisplayName("Lỗi không mong muốn ẩn stack trace và hiển thị thông báo thân thiện")
    void resolveErrorMessage_unknownException() {
        RuntimeException ex = new NullPointerException("Null pointer internal detail");
        String msg = AlertUtil.resolveErrorMessage(ex);
        assertEquals("Lỗi hệ thống, vui lòng thử lại.", msg);
        assertFalse(msg.contains("NullPointer"));
    }

    @Test
    @DisplayName("Xử lý exception null")
    void resolveErrorMessage_null() {
        String msg = AlertUtil.resolveErrorMessage(null);
        assertEquals("Lỗi không xác định.", msg);
    }
}
