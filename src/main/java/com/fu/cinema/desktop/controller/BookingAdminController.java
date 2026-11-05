package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.BookingFacade;
import com.fu.cinema.desktop.model.BookingDetail;
import com.fu.cinema.desktop.model.BookingRow;
import com.fu.cinema.desktop.model.PageResult;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.MoneyUtil;
import com.fu.cinema.enums.BookingStatus;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class BookingAdminController {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final BookingFacade facade;
    private final FxAsync fxAsync;

    @FXML private ComboBox<BookingStatus> cbStatus;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private TextField tfKeyword;

    @FXML private TableView<BookingRow> table;
    @FXML private TableColumn<BookingRow, Long> colId;
    @FXML private TableColumn<BookingRow, String> colCustomer;
    @FXML private TableColumn<BookingRow, String> colMovie;
    @FXML private TableColumn<BookingRow, String> colRoom;
    @FXML private TableColumn<BookingRow, String> colStart;
    @FXML private TableColumn<BookingRow, Integer> colSeats;
    @FXML private TableColumn<BookingRow, String> colTotal;
    @FXML private TableColumn<BookingRow, BookingStatus> colStatus;
    @FXML private TableColumn<BookingRow, String> colDate;

    @FXML private Pagination pagination;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    @FXML private Button btnRefresh;

    private final ObservableList<BookingRow> rows = FXCollections.observableArrayList();
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    @FXML
    private void initialize() {
        initFilters();
        initTableColumns();
        initRowDoubleClick();
        initActionButtons();

        pagination.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> loadPage(newIdx.intValue()));
        loadPage(0);
    }

    private void initFilters() {
        List<BookingStatus> statusList = new ArrayList<>();
        statusList.add(null); // "Tất cả"
        statusList.addAll(List.of(BookingStatus.values()));
        cbStatus.setItems(FXCollections.observableArrayList(statusList));
        cbStatus.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BookingStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Tất cả" : item.name()));
            }
        });
        cbStatus.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(BookingStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? "Tất cả" : item.name()));
            }
        });
        cbStatus.getSelectionModel().select(0);
    }

    private void initTableColumns() {
        table.setItems(rows);

        colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().id()));
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().customerName()));
        colMovie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().movieTitle()));
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().roomName()));
        colStart.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().startTime() != null ? c.getValue().startTime().format(DATETIME_FMT) : ""
        ));
        colSeats.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().seatCount()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(MoneyUtil.format(c.getValue().total())));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().bookingDate() != null ? c.getValue().bookingDate().format(DATETIME_FMT) : ""
        ));

        // Tô màu trạng thái đơn hàng: PENDING (#b45309), CONFIRMED (#15803d), CANCELLED (#b91c1c)
        colStatus.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().status()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BookingStatus st, boolean empty) {
                super.updateItem(st, empty);
                getStyleClass().removeAll("st-pending", "st-confirmed", "st-cancelled");
                if (empty || st == null) {
                    setText(null);
                } else {
                    setText(st.name());
                    getStyleClass().add("st-" + st.name().toLowerCase());
                }
            }
        });
    }

    private void initRowDoubleClick() {
        table.setRowFactory(tv -> {
            TableRow<BookingRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    BookingRow rowData = row.getItem();
                    if (rowData != null) {
                        onViewDetail(rowData.id());
                    }
                }
            });
            return row;
        });
    }

    private void initActionButtons() {
        ReadOnlyObjectProperty<BookingRow> sel = table.getSelectionModel().selectedItemProperty();

        // Nút Xác nhận chỉ bật khi chọn đơn PENDING và không đang xử lý
        btnConfirm.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sel.get() == null || sel.get().status() != BookingStatus.PENDING || busy.get(),
                sel, busy
        ));

        // Nút Huỷ chỉ bật khi đơn chưa bị CANCELLED và không đang xử lý
        btnCancel.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sel.get() == null || sel.get().status() == BookingStatus.CANCELLED || busy.get(),
                sel, busy
        ));
    }

    @FXML
    private void onSearch() {
        if (pagination.getCurrentPageIndex() == 0) {
            loadPage(0);
        } else {
            pagination.setCurrentPageIndex(0);
        }
    }

    @FXML
    private void onResetFilters() {
        cbStatus.getSelectionModel().select(0);
        dpFrom.setValue(null);
        dpTo.setValue(null);
        tfKeyword.clear();
        onSearch();
    }

    @FXML
    private void onRefresh() {
        loadPage(pagination.getCurrentPageIndex());
    }

    private void loadPage(int page) {
        BookingStatus status = cbStatus.getValue();
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();
        String keyword = tfKeyword.getText() != null ? tfKeyword.getText().trim() : "";

        busy.set(true);
        fxAsync.run(() -> facade.search(status, from, to, keyword, page), (PageResult<BookingRow> result) -> {
            busy.set(false);
            rows.setAll(result.items());
            int totalPages = Math.max(1, result.totalPages());
            pagination.setPageCount(totalPages);
            if (page >= totalPages) {
                pagination.setCurrentPageIndex(totalPages - 1);
            }
        }, ex -> {
            busy.set(false);
            AlertUtil.showError(ex);
        }, null);
    }

    @FXML
    private void onConfirm() {
        BookingRow r = table.getSelectionModel().getSelectedItem();
        if (r == null || busy.get()) {
            return;
        }
        if (!AlertUtil.confirm("Xác nhận đơn #" + r.id() + "?")) {
            return;
        }
        busy.set(true);
        fxAsync.runVoid(() -> facade.confirm(r.id()), () -> {
            busy.set(false);
            AlertUtil.info("Đã xác nhận đơn #" + r.id());
            onRefresh();
        }, ex -> {
            busy.set(false);
            AlertUtil.showError(ex);
        }, null);
    }

    @FXML
    private void onCancel() {
        BookingRow r = table.getSelectionModel().getSelectedItem();
        if (r == null || busy.get()) {
            return;
        }
        if (!AlertUtil.confirm("Huỷ đơn #" + r.id() + "? Thao tác này không thể hoàn tác.")) {
            return;
        }
        busy.set(true);
        fxAsync.runVoid(() -> facade.cancel(r.id()), () -> {
            busy.set(false);
            AlertUtil.info("Đã huỷ đơn #" + r.id());
            onRefresh();
        }, ex -> {
            busy.set(false);
            AlertUtil.showError(ex);
        }, null);
    }

    private void onViewDetail(Long id) {
        fxAsync.run(() -> facade.detail(id), this::showDetailDialog);
    }

    private void showDetailDialog(BookingDetail d) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn hàng #" + d.id());
        dialog.setHeaderText("Thông tin chi tiết đơn hàng #" + d.id());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 16; -fx-font-size: 13px;");

        int row = 0;
        addDetailRow(grid, row++, "Mã đơn:", "#" + d.id());
        addDetailRow(grid, row++, "Khách hàng:", d.customerName() + (d.mobile() != null && !d.mobile().isBlank() ? " (" + d.mobile() + ")" : ""));
        addDetailRow(grid, row++, "Phim:", d.movieTitle());
        addDetailRow(grid, row++, "Phòng chiếu:", d.roomName());
        addDetailRow(grid, row++, "Suất chiếu:", d.startTime() != null ? d.startTime().format(DATETIME_FMT) : "");
        addDetailRow(grid, row++, "Danh sách ghế:", d.seats() != null ? String.join(", ", d.seats()) : "");
        addDetailRow(grid, row++, "Tổng tiền:", MoneyUtil.format(d.total()));
        addDetailRow(grid, row++, "Trạng thái:", d.status() != null ? d.status().name() : "");
        addDetailRow(grid, row++, "Ngày đặt:", d.bookingDate() != null ? d.bookingDate().format(DATETIME_FMT) : "");

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #111827;");
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }
}
