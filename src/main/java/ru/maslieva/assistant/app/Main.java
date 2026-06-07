package ru.maslieva.assistant.app;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.maslieva.assistant.checker.CheckerService;
import ru.maslieva.assistant.service.AssignmentService;
import ru.maslieva.assistant.service.ResultService;
import ru.maslieva.assistant.service.SettingsService;
import ru.maslieva.assistant.ui.MainView;
import ru.maslieva.assistant.util.DataPaths;

public class Main extends Application {
    private final AssignmentService assignmentService = new AssignmentService();
    private final ResultService resultService = new ResultService();
    private final SettingsService settingsService = new SettingsService();
    private CheckerService checkerService;

    @Override
    public void start(Stage stage) {
        DataPaths.ensureDataDirExists();
        assignmentService.load();
        resultService.load();
        settingsService.load();

        checkerService = new CheckerService(settingsService, resultService);

        MainView mainView = new MainView(assignmentService, resultService, settingsService, checkerService);
        mainView.show(stage);

        stage.setOnCloseRequest(e -> settingsService.save());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
