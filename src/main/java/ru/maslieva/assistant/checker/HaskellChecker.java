package ru.maslieva.assistant.checker;

import ru.maslieva.assistant.model.Assignment;
import ru.maslieva.assistant.model.CheckResult;
import ru.maslieva.assistant.model.CheckStatus;
import ru.maslieva.assistant.util.OutputNormalizer;
import ru.maslieva.assistant.util.ProcessRunner;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class HaskellChecker implements SolutionChecker {
    private static final String HASKELL_NOT_FOUND =
            "Среда Haskell не найдена. Установите GHC или используйте демонстрационный режим";

    @Override
    public CheckResult check(Assignment assignment, File solutionFile, String studentName, String group) {
        CheckResult result = createBaseResult(assignment, studentName, group);

        boolean hasRunGhc = ProcessRunner.isCommandAvailable("runghc");
        boolean hasGhc = ProcessRunner.isCommandAvailable("ghc");

        if (!hasRunGhc && !hasGhc) {
            result.setStatus(CheckStatus.RUNTIME_NOT_FOUND);
            result.setScore(0);
            result.setComment(HASKELL_NOT_FOUND);
            return result;
        }

        Path workDir = solutionFile.getParentFile().toPath();
        ProcessRunner.ProcessResult runResult;

        if (hasRunGhc) {
            List<String> command = ProcessRunner.buildCommand("runghc", solutionFile.getAbsolutePath());
            runResult = ProcessRunner.run(command, workDir, assignment.getTestInput());
        } else {
            runResult = runWithGhc(solutionFile, workDir, assignment.getTestInput());
        }

        return evaluateResult(result, assignment, runResult);
    }

    private ProcessRunner.ProcessResult runWithGhc(File solutionFile, Path workDir, String testInput) {
        String outName = "hs_out";
        List<String> compileCmd = ProcessRunner.buildCommand(
                "ghc", "-o", outName, solutionFile.getAbsolutePath()
        );
        ProcessRunner.ProcessResult compileResult = ProcessRunner.run(compileCmd, workDir, null);

        if (compileResult.timedOut()) {
            return new ProcessRunner.ProcessResult(-1, "", "Превышено время компиляции.", true);
        }
        if (compileResult.exitCode() != 0) {
            return compileResult;
        }

        Path executable = workDir.resolve(outName);
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            executable = workDir.resolve(outName + ".exe");
        }
        return ProcessRunner.runExecutable(executable, workDir, testInput);
    }

    private CheckResult evaluateResult(CheckResult result, Assignment assignment, ProcessRunner.ProcessResult runResult) {
        if (runResult.timedOut()) {
            result.setStatus(CheckStatus.TIME_LIMIT);
            result.setComment("Программа не завершилась за 5 секунд.");
            return result;
        }

        String combinedError = runResult.stderr();
        if (runResult.exitCode() != 0) {
            if (looksLikeCompileError(combinedError)) {
                result.setStatus(CheckStatus.COMPILE_ERROR);
            } else {
                result.setStatus(CheckStatus.RUNTIME_ERROR);
            }
            result.setComment(truncate(combinedError.isBlank() ? runResult.stdout() : combinedError));
            return result;
        }

        if (OutputNormalizer.matches(runResult.stdout(), assignment.getExpectedOutput())) {
            result.setStatus(CheckStatus.SUCCESS);
            result.setScore(assignment.getMaxScore());
            result.setComment("Вывод программы совпадает с ожидаемым результатом.");
        } else {
            result.setStatus(CheckStatus.WRONG_ANSWER);
            result.setScore(0);
            result.setComment("Полученный вывод: " + truncate(runResult.stdout()));
        }

        return result;
    }

    private boolean looksLikeCompileError(String stderr) {
        if (stderr == null) {
            return false;
        }
        String lower = stderr.toLowerCase();
        return lower.contains("error") || lower.contains("parse error") || lower.contains("not found");
    }

    private CheckResult createBaseResult(Assignment assignment, String studentName, String group) {
        CheckResult result = new CheckResult();
        result.setStudentName(studentName);
        result.setGroup(group);
        result.setAssignmentTitle(assignment.getTitle());
        result.setLanguage(assignment.getLanguage());
        result.setScore(0);
        return result;
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) + "..." : trimmed;
    }
}
