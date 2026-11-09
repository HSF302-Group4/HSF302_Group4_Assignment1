package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.ShowtimeFacade;
import com.fu.cinema.desktop.model.MovieItem;
import com.fu.cinema.desktop.model.RoomItem;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.dto.ShowtimeForm;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ScheduleController {

    private final ShowtimeFacade facade;
    private final FxValidator validator;
    private final FxAsync fxAsync;

    @FXML private ComboBox<MovieItem> cbMovie;
    @FXML private ComboBox<RoomItem> cbRoom;
    @FXML private DatePicker dpDate;
    @FXML private Spinner<Integer> spHour, spMinute;
    @FXML private TextField tfPrice;
    @FXML private Label lblEnd;
    @FXML private Pane formBox;

    private Long editingId;

    @FXML
    private void initialize() {
        spHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 19));
        spMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5));
        tfPrice.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d{0,9}") ? c : null));
        dpDate.setDayCellFactory(p -> new DateCell() {                     // không cho chọn ngày quá khứ
            @Override public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || d.isBefore(LocalDate.now()));
            }
        });
        lblEnd.textProperty().bind(Bindings.createStringBinding(() -> {
            LocalDateTime end = suggestedEnd();
            return end == null ? "—" : "Kết thúc (gợi ý): " + end.format(DateTimeFormatter.ofPattern("HH:mm dd/MM"));
        }, cbMovie.valueProperty(), dpDate.valueProperty(), spHour.valueProperty(), spMinute.valueProperty()));

        loadCombos();
    }

    private void loadCombos() {
        if (facade != null) {
            fxAsync.run(facade::schedulableMovies, list -> cbMovie.getItems().setAll(list));
            fxAsync.run(facade::activeRooms, list -> cbRoom.getItems().setAll(list));
        }
    }

    private LocalDateTime startTime() {
        return dpDate.getValue() == null ? null : LocalDateTime.of(dpDate.getValue(), LocalTime.of(spHour.getValue(), spMinute.getValue()));
    }

    private LocalDateTime suggestedEnd() {                                  // Start + Duration + 15' dọn phòng
        LocalDateTime start = startTime();
        return start == null || cbMovie.getValue() == null ? null : start.plusMinutes(cbMovie.getValue().duration() + 15L);
    }

    @FXML
    private void onSaveShowtime() {
        ShowtimeForm f = new ShowtimeForm();
        f.setShowtimeId(editingId);
        f.setMovieId(cbMovie.getValue() == null ? null : cbMovie.getValue().id());
        f.setRoomId(cbRoom.getValue() == null ? null : cbRoom.getValue().id());
        f.setStartTime(startTime());
        f.setEndTime(suggestedEnd());               // hoặc null → service tự tính
        f.setTicketPrice(tfPrice.getText().isBlank() ? null : new BigDecimal(tfPrice.getText()));
        if (!validator.mark(validator.validate(f), Map.of("movieId", cbMovie, "roomId", cbRoom,
                "startTime", dpDate, "ticketPrice", tfPrice))) return;
        fxAsync.runVoid(() -> facade.save(f), () -> { AlertUtil.info("Đã lưu suất chiếu"); reload(); }, formBox);
    }

    private void reload() {
        editingId = null;
        tfPrice.clear();
        loadCombos();
    }
}
