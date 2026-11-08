package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.MovieFacade;
import com.fu.cinema.desktop.model.GenreItem;
import com.fu.cinema.desktop.model.MovieRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.enums.AgeRating;
import com.fu.cinema.enums.MovieStatus;
import com.fu.cinema.navigation.SceneManager;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MovieController {

    private static final PseudoClass STOPPED = PseudoClass.getPseudoClass("stopped");

    private final MovieFacade facade;
    private final FxAsync fxAsync;
    private final SceneManager sceneManager;

    @FXML private BorderPane root;
    @FXML private Label lblCount;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<GenreItem> cbGenre;
    @FXML private ComboBox<MovieStatus> cbStatus;
    @FXML private ComboBox<SortItem> cbSort;

    @FXML private TableView<MovieRow> table;
    @FXML private TableColumn<MovieRow, Long> colId;
    @FXML private TableColumn<MovieRow, String> colTitle;
    @FXML private TableColumn<MovieRow, String> colGenre;
    @FXML private TableColumn<MovieRow, Integer> colDuration;
    @FXML private TableColumn<MovieRow, LocalDate> colRelease;
    @FXML private TableColumn<MovieRow, AgeRating> colAgeRating;
    @FXML private TableColumn<MovieRow, MovieStatus> colStatus;

    @FXML private Pagination pagination;

    private final ObservableList<MovieRow> rows = FXCollections.observableArrayList();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));

    public record SortItem(String code, String label) {
        @Override
        public String toString() { return label; }
    }

    @FXML
    private void initialize() {
        initTableColumns();
        initRowBehavior();
        initFilters();

        pagination.currentPageIndexProperty().addListener((o, a, b) -> load(b.intValue()));

        // Tải danh mục thể loại và trang phim đầu tiên
        fxAsync.run(facade::genres, genres -> {
            List<GenreItem> list = new ArrayList<>();
            list.add(null); // null = "Tất cả"
            list.addAll(genres);
            cbGenre.getItems().setAll(list);
            cbGenre.getSelectionModel().select(0);
            load(0);
        }, root);
    }

    private void initTableColumns() {
        colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().id()));
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().title()));
        colGenre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().genreName()));

        // Cột thời lượng: setCellFactory
        colDuration.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().duration()));
        colDuration.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v + " phút");
            }
        });

        // Cột ngày khởi chiếu: setCellFactory
        colRelease.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().releaseDate()));
        colRelease.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        });

        colAgeRating.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().ageRating()));
        colStatus.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().status()));
        table.setItems(rows);
    }

    private void initRowBehavior() {
        table.setRowFactory(tv -> {
            TableRow<MovieRow> row = new TableRow<>() {
                @Override
                protected void updateItem(MovieRow m, boolean empty) {
                    super.updateItem(m, empty);
                    // Dòng STOPPED tô xám bằng PseudoClass
                    pseudoClassStateChanged(STOPPED, !empty && m != null && m.status() == MovieStatus.STOPPED);
                }
            };

            // Double click mở Sửa
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(row.getItem().id());
                }
            });

            // Context menu Sửa / Xoá
            MenuItem mnuEdit = new MenuItem("Sửa");
            mnuEdit.setOnAction(e -> openDialog(row.getItem().id()));

            MenuItem mnuDelete = new MenuItem("Xoá");
            mnuDelete.setOnAction(e -> onDelete(row.getItem()));

            ContextMenu cm = new ContextMenu(mnuEdit, mnuDelete);
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(cm)
            );

            return row;
        });
    }

    private void initFilters() {
        // ComboBox trạng thái (có "Tất cả")
        List<MovieStatus> statuses = new ArrayList<>();
        statuses.add(null);
        statuses.addAll(Arrays.asList(MovieStatus.values()));
        cbStatus.getItems().setAll(statuses);
        cbStatus.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MovieStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Tất cả trạng thái" : item.name()));
            }
        });
        cbStatus.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MovieStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Tất cả trạng thái" : item.name()));
            }
        });
        cbStatus.getSelectionModel().select(0);

        // ComboBox thể loại (hiển thị Tất cả khi null)
        cbGenre.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(GenreItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Tất cả thể loại" : item.name()));
            }
        });
        cbGenre.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(GenreItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Tất cả thể loại" : item.name()));
            }
        });

        // ComboBox sắp xếp
        cbSort.getItems().setAll(
                new SortItem("releaseDate", "Khởi chiếu mới nhất"),
                new SortItem("title", "Tên phim (A-Z)"),
                new SortItem("duration", "Thời lượng giảm dần")
        );
        cbSort.getSelectionModel().select(0);

        // Lắng nghe thay đổi bộ lọc
        debounce.setOnFinished(e -> search());
        tfSearch.textProperty().addListener((o, a, b) -> debounce.playFromStart());
        cbGenre.valueProperty().addListener((o, a, b) -> search());
        cbStatus.valueProperty().addListener((o, a, b) -> search());
        cbSort.valueProperty().addListener((o, a, b) -> search());
    }

    /** Đổi bộ lọc -> về trang 1 */
    private void search() {
        if (pagination.getCurrentPageIndex() != 0) {
            pagination.setCurrentPageIndex(0); // Listener sẽ tự gọi load(0)
        } else {
            load(0);
        }
    }

    private void load(int page) {
        String kw = tfSearch.getText();
        Long genreId = cbGenre.getValue() == null ? null : cbGenre.getValue().id();
        MovieStatus status = cbStatus.getValue();
        SortItem sortItem = cbSort.getValue();
        String sort = sortItem != null ? sortItem.code() : "releaseDate";

        fxAsync.run(() -> facade.search(kw, genreId, status, sort, page), result -> {
            rows.setAll(result.items());
            pagination.setPageCount(result.totalPages());
            lblCount.setText(result.totalElements() + " phim");
        }, root);
    }

    @FXML
    private void onRefresh() {
        load(pagination.getCurrentPageIndex());
    }

    @FXML
    private void onResetFilters() {
        tfSearch.clear();
        cbGenre.getSelectionModel().select(0);
        cbStatus.getSelectionModel().select(0);
        cbSort.getSelectionModel().select(0);
        search();
    }

    @FXML
    private void onAdd() {
        openDialog(null);
    }

    private void openDialog(Long movieId) {
        // Nơi mở Dialog thêm/sửa phim — sẽ hoàn thiện chi tiết ở TODO 40
        AlertUtil.info(movieId == null ? "Dialog thêm phim mới (sẽ hoàn thiện ở TODO 40)"
                : "Dialog sửa phim #" + movieId + " (sẽ hoàn thiện ở TODO 40)");
    }

    private void onDelete(MovieRow row) {
        if (row == null) return;
        if (AlertUtil.confirm("Xoá phim \"" + row.title() + "\"?")) {
            fxAsync.run(() -> facade.delete(row.id()), deleted -> {
                if (deleted) {
                    AlertUtil.info("Đã xoá phim thành công!");
                } else {
                    AlertUtil.info("Phim đã có suất chiếu: Đã chuyển sang trạng thái STOPPED (BR-06).");
                }
                load(pagination.getCurrentPageIndex());
            }, root);
        }
    }
}