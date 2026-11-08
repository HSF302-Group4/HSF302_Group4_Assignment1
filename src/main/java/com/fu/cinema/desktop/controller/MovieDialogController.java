package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.model.GenreItem;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.dto.MovieForm;
import com.fu.cinema.enums.AgeRating;
import com.fu.cinema.enums.MovieStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MovieDialogController {

    private final FxValidator validator;

    @FXML private GridPane root;
    @FXML private TextField tfTitle, tfDirector, tfPoster;
    @FXML private Spinner<Integer> spDuration;
    @FXML private DatePicker dpRelease;
    @FXML private ComboBox<AgeRating> cbAgeRating;
    @FXML private ComboBox<GenreItem> cbGenre;
    @FXML private ComboBox<MovieStatus> cbStatus;
    @FXML private TextArea taDescription;

    private Long movieId;

    @FXML
    private void initialize() {
        // Đặt min = 1 để Bean Validation (@Min(30), @Max(300)) là nơi báo lỗi -> đạt TC-D09
        spDuration.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 120));
        spDuration.setEditable(true);

        // Spinner editable không tự commit giá trị gõ tay khi mất focus
        spDuration.focusedProperty().addListener((o, was, is) -> {
            if (!is) commitSpinner();
        });

        cbAgeRating.getItems().setAll(AgeRating.values());
        cbStatus.getItems().setAll(MovieStatus.values());
    }

    public Optional<MovieForm> open(Window owner, MovieForm form, List<GenreItem> genres) {
        cbGenre.getItems().setAll(genres);
        fill(form);

        Dialog<MovieForm> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(form.getMovieId() == null ? "Thêm phim mới" : "Sửa phim (#" + form.getMovieId() + ")");
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());

        ButtonType save = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        Button btnSave = (Button) dialog.getDialogPane().lookupButton(save);
        btnSave.getStyleClass().add("primary");

        // Chặn đóng dialog khi dữ liệu không hợp lệ (Event Filter — Javafx_exercises 1.5)
        btnSave.addEventFilter(ActionEvent.ACTION, e -> {
            if (!validate()) {
                e.consume(); // Chặn không cho dialog đóng
            }
        });

        dialog.setResultConverter(bt -> bt == save ? toForm() : null);
        return dialog.showAndWait();
    }

    private boolean validate() {
        commitSpinner();
        MovieForm form = toForm();
        Map<String, String> errors = validator.validate(form);

        Map<String, Control> fields = new HashMap<>();
        fields.put("title", tfTitle);
        fields.put("director", tfDirector);
        fields.put("duration", spDuration);
        fields.put("releaseDate", dpRelease);
        fields.put("ageRating", cbAgeRating);
        fields.put("genreId", cbGenre);
        fields.put("status", cbStatus);
        fields.put("posterUrl", tfPoster);
        fields.put("description", taDescription);

        return validator.mark(errors, fields);
    }

    private MovieForm toForm() {
        MovieForm f = new MovieForm();
        f.setMovieId(movieId);
        f.setTitle(tfTitle.getText() != null ? tfTitle.getText().trim() : "");
        f.setDirector(tfDirector.getText() != null ? tfDirector.getText().trim() : "");
        f.setDuration(spDuration.getValue());
        f.setReleaseDate(dpRelease.getValue());
        f.setAgeRating(cbAgeRating.getValue());
        f.setGenreId(cbGenre.getValue() == null ? null : cbGenre.getValue().id());
        f.setStatus(cbStatus.getValue());
        f.setPosterUrl(tfPoster.getText() != null ? tfPoster.getText().trim() : "");
        f.setDescription(taDescription.getText() != null ? taDescription.getText().trim() : "");
        return f;
    }

    private void fill(MovieForm f) {
        if (f == null) {
            f = new MovieForm();
        }
        this.movieId = f.getMovieId();
        tfTitle.setText(f.getTitle() != null ? f.getTitle() : "");
        tfDirector.setText(f.getDirector() != null ? f.getDirector() : "");
        spDuration.getValueFactory().setValue(f.getDuration() != null ? f.getDuration() : 120);
        dpRelease.setValue(f.getReleaseDate() != null ? f.getReleaseDate() : LocalDate.now());
        cbAgeRating.setValue(f.getAgeRating() != null ? f.getAgeRating() : AgeRating.P);
        cbStatus.setValue(f.getStatus() != null ? f.getStatus() : MovieStatus.SHOWING);
        tfPoster.setText(f.getPosterUrl() != null ? f.getPosterUrl() : "");
        taDescription.setText(f.getDescription() != null ? f.getDescription() : "");

        if (f.getGenreId() != null) {
            cbGenre.getItems().stream()
                    .filter(g -> g != null && g.id().equals(f.getGenreId()))
                    .findFirst()
                    .ifPresent(cbGenre::setValue);
        } else if (!cbGenre.getItems().isEmpty()) {
            cbGenre.getSelectionModel().select(0);
        }
    }

    private void commitSpinner() {
        try {
            spDuration.getValueFactory().setValue(Integer.parseInt(spDuration.getEditor().getText().trim()));
        } catch (NumberFormatException ignored) {
            spDuration.getEditor().setText(String.valueOf(spDuration.getValue()));
        }
    }
}