package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.ReportFacade;
import com.fu.cinema.desktop.model.ReportBookingRow;
import com.fu.cinema.desktop.model.ReportData;
import com.fu.cinema.desktop.model.RevenueRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.CsvExporter;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.MoneyUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller màn hình Báo cáo doanh thu & Xuất CSV (TODO 45).
 * - BarChart: doanh thu theo phim, tooltip "Phim: x vé – y đ."
 * - PieChart: tỷ trọng số vé theo phim
 * - TableView: đơn CONFIRMED, sắp xếp theo ngày giảm dần
 * - Xuất CSV UTF-8 BOM (Excel đọc đúng tiếng Việt)
 */
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ReportController {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final ReportFacade facade;
    private final FxAsync fxAsync;

    // ===== FXML fields =====
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private Button btnGenerate;
    @FXML private Button btnExport;
    @FXML private Label lblEmpty;
    @FXML private Label lblTotals;

    @FXML private BarChart<String, Number> chartRevenue;
    @FXML private PieChart pieTickets;

    @FXML private TableView<ReportBookingRow> tblBookings;
    @FXML private TableColumn<ReportBookingRow, Long> colId;
    @FXML private TableColumn<ReportBookingRow, String> colDate;
    @FXML private TableColumn<ReportBookingRow, String> colCustomer;
    @FXML private TableColumn<ReportBookingRow, String> colMovie;
    @FXML private TableColumn<ReportBookingRow, String> colTotal;

    // ===== State =====
    private final ObjectProperty<ReportData> data = new SimpleObjectProperty<>();
    private final ObservableList<ReportBookingRow> bookingRows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Mặc định: từ đầu tháng đến hôm nay
        dpStart.setValue(LocalDate.now().withDayOfMonth(1));
        dpEnd.setValue(LocalDate.now());

        // Chặn trường hợp End < Start
        dpEnd.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || (dpStart.getValue() != null && d.isBefore(dpStart.getValue())));
            }
        });

        // Khi thay đổi Start, nếu End < Start thì cập nhật End = Start
        dpStart.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpEnd.getValue() != null && dpEnd.getValue().isBefore(newVal)) {
                dpEnd.setValue(newVal);
            }
        });

        // Tắt animation chart để node hiển thị ngay cho Tooltip
        chartRevenue.setAnimated(false);
        pieTickets.setAnimated(false);

        // Disable nút Xuất CSV khi không có dữ liệu hoặc dữ liệu rỗng
        btnExport.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> data.get() == null || data.get().isEmpty(),
                        data
                )
        );

        // Ban đầu hiển thị thông báo chưa có dữ liệu
        lblEmpty.setVisible(true);
        lblTotals.setText("");

        setupTableColumns();
        tblBookings.setItems(bookingRows);
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().id()));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().bookingDate() != null ? c.getValue().bookingDate().format(DATETIME_FMT) : ""));
        colCustomer.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().customerName()));
        colMovie.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().movieTitle()));
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                MoneyUtil.format(c.getValue().total())));
    }

    // ===== Actions =====

    @FXML
    private void onGenerate() {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        fxAsync.run(
                () -> facade.generate(start, end),
                this::render,
                btnGenerate
        );
    }

    private void render(ReportData d) {
        data.set(d);

        boolean empty = d.isEmpty();
        lblEmpty.setVisible(empty);

        if (empty) {
            bookingRows.clear();
            chartRevenue.getData().clear();
            pieTickets.getData().clear();
            lblTotals.setText("");
            return;
        }

        // 1. Cập nhật bảng đơn hàng
        bookingRows.setAll(d.bookings());

        // 2. Cập nhật dòng tổng kết
        lblTotals.setText("Tổng: %d đơn  •  %d vé  •  %s"
                .formatted(d.totalBookings(), d.totalTickets(), MoneyUtil.format(d.totalRevenue())));

        // 3. BarChart doanh thu theo phim
        chartRevenue.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        for (RevenueRow r : d.revenue()) {
            series.getData().add(new XYChart.Data<>(r.title(), r.revenue()));
        }
        chartRevenue.getData().setAll(List.of(series));

        // Tooltip cho từng cột: "Phim: x vé – y đ."
        for (int i = 0; i < series.getData().size(); i++) {
            RevenueRow r = d.revenue().get(i);
            javafx.scene.Node node = series.getData().get(i).getNode();
            if (node != null) {
                Tooltip.install(node, new Tooltip(
                        "%s: %d vé – %s".formatted(r.title(), r.tickets(), MoneyUtil.format(r.revenue()))
                ));
            }
        }

        // 4. PieChart tỷ trọng số vé
        pieTickets.getData().clear();
        List<PieChart.Data> pieData = d.revenue().stream()
                .map(r -> new PieChart.Data(r.title() + " (" + r.tickets() + ")", r.tickets()))
                .toList();
        pieTickets.getData().setAll(pieData);
    }

    @FXML
    private void onExport() {
        ReportData snapshot = data.get();
        if (snapshot == null || snapshot.isEmpty()) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu báo cáo CSV");
        fc.setInitialFileName("doanh-thu_%s_%s.csv".formatted(snapshot.start(), snapshot.end()));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = fc.showSaveDialog(btnExport.getScene().getWindow());
        if (file == null) {
            return; // Người dùng nhấn Cancel: không xuất file và không hiển thị lỗi
        }

        fxAsync.runVoid(
                () -> CsvExporter.write(file.toPath(), snapshot),
                () -> AlertUtil.info("Đã xuất báo cáo thành công: " + file.getName()),
                btnGenerate
        );
    }
}
