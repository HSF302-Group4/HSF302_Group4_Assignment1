package com.fu.cinema.desktop.util;

import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.exception.ResourceNotFoundException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/** Tiện ích hiển thị thông báo và phân loại lỗi bằng Switch Pattern Matching. */
@Slf4j
public final class AlertUtil {

    private AlertUtil() {
    }

    public static Throwable unwrap(Throwable ex) {
        if (ex == null) {
            return null;
        }
        Throwable cause = (ex instanceof CompletionException || ex instanceof ExecutionException) && ex.getCause() != null
                ? ex.getCause()
                : ex;
        return cause;
    }

    public static String resolveErrorMessage(Throwable ex) {
        Throwable cause = unwrap(ex);
        return switch (cause) {
            case null -> "Lỗi không xác định.";
            case BusinessException be -> be.getMessage();
            case ResourceNotFoundException nf -> nf.getMessage();
            case CannotCreateTransactionException cc -> "Không kết nối được cơ sở dữ liệu. Kiểm tra SQL Server.";
            default -> {
                log.error("Lỗi không mong muốn", cause); // Stack trace chỉ ghi vào log, không hiển thị cho người dùng
                yield "Lỗi hệ thống, vui lòng thử lại.";
            }
        };
    }

    public static void showError(Throwable ex) {
        Throwable cause = unwrap(ex);
        Alert.AlertType type = Alert.AlertType.ERROR;
        String message = switch (cause) { // Java 21 Switch Pattern Matching
            case null -> "Lỗi không xác định.";
            case BusinessException be -> {
                type = Alert.AlertType.WARNING;
                yield be.getMessage();
            }
            case ResourceNotFoundException nf -> nf.getMessage();
            case CannotCreateTransactionException cc -> "Không kết nối được cơ sở dữ liệu. Kiểm tra SQL Server.";
            default -> {
                log.error("Lỗi không mong muốn", cause);
                yield "Lỗi hệ thống, vui lòng thử lại.";
            }
        };
        show(type, type == Alert.AlertType.WARNING ? "Không thực hiện được" : "Lỗi", message);
    }

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "Thông báo", message);
    }

    public static void warn(String message) {
        show(Alert.AlertType.WARNING, "Cảnh báo", message);
    }

    public static boolean confirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Runnable action = () -> {
            Alert a = new Alert(type, message, ButtonType.OK);
            a.setTitle(title);
            a.setHeaderText(null);
            a.showAndWait();
        };
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
