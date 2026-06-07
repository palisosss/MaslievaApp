package ru.maslieva.assistant.checker;

import ru.maslieva.assistant.model.Assignment;
import ru.maslieva.assistant.model.CheckResult;
import ru.maslieva.assistant.model.ProgrammingLanguage;
import ru.maslieva.assistant.service.ResultService;
import ru.maslieva.assistant.service.SettingsService;

import java.io.File;

public class CheckerService {
    private final SettingsService settingsService;
    private final ResultService resultService;
    private final DemoChecker demoChecker = new DemoChecker();
    private final JavaChecker javaChecker = new JavaChecker();
    private final HaskellChecker haskellChecker = new HaskellChecker();

    public CheckerService(SettingsService settingsService, ResultService resultService) {
        this.settingsService = settingsService;
        this.resultService = resultService;
    }

    public CheckResult check(Assignment assignment, File solutionFile, String studentName, String group) {
        SolutionChecker checker;
        if (settingsService.isDemoMode()) {
            checker = demoChecker;
        } else if (assignment.getLanguage() == ProgrammingLanguage.HASKELL) {
            checker = haskellChecker;
        } else {
            checker = javaChecker;
        }

        CheckResult result = checker.check(assignment, solutionFile, studentName, group);
        resultService.add(result);
        return result;
    }
}
