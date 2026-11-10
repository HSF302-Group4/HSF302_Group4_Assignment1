package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.session.DesktopSession;
import com.fu.cinema.navigation.SceneManager;
import com.fu.cinema.navigation.View;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MainController {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss  dd/MM/yyyy");

    private final SceneManager sceneManager;
    private final DesktopSession session;                      // TODO 36

    @FXML
    private BorderPane root;
    @FXML private Label lblUser;
    @FXML private Label lblClock;

    @FXML private Button btnDashboard;
    @FXML private Button btnCounterSale;
    @FXML private Button btnBooking;
    @FXML private Button btnGenre;
    @FXML private Button btnMovie;
    @FXML private Button btnSchedule;
    @FXML private Button btnReport;

    private Timeline clock;

    @FXML
    private void initialize() {
        lblUser.textProperty().bind(Bindings.concat("Nhân viên: ", session.displayNameProperty()));
        clock = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> lblClock.setText(LocalDateTime.now().format(CLOCK))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        onDashboard();
    }

    private void setActive(Button target) {
        Button[] buttons = {btnDashboard, btnCounterSale, btnBooking, btnGenre, btnMovie, btnSchedule, btnReport};
        for (Button b : buttons) {
            if (b != null) {
                b.getStyleClass().remove("primary");
            }
        }
        if (target != null && !target.getStyleClass().contains("primary")) {
            target.getStyleClass().add("primary");
        }
    }

    @FXML private void onDashboard()   { setActive(btnDashboard); sceneManager.showIn(root, View.DASHBOARD); }
    @FXML private void onCounterSale() { setActive(btnCounterSale); sceneManager.showIn(root, View.COUNTER_SALE); }
    @FXML private void onBooking()     { setActive(btnBooking); sceneManager.showIn(root, View.BOOKING); }
    @FXML private void onGenre()       { setActive(btnGenre); sceneManager.showIn(root, View.GENRE); }
    @FXML private void onMovie()       { setActive(btnMovie); sceneManager.showIn(root, View.MOVIE); }
    @FXML private void onSchedule()    { setActive(btnSchedule); sceneManager.showIn(root, View.SCHEDULE); }
    @FXML private void onReport()      { setActive(btnReport); sceneManager.showIn(root, View.REPORT); }

    @FXML
    private void onLogout() {
        clock.stop();
        session.logout();
        sceneManager.switchTo(View.LOGIN);
    }
}
