package com.fu.cinema.desktop;

import javafx.application.Application;

/** Entry point desktop. KHÔNG extends Application → tránh "JavaFX runtime components are missing". */
public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");   // phải đặt TRƯỚC khi Spring chạy
        Application.launch(CinemaFxApp.class, args);
    }
}