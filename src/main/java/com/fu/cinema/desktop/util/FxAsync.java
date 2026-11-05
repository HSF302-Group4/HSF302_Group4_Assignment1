package com.fu.cinema.desktop.util;

import javafx.concurrent.Task;
import javafx.scene.Node;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Tiện ích thực thi các tác vụ bất đồng bộ trên Virtual Thread.
 * Đảm bảo các callback thành công và thất bại luôn được chuyển tiếp về JavaFX Application Thread.
 */
@Component
public class FxAsync implements DisposableBean {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess) {
        return run(work, onSuccess, AlertUtil::showError, null);
    }

    public <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess, Node busy) {
        return run(work, onSuccess, AlertUtil::showError, busy);
    }

    /**
     * Thực thi tác vụ bất đồng bộ và xử lý callback trên JavaFX Application Thread.
     *
     * @param work      công việc cần xử lý ở background (Virtual Thread) — không can thiệp UI trực tiếp tại đây
     * @param onSuccess callback khi thành công (thực thi trên JavaFX Application Thread)
     * @param onError   callback khi có lỗi (thực thi trên JavaFX Application Thread)
     * @param busy      Node giao diện cần vô hiệu hóa (disable) trong quá trình xử lý, có thể null
     * @param <T>       kiểu dữ liệu kết quả trả về
     * @return Task<T> đang thực thi
     */
    public <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError, Node busy) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call(); // Thực thi trên Virtual Thread
            }
        };

        if (busy != null) {
            busy.setDisable(true);
        }

        task.setOnSucceeded(e -> { // Thực thi trên JavaFX Application Thread
            if (busy != null) {
                busy.setDisable(false);
            }
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });

        task.setOnFailed(e -> { // Thực thi trên JavaFX Application Thread
            if (busy != null) {
                busy.setDisable(false);
            }
            Consumer<Throwable> errHandler = onError != null ? onError : AlertUtil::showError;
            errHandler.accept(task.getException());
        });

        executor.submit(task);
        return task;
    }

    /** Cho các thao tác không trả kết quả (xóa, xác nhận, lưu không cần return...). */
    public Task<Void> runVoid(Runnable work, Runnable onSuccess) {
        return runVoid(work, onSuccess, null);
    }

    public Task<Void> runVoid(Runnable work, Runnable onSuccess, Node busy) {
        return run(() -> {
            work.run();
            return null;
        }, v -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        }, AlertUtil::showError, busy);
    }

    public Task<Void> runVoid(Runnable work, Runnable onSuccess, Consumer<Throwable> onError, Node busy) {
        return run(() -> {
            work.run();
            return null;
        }, v -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        }, onError, busy);
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
