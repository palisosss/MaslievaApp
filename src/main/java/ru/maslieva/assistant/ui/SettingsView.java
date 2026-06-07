package ru.maslieva.assistant.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ru.maslieva.assistant.model.AppSettings;
import ru.maslieva.assistant.service.SettingsService;

import java.io.File;

public class SettingsView {
    private final SettingsService settingsService;
    private final Runnable onDemoModeChanged;
    private final VBox root = new VBox(16);

    private final TextField emailField = new TextField();
    private final TextField solutionsPathField = new TextField();
    private final TextField csvPathField = new TextField();
    private final CheckBox demoModeCheck = new CheckBox("Включить демонстрационный режим");

    public SettingsView(SettingsService settingsService, Runnable onDemoModeChanged) {
        this.settingsService = settingsService;
        this.onDemoModeChanged = onDemoModeChanged;
        build();
        loadFromSettings();
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        loadFromSettings();
    }

    private void build() {
        root.getStyleClass().add("content-area");

        Label title = new Label("Настройки");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Параметры работы приложения и демонстрационные модули");
        subtitle.getStyleClass().add("section-subtitle");

        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(16));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #dcdde1; -fx-border-radius: 8;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        emailField.setPromptText("teacher@example.com");
        solutionsPathField.setEditable(false);
        csvPathField.setEditable(false);

        int row = 0;
        addRow(grid, row++, "Email преподавателя:", emailField);
        addRow(grid, row++, "Папка с решениями:", pathRow(solutionsPathField, true));
        addRow(grid, row, "Файл CSV для экспорта:", pathRow(csvPathField, false));

        demoModeCheck.setStyle("-fx-font-weight: bold;");

        Button saveBtn = new Button("Сохранить настройки");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setOnAction(e -> saveSettings());

        Button importBtn = new Button("Импорт из почты");
        importBtn.getStyleClass().add("secondary-button");
        importBtn.setOnAction(e -> showStubMessage(
                "Модуль импорта из почты",
                "Функция «Импорт из почты» реализована как демонстрационный модуль.\n" +
                        "В рабочей версии сюда можно подключить IMAP/OAuth без хранения паролей в приложении.\n" +
                        "Сейчас решения загружаются вручную через раздел «Проверка»."
        ));

        Button sheetsBtn = new Button("Экспорт в Google Sheets");
        sheetsBtn.getStyleClass().add("secondary-button");
        sheetsBtn.setOnAction(e -> showStubMessage(
                "Модуль экспорта в Google Sheets",
                "Функция «Экспорт в Google Sheets» реализована как демонстрационный модуль.\n" +
                        "Модуль можно подключить позже через Google Sheets API.\n" +
                        "Сейчас результаты экспортируются в CSV в разделе «Результаты»."
        ));

        HBox stubButtons = new HBox(10, importBtn, sheetsBtn);
        stubButtons.setAlignment(Pos.CENTER_LEFT);

        formCard.getChildren().addAll(grid, demoModeCheck, saveBtn, new Separator(), stubButtons);

        root.getChildren().addAll(title, subtitle, formCard);
    }

    private HBox pathRow(TextField field, boolean directory) {
        Button browse = new Button("Обзор...");
        browse.getStyleClass().add("secondary-button");
        browse.setOnAction(e -> {
            if (directory) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Выберите папку с решениями");
                File dir = chooser.showDialog(root.getScene().getWindow());
                if (dir != null) {
                    field.setText(dir.getAbsolutePath());
                }
            } else {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Выберите файл CSV");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
                chooser.setInitialFileName("results.csv");
                File file = chooser.showSaveDialog(root.getScene().getWindow());
                if (file != null) {
                    field.setText(file.getAbsolutePath());
                }
            }
        });
        HBox box = new HBox(8, field, browse);
        HBox.setHgrow(field, Priority.ALWAYS);
        return box;
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        grid.add(label, 0, row);
        grid.add(control, 1, row);
        GridPane.setHgrow(control, Priority.ALWAYS);
    }

    private void loadFromSettings() {
        AppSettings settings = settingsService.getSettings();
        emailField.setText(settings.getTeacherEmail());
        solutionsPathField.setText(settings.getSolutionsFolderPath());
        csvPathField.setText(settings.getCsvExportPath());
        demoModeCheck.setSelected(settings.isDemoMode());
    }

    private void saveSettings() {
        AppSettings settings = new AppSettings();
        settings.setTeacherEmail(emailField.getText().trim());
        settings.setSolutionsFolderPath(solutionsPathField.getText().trim());
        settings.setCsvExportPath(csvPathField.getText().trim());
        settings.setDemoMode(demoModeCheck.isSelected());
        settingsService.update(settings);

        if (onDemoModeChanged != null) {
            onDemoModeChanged.run();
        }

        showInfo("Настройки сохранены.");
    }

    private void showStubMessage(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
