package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.GenreFacade;
import com.fu.cinema.desktop.model.GenreRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.dto.GenreForm;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class GenreController {

    private final GenreFacade facade;
    private final FxAsync fxAsync;
    private final FxValidator validator;

    @FXML private BorderPane root;
    @FXML private TableView<GenreRow> table;
    @FXML private TableColumn<GenreRow, String> colName;
    @FXML private TableColumn<GenreRow, String> colDesc;

    @FXML private VBox formBox;
    @FXML private Label lblTitle;
    @FXML private TextField tfName;
    @FXML private TextArea taDesc;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;

    private final ObservableList<GenreRow> rows = FXCollections.observableArrayList();
    private Long editingId; // null = thêm mới

    @FXML
    private void initialize() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().description()));
        table.setItems(rows);

        table.getSelectionModel().selectedItemProperty().addListener((o, a, row) -> fill(row));

        // Ràng buộc trạng thái nút
        btnDelete.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnSave.disableProperty().bind(tfName.textProperty().isEmpty());

        reload();
    }

    private void reload() {
        fxAsync.run(facade::findAll, rows::setAll, root);
    }

    private void fill(GenreRow row) {
        if (row == null) {
            onNew();
            return;
        }
        editingId = row.id();
        lblTitle.setText("Chỉnh sửa thể loại (#" + editingId + ")");
        tfName.setText(row.name());
        taDesc.setText(row.description());
        validator.mark(Map.of(), Map.of("genreName", tfName, "description", taDesc));
    }

    @FXML
    private void onRefresh() {
        reload();
    }

    @FXML
    private void onNew() {
        table.getSelectionModel().clearSelection();
        editingId = null;
        lblTitle.setText("Thêm thể loại mới");
        tfName.clear();
        taDesc.clear();
        validator.mark(Map.of(), Map.of("genreName", tfName, "description", taDesc));
    }

    @FXML
    private void onSave() {
        GenreForm form = new GenreForm();
        form.setGenreId(editingId);
        form.setGenreName(tfName.getText().trim());
        form.setDescription(taDesc.getText().trim());

        // Validate rỗng và độ dài bằng Bean Validation
        if (!validator.mark(validator.validate(form), Map.of("genreName", tfName, "description", taDesc))) {
            return;
        }

        // Truyền formBox làm busy node (không truyền btnSave vì đã bị bind)
        fxAsync.runVoid(() -> facade.save(form), () -> {
            AlertUtil.info("Đã lưu thể loại");
            onNew();
            reload();
        }, formBox);
    }

    @FXML
    private void onDelete() {
        GenreRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) return;

        if (AlertUtil.confirm("Xoá thể loại \"" + row.name() + "\"?")) {
            fxAsync.runVoid(() -> facade.delete(row.id()), () -> {
                AlertUtil.info("Đã xoá");
                onNew();
                reload();
            }, null);
        }
    }
}