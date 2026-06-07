package ru.maslieva.assistant.checker;

import ru.maslieva.assistant.model.Assignment;
import ru.maslieva.assistant.model.CheckResult;
import ru.maslieva.assistant.model.CheckStatus;
import ru.maslieva.assistant.util.OutputNormalizer;
import ru.maslieva.assistant.util.ProcessRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class JavaChecker implements SolutionChecker {
    @Override
    public CheckResult check(Assignment assignment, File solutionFile, String studentName, String group) {
        CheckResult result = createBaseResult(assignment, studentName, group);

        if (!ProcessRunner.isCommandAvailable("javac") || !ProcessRunner.isCommandAvailable("java")) {
            result.setStatus(CheckStatus.RUNTIME_NOT_FOUND);
            result.setScore(0);
            result.setComment("Среда Java не найдена. Убедитесь, что JDK 17 установлен и доступен в PATH.");
            return result;
        }

        String className = getClassName(solutionFile.getName());
        CompileContext context = null;
        try {
            context = prepareCompileContext(solutionFile, className);
            Path workDir = context.workDir();
            File sourceForCompile = context.sourceFile();

            List<String> compileCmd = List.of(
                    "javac",
                    "-encoding", "UTF-8",
                    sourceForCompile.getAbsolutePath()
            );
            ProcessRunner.ProcessResult compileResult = ProcessRunner.run(compileCmd, workDir, null);

            if (compileResult.timedOut()) {
                result.setStatus(CheckStatus.TIME_LIMIT);
                result.setComment("Превышено время компиляции.");
                return result;
            }
            if (compileResult.exitCode() != 0) {
                result.setStatus(CheckStatus.COMPILE_ERROR);
                String compilerOutput = compileResult.stderr().isBlank()
                        ? compileResult.stdout()
                        : compileResult.stderr();
                result.setComment("Ошибка компиляции: " + truncate(compilerOutput));
                return result;
            }

            String classPath = context.tempDir() != null
                    ? workDir.toString()
                    : (solutionFile.getParent() != null ? solutionFile.getParent() : workDir.toString());

            List<String> runCmd = List.of("java", "-cp", classPath, className);
            ProcessRunner.ProcessResult runResult = ProcessRunner.run(runCmd, workDir, assignment.getTestInput());

            if (runResult.timedOut()) {
                result.setStatus(CheckStatus.TIME_LIMIT);
                result.setComment("Программа не завершилась за 5 секунд.");
                return result;
            }
            if (runResult.exitCode() != 0) {
                result.setStatus(CheckStatus.RUNTIME_ERROR);
                result.setComment(truncate(runResult.stderr().isBlank() ? runResult.stdout() : runResult.stderr()));
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
        } catch (IOException e) {
            result.setStatus(CheckStatus.RUNTIME_ERROR);
            result.setComment("Не удалось подготовить файл для компиляции: " + e.getMessage());
        } finally {
            if (context != null) {
                context.cleanup();
            }
        }

        return result;
    }

    private CompileContext prepareCompileContext(File solutionFile, String className) throws IOException {
        File parent = solutionFile.getParentFile();
        Path workDir = parent != null ? parent.toPath() : Path.of(".");

        if (hasJavaExtension(solutionFile.getName()) && solutionFile.getName().endsWith(".java")) {
            return new CompileContext(workDir, solutionFile, null);
        }

        if (hasJavaExtension(solutionFile.getName())) {
            Path tempDir = Files.createTempDirectory("assistant-java-check-");
            Path normalizedSource = tempDir.resolve(className + ".java");
            Files.copy(solutionFile.toPath(), normalizedSource);
            return new CompileContext(tempDir, normalizedSource.toFile(), tempDir);
        }

        return new CompileContext(workDir, solutionFile, null);
    }

    private boolean hasJavaExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return false;
        }
        return "java".equalsIgnoreCase(fileName.substring(dot + 1));
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

    private String getClassName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) + "..." : trimmed;
    }

    private record CompileContext(Path workDir, File sourceFile, Path tempDir) {
        void cleanup() {
            if (tempDir == null) {
                return;
            }
            try (var walk = Files.walk(tempDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                            }
                        });
            } catch (IOException ignored) {
            }
        }
    }
}
