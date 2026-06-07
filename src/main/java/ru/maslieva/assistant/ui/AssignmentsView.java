package ru.maslieva.assistant.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.maslieva.assistant.model.Assignment;
import ru.maslieva.assistant.model.ProgrammingLanguage;
import ru.maslieva.assistant.service.AssignmentService;

public class AssignmentsView {
    private final AssignmentService assignmentService;
    private final VBox root = new VBox(16);
    private final TableView<Assignment> table = new TableView<>();

    private final TextField titleField = new TextField();
    private final ComboBox<ProgrammingLanguage> languageBox = new ComboBox<>();
    private final TextArea descriptionArea = new TextArea();
    private final TextArea testInputArea = new TextArea();
    private final TextArea expectedOutputArea = new TextArea();
    private final Spinner<Integer> maxScoreSpinner = new Spinner<>(1, 100, 10);

    public AssignmentsView(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
        build();
        refreshTable();
    }

    public VBox getRoot() {
        return root;
    }

    public void refresh() {
        refreshTable();
    }

    private void build() {
        root.getStyleClass().add("content-area");

        Label title = new Label("Задания");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Создание и управление заданиями для автоматической проверки");
        subtitle.getStyleClass().add("section-subtitle");

        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(16));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #dcdde1; -fx-border-radius: 8;");

        Label formTitle = new Label("Новое задание");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        languageBox.setItems(FXCollections.observableArrayList(ProgrammingLanguage.values()));
        languageBox.getSelectionModel().select(ProgrammingLanguage.JAVA);
        languageBox.setMaxWidth(Double.MAX_VALUE);

        descriptionArea.setPrefRowCount(2);
        testInputArea.setPrefRowCount(2);
        expectedOutputArea.setPrefRowCount(2);
        maxScoreSpinner.setEditable(true);

        int row = 0;
        addRow(grid, row++, "Название задания:", titleField);
        addRow(grid, row++, "Язык программирования:", languageBox);
        addRow(grid, row++, "Описание:", descriptionArea);
        addRow(grid, row++, "Тестовые входные данные:", testInputArea);
        addRow(grid, row++, "Ожидаемый вывод:", expectedOutputArea);
        addRow(grid, row, "Максимальный балл:", maxScoreSpinner);

        Button createBtn = new Button("Создать задание");
        createBtn.getStyleClass().add("primary-button");
        createBtn.setOnAction(e -> createAssignment());

        Button deleteBtn = new Button("Удалить выбранное");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteSelected());

        HBox buttons = new HBox(10, createBtn, deleteBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        formCard.getChildren().addAll(formTitle, grid, buttons);

        setupTable();

        VBox tableCard = new VBox(8);
        tableCard.setPadding(new Insets(16));
        tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #dcdde1; -fx-border-radius: 8;");
        Label tableTitle = new Label("Список заданий");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().addAll(tableTitle, table);

        root.getChildren().addAll(title, subtitle, formCard, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
    }

    private void addRow(GridPane grid, int row, String labelText, Control control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        grid.add(label, 0, row);
        grid.add(control, 1, row);
        GridPane.setHgrow(control, Priority.ALWAYS);
    }

    private void setupTable() {
        TableColumn<Assignment, String> titleCol = new TableColumn<>("Название");
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        titleCol.setPrefWidth(180);

        TableColumn<Assignment, String> langCol = new TableColumn<>("Язык");
        langCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getLanguage() != null ? data.getValue().getLanguage().getDisplayName() : ""));
        langCol.setPrefWidth(80);

        TableColumn<Assignment, String> descCol = new TableColumn<>("Описание");
        descCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        descCol.setPrefWidth(200);

        TableColumn<Assignment, String> scoreCol = new TableColumn<>("Макс. балл");
        scoreCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getMaxScore())));
        scoreCol.setPrefWidth(80);

        table.getColumns().addAll(titleCol, langCol, descCol, scoreCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void createAssignment() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert("Укажите название задания.");
            return;
        }
        ProgrammingLanguage language = languageBox.getValue();
        if (language == null) {
            showAlert("Выберите язык программирования.");
            return;
        }

        Assignment assignment = new Assignment(
                title,
                language,
                descriptionArea.getText().trim(),
                testInputArea.getText(),
                expectedOutputArea.getText(),
                maxScoreSpinner.getValue()
        );
        assignmentService.add(assignment);
        refreshTable();
        clearForm();
    }

    private void deleteSelected() {
        Assignment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите задание для удаления.");
            return;
        }
        assignmentService.remove(selected);
        refreshTable();
    }

    private void refreshTable() {
        table.setItems(FXCollections.observableArrayList(assignmentService.getAll()));
    }

    private void clearForm() {
        titleField.clear();
        languageBox.getSelectionModel().select(ProgrammingLanguage.JAVA);
        descriptionArea.clear();
        testInputArea.clear();
        expectedOutputArea.clear();
        maxScoreSpinner.getValueFactory().setValue(10);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
