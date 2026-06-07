package ru.maslieva.assistant.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import ru.maslieva.assistant.checker.CheckerService;
import ru.maslieva.assistant.model.Assignment;
import ru.maslieva.assistant.model.CheckResult;
import ru.maslieva.assistant.model.CheckStatus;
import ru.maslieva.assistant.model.ProgrammingLanguage;
import ru.maslieva.assistant.service.AssignmentService;
import ru.maslieva.assistant.service.SettingsService;

import java.io.File;

public class CheckView {
    private static final String EXTENSION_MISMATCH_WARNING =
            "Формат файла не соответствует выбранному языку задания. "
                    + "Проверка может быть выполнена только для Java (.java) или Haskell (.hs).";

    private final AssignmentService assignmentService;
    private final CheckerService checkerService;
    private final SettingsService settingsService;
    private final Runnable onResultSaved;

    private final VBox root = new VBox(16);
    private final ComboBox<Assignment> assignmentBox = new ComboBox<>();
    private final TextField studentNameField = new TextField();
    private final TextField groupField = new TextField();
    private final TextField filePathField = new TextField();
    private final Label statusLabel = new Label("—");
    private final TextArea commentArea = new TextArea();
    private final Label demoIndicator = new Label();

    private File selectedFile;

    public CheckView(AssignmentService assignmentService, CheckerService checkerService,
                     SettingsService settingsService, Runnable onResultSaved) {
        this.assignmentService = assignmentService;
        this.checkerService = checkerService;
        this.settingsService = settingsService;
        this.onResultSaved = onResultSaved;
        build();
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        assignmentBox.getItems().setAll(assignmentService.getAll());
        if (!assignmentBox.getItems().isEmpty()) {
            assignmentBox.getSelectionModel().selectFirst();
        }
        updateDemoIndicator();
    }

    public void updateDemoIndicator() {
        if (settingsService.isDemoMode()) {
            demoIndicator.setText("⚠ ДЕМОНСТРАЦИОННЫЙ РЕЖИМ — проверка симулируется");
            demoIndicator.getStyleClass().setAll("demo-banner");
            demoIndicator.setVisible(true);
            demoIndicator.setManaged(true);
        } else {
            demoIndicator.setVisible(false);
            demoIndicator.setManaged(false);
        }
    }

    private void build() {
        root.getStyleClass().add("content-area");

        Label title = new Label("Проверка");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Загрузка и автоматическая проверка решения студента");
        subtitle.getStyleClass().add("section-subtitle");

        demoIndicator.setMaxWidth(Double.MAX_VALUE);

        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(16));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #dcdde1; -fx-border-radius: 8;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        assignmentBox.setMaxWidth(Double.MAX_VALUE);
        filePathField.setEditable(false);
        filePathField.setPromptText("Файл не выбран");

        Button browseBtn = new Button("Выбрать файл...");
        browseBtn.getStyleClass().add("secondary-button");
        browseBtn.setOnAction(e -> chooseFile());

        HBox fileBox = new HBox(8, filePathField, browseBtn);
        HBox.setHgrow(filePathField, Priority.ALWAYS);

        int row = 0;
        addRow(grid, row++, "Задание:", assignmentBox);
        addRow(grid, row++, "ФИО студента:", studentNameField);
        addRow(grid, row++, "Группа:", groupField);
        addRow(grid, row, "Файл решения:", fileBox);

        Button checkBtn = new Button("Проверить");
        checkBtn.getStyleClass().add("primary-button");
        checkBtn.setOnAction(e -> runCheck());

        formCard.getChildren().addAll(grid, checkBtn);

        VBox resultCard = new VBox(10);
        resultCard.setPadding(new Insets(16));
        resultCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #dcdde1; -fx-border-radius: 8;");

        Label resultTitle = new Label("Результат проверки");
        resultTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label statusCaption = new Label("Статус:");
        statusCaption.getStyleClass().add("form-label");

        commentArea.setEditable(false);
        commentArea.setPrefRowCount(5);
        commentArea.setWrapText(true);
        commentArea.setPromptText("Комментарий появится после проверки");

        resultCard.getChildren().addAll(resultTitle, statusCaption, statusLabel,
                new Label("Комментарий:"), commentArea);

        root.getChildren().addAll(title, subtitle, demoIndicator, formCard, resultCard);
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        grid.add(label, 0, row);
        grid.add(control, 1, row);
        GridPane.setHgrow(control, Priority.ALWAYS);
    }

    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите файл решения");

        FileChooser.ExtensionFilter defaultFilter =
                new FileChooser.ExtensionFilter("Java и Haskell", "*.java", "*.hs");
        chooser.getExtensionFilters().addAll(
                defaultFilter,
                new FileChooser.ExtensionFilter("Java файлы", "*.java"),
                new FileChooser.ExtensionFilter("Haskell файлы", "*.hs"),
                new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );
        chooser.setSelectedExtensionFilter(defaultFilter);

        String solutionsPath = settingsService.getSettings().getSolutionsFolderPath();
        if (solutionsPath != null && !solutionsPath.isBlank()) {
            File dir = new File(solutionsPath);
            if (dir.isDirectory()) {
                chooser.setInitialDirectory(dir);
            }
        }

        File file = chooser.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            filePathField.setText(file.getAbsolutePath());
        }
    }

    private void runCheck() {
        Assignment assignment = assignmentBox.getValue();
        if (assignment == null) {
            showAlert("Создайте и выберите задание.");
            return;
        }
        String studentName = studentNameField.getText().trim();
        String group = groupField.getText().trim();
        if (studentName.isEmpty() || group.isEmpty()) {
            showAlert("Укажите ФИО студента и группу.");
            return;
        }
        if (selectedFile == null || !selectedFile.exists()) {
            showAlert("Выберите файл решения.");
            return;
        }
        if (!matchesAssignmentLanguage(selectedFile, assignment)) {
            showAlert(EXTENSION_MISMATCH_WARNING);
            return;
        }

        statusLabel.setText("Выполняется проверка...");
        statusLabel.getStyleClass().setAll();
        commentArea.setText("");

        new Thread(() -> {
            CheckResult result = checkerService.check(assignment, selectedFile, studentName, group);
            javafx.application.Platform.runLater(() -> {
                displayResult(result);
                if (onResultSaved != null) {
                    onResultSaved.run();
                }
            });
        }).start();
    }

    private void displayResult(CheckResult result) {
        CheckStatus status = result.getStatus();
        statusLabel.setText(status.getDisplayName() + "  |  Балл: " + result.getScore());
        statusLabel.getStyleClass().setAll();

        if (status == CheckStatus.SUCCESS) {
            statusLabel.getStyleClass().add("status-success");
        } else if (status == CheckStatus.WRONG_ANSWER || status == CheckStatus.TIME_LIMIT) {
            statusLabel.getStyleClass().add("status-warning");
        } else {
            statusLabel.getStyleClass().add("status-error");
        }

        commentArea.setText(result.getComment());
    }

    private boolean matchesAssignmentLanguage(File file, Assignment assignment) {
        String extension = getFileExtension(file);
        ProgrammingLanguage language = assignment.getLanguage();
        if (language == ProgrammingLanguage.JAVA) {
            return "java".equalsIgnoreCase(extension);
        }
        if (language == ProgrammingLanguage.HASKELL) {
            return "hs".equalsIgnoreCase(extension);
        }
        return false;
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
