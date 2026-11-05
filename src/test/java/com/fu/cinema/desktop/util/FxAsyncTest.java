package com.fu.cinema.desktop.util;

import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class FxAsyncTest {

    private FxAsync fxAsync;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @BeforeEach
    void setUp() {
        fxAsync = new FxAsync();
    }

    @AfterEach
    void tearDown() {
        fxAsync.destroy();
    }

    @Test
    @DisplayName("FxAsync thực thi công việc trên Virtual Thread")
    void run_executesOnVirtualThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean isVirtual = new AtomicBoolean(false);
        AtomicReference<String> resultRef = new AtomicReference<>();

        fxAsync.run(() -> {
            isVirtual.set(Thread.currentThread().isVirtual());
            return "SUCCESS";
        }, res -> {
            resultRef.set(res);
            latch.countDown();
        });

        boolean finished = latch.await(3, TimeUnit.SECONDS);
        assertTrue(finished, "Tác vụ phải hoàn thành trong thời gian cho phép");
        assertTrue(isVirtual.get(), "Tác vụ của FxAsync phải chạy trên Virtual Thread");
        assertEquals("SUCCESS", resultRef.get());
    }

    @Test
    @DisplayName("FxAsync runVoid thực thi Runnable trên Virtual Thread")
    void runVoid_executesOnVirtualThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        fxAsync.runVoid(() -> {
            executed.set(Thread.currentThread().isVirtual());
        }, latch::countDown);

        boolean finished = latch.await(3, TimeUnit.SECONDS);
        assertTrue(finished);
        assertTrue(executed.get());
    }
}
