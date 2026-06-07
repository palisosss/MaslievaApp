package ru.maslieva.assistant.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.maslieva.assistant.checker.CheckerService;
import ru.maslieva.assistant.service.AssignmentService;
import ru.maslieva.assistant.service.ResultService;
import ru.maslieva.assistant.service.SettingsService;

public class MainView {
    private final AssignmentService assignmentService;
    private final ResultService resultService;
    private final SettingsService settingsService;
    private final CheckerService checkerService;

    private final BorderPane root = new BorderPane();
    private final StackPane contentPane = new StackPane();
    private final Label demoBanner = new Label("ДЕМОНСТРАЦИОННЫЙ РЕЖИМ — проверка выполняется в симулированном режиме");

    private AssignmentsView assignmentsView;
    private CheckView checkView;
    private ResultsView resultsView;
    private SettingsView settingsView;
    private AboutView aboutView;

    private Button selectedNavButton;

    public MainView(AssignmentService assignmentService, ResultService resultService,
                    SettingsService settingsService, CheckerService checkerService) {
        this.assignmentService = assignmentService;
        this.resultService = resultService;
        this.settingsService = settingsService;
        this.checkerService = checkerService;
        buildViews();
        buildLayout();
        showSection("assignments");
    }

    public void show(Stage stage) {
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        stage.setTitle("Ассистент преподавателя");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.show();
    }

    private void buildViews() {
        assignmentsView = new AssignmentsView(assignmentService);
        checkView = new CheckView(assignmentService, checkerService, settingsService, this::onResultsUpdated);
        resultsView = new ResultsView(resultService, settingsService);
        settingsView = new SettingsView(settingsService, this::onSettingsUpdated);
        aboutView = new AboutView();
    }

    private void buildLayout() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        Label appTitle = new Label("Ассистент\nпреподавателя");
        appTitle.getStyleClass().add("sidebar-title");
        appTitle.setWrapText(true);

        Button assignmentsBtn = navButton("Задания", "assignments");
        Button checkBtn = navButton("Проверка", "check");
        Button resultsBtn = navButton("Результаты", "results");
        Button settingsBtn = navButton("Настройки", "settings");
        Button aboutBtn = navButton("О программе", "about");

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(appTitle, assignmentsBtn, checkBtn, resultsBtn, settingsBtn, spacer, aboutBtn);
        sidebar.setPadding(new Insets(0, 0, 16, 0));

        demoBanner.getStyleClass().add("demo-banner");
        demoBanner.setMaxWidth(Double.MAX_VALUE);
        updateDemoBanner();

        VBox center = new VBox(contentPane);
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        root.setLeft(sidebar);
        root.setCenter(center);
        root.setTop(demoBanner);
        updateTopBanner();
    }

    private Button navButton(String text, String sectionId) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> showSection(sectionId));
        button.setUserData(sectionId);
        return button;
    }

    private void showSection(String sectionId) {
        contentPane.getChildren().clear();

        switch (sectionId) {
            case "assignments" -> {
                assignmentsView.refresh();
                contentPane.getChildren().add(assignmentsView.getRoot());
            }
            case "check" -> {
                checkView.refresh();
                contentPane.getChildren().add(checkView.getRoot());
            }
            case "results" -> {
                resultsView.refresh();
                contentPane.getChildren().add(resultsView.getRoot());
            }
            case "settings" -> {
                settingsView.refresh();
                contentPane.getChildren().add(settingsView.getRoot());
            }
            case "about" -> contentPane.getChildren().add(aboutView.getRoot());
        }

        updateNavSelection(sectionId);
    }

    private void updateNavSelection(String sectionId) {
        if (selectedNavButton != null) {
            selectedNavButton.getStyleClass().setAll("nav-button");
        }
        VBox sidebar = (VBox) root.getLeft();
        for (var node : sidebar.getChildren()) {
            if (node instanceof Button btn && sectionId.equals(btn.getUserData())) {
                btn.getStyleClass().setAll("nav-button-selected");
                selectedNavButton = btn;
                break;
            }
        }
    }

    private void onResultsUpdated() {
        resultsView.refresh();
    }

    private void onSettingsUpdated() {
        updateDemoBanner();
        checkView.updateDemoIndicator();
    }

    private void updateDemoBanner() {
        updateTopBanner();
    }

    private void updateTopBanner() {
        boolean demo = settingsService.isDemoMode();
        demoBanner.setVisible(demo);
        demoBanner.setManaged(demo);
    }
}
