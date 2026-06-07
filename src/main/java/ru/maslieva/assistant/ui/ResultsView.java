package ru.maslieva.assistant.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import ru.maslieva.assistant.model.CheckResult;
import ru.maslieva.assistant.service.ResultService;
import ru.maslieva.assistant.service.SettingsService;

import java.io.File;

public class ResultsView {
    private final ResultService resultService;
    private final SettingsService settingsService;
    private final VBox root = new VBox(16);
    private final TableView<CheckResult> table = new TableView<>();

    public ResultsView(ResultService resultService, SettingsService settingsService) {
        this.resultService = resultService;
        this.settingsService = settingsService;
        build();
        refresh();
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        table.setItems(FXCollections.observableArrayList(resultService.getAll()));
    }

    private void build() {
        root.getStyleClass().add("content-area");

        Label title = new Label("Результаты");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("История проверок и экспорт результатов");
        subtitle.getStyleClass().add("section-subtitle");

        setupTable();

        VBox tableCard = new VBox(8);
        tableCard.setPadding(new Insets(16));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #dcdde1; -fx-border-radius: 8;");
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().add(table);

        Button exportBtn = new Button("Сохранить в CSV");
        exportBtn.getStyleClass().add("primary-button");
        exportBtn.setOnAction(e -> exportCsv());

        Button clearBtn = new Button("Очистить результаты");
        clearBtn.getStyleClass().add("danger-button");
        clearBtn.setOnAction(e -> clearResults());

        HBox buttons = new HBox(10, exportBtn, clearBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, subtitle, buttons, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
    }

    private void setupTable() {
        TableColumn<CheckResult, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFormattedDate()));
        dateCol.setPrefWidth(140);

        TableColumn<CheckResult, String> nameCol = new TableColumn<>("ФИО");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStudentName()));
        nameCol.setPrefWidth(160);

        TableColumn<CheckResult, String> groupCol = new TableColumn<>("Группа");
        groupCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getGroup()));
        groupCol.setPrefWidth(80);

        TableColumn<CheckResult, String> assignmentCol = new TableColumn<>("Задание");
        assignmentCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getAssignmentTitle()));
        assignmentCol.setPrefWidth(140);

        TableColumn<CheckResult, String> langCol = new TableColumn<>("Язык");
        langCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getLanguage() != null ? d.getValue().getLanguage().getDisplayName() : ""));
        langCol.setPrefWidth(70);

        TableColumn<CheckResult, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getStatus() != null ? d.getValue().getStatus().getDisplayName() : ""));
        statusCol.setPrefWidth(140);

        TableColumn<CheckResult, String> scoreCol = new TableColumn<>("Балл");
        scoreCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getScore())));
        scoreCol.setPrefWidth(50);

        TableColumn<CheckResult, String> commentCol = new TableColumn<>("Комментарий");
        commentCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getComment()));
        commentCol.setPrefWidth(200);

        table.getColumns().addAll(dateCol, nameCol, groupCol, assignmentCol, langCol, statusCol, scoreCol, commentCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void exportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить результаты в CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV файлы", "*.csv"));
        chooser.setInitialFileName("results.csv");

        String configuredPath = settingsService.getSettings().getCsvExportPath();
        if (configuredPath != null && !configuredPath.isBlank()) {
            File file = new File(configuredPath);
            if (file.getParentFile() != null && file.getParentFile().isDirectory()) {
                chooser.setInitialDirectory(file.getParentFile());
                chooser.setInitialFileName(file.getName());
            }
        }

        File target = chooser.showSaveDialog(root.getScene().getWindow());
        if (target == null) {
            return;
        }
        try {
            resultService.exportTo(target.toPath());
            showInfo("Результаты сохранены в файл:\n" + target.getAbsolutePath());
        } catch (Exception e) {
            showError("Не удалось сохранить CSV: " + e.getMessage());
        }
    }

    private void clearResults() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить все результаты проверки?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                resultService.clearAll();
                refresh();
            }
        });
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
