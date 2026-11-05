package com.fu.cinema.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;


@Component
@RequiredArgsConstructor
public class SceneManager {

    private static final String CSS = "/desktop/css/desktop.css";

    private final ApplicationContext context;
    private Stage stage;
    private Scene scene;

    public void init(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(new StackPane(), 1200, 720);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(CSS)).toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(640);
        stage.show();
    }

    /** Đổi toàn bộ nội dung cửa sổ (Login ↔ Main). */
    public void switchTo(View view) {
        scene.setRoot(load(view).root());
        stage.setTitle(view.title());
    }

    /** Nạp màn hình con vào vùng center của khung chính. */
    public void showIn(BorderPane host, View view) {
        host.setCenter(load(view).root());
    }

    public <C> Loaded<C> load(View view) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(view.path()));
        loader.setControllerFactory(context::getBean);          // controller = Spring bean (prototype)
        try {
            Parent root = loader.load();
            return new Loaded<>(root, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("Không nạp được " + view.path(), e);
        }
    }

    public Stage getStage() { return stage; }

    public record Loaded<C>(Parent root, C controller) {}
}