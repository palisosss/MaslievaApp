package ru.maslieva.assistant.checker;

import ru.maslieva.assistant.model.Assignment;
import ru.maslieva.assistant.model.CheckResult;
import ru.maslieva.assistant.model.CheckStatus;

import java.io.File;
import java.util.Random;

public class DemoChecker implements SolutionChecker {
    private final Random random = new Random();

    @Override
    public CheckResult check(Assignment assignment, File solutionFile, String studentName, String group) {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        CheckResult result = new CheckResult();
        result.setStudentName(studentName);
        result.setGroup(group);
        result.setAssignmentTitle(assignment.getTitle());
        result.setLanguage(assignment.getLanguage());

        String expected = assignment.getExpectedOutput();
        if (expected != null && !expected.isBlank()) {
            result.setStatus(CheckStatus.SUCCESS);
            result.setScore(assignment.getMaxScore());
            result.setComment("Демонстрационный режим: результат совпадает с ожидаемым выводом. Реальная компиляция не выполнялась.");
        } else {
            CheckStatus[] statuses = {
                    CheckStatus.SUCCESS,
                    CheckStatus.WRONG_ANSWER,
                    CheckStatus.COMPILE_ERROR,
                    CheckStatus.RUNTIME_ERROR
            };
            CheckStatus status = statuses[random.nextInt(statuses.length)];
            result.setStatus(status);
            result.setScore(status == CheckStatus.SUCCESS ? assignment.getMaxScore() : 0);
            result.setComment("Демонстрационный режим: сгенерирован пример результата («" + status.getDisplayName() + "»).");
        }

        return result;
    }
}
