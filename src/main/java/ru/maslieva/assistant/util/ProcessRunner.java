package ru.maslieva.assistant.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessRunner {
    public static final long TIMEOUT_SECONDS = 5;

    private static final List<String> ALLOWED_COMMANDS = List.of("javac", "java", "runghc", "ghc");

    public record ProcessResult(int exitCode, String stdout, String stderr, boolean timedOut) {
    }

    public static boolean isCommandAvailable(String command) {
        if (!ALLOWED_COMMANDS.contains(command)) {
            return false;
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    isWindows() ? new String[]{"where", command} : new String[]{"which", command}
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();
            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static ProcessResult runExecutable(Path executable, Path workingDir, String stdin) {
        if (executable == null || workingDir == null) {
            throw new IllegalArgumentException("Некорректный путь к исполняемому файлу");
        }
        String fileName = executable.getFileName().toString().toLowerCase();
        if (!fileName.equals("hs_out") && !fileName.equals("hs_out.exe")) {
            throw new IllegalArgumentException("Исполняемый файл не разрешён: " + fileName);
        }
        if (!executable.normalize().startsWith(workingDir.normalize())) {
            throw new IllegalArgumentException("Исполняемый файл должен находиться в рабочей директории");
        }
        return run(List.of(executable.toString()), workingDir, stdin, true);
    }

    public static ProcessResult run(List<String> command, Path workingDir, String stdin) {
        if (command.isEmpty() || !ALLOWED_COMMANDS.contains(command.get(0))) {
            throw new IllegalArgumentException("Команда не разрешена: " + command);
        }
        return run(command, workingDir, stdin, false);
    }

    private static ProcessResult run(List<String> command, Path workingDir, String stdin, boolean allowExecutable) {
        if (!allowExecutable && (command.isEmpty() || !ALLOWED_COMMANDS.contains(command.get(0)))) {
            throw new IllegalArgumentException("Команда не разрешена: " + command);
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            if (workingDir != null) {
                builder.directory(workingDir.toFile());
            }
            builder.redirectErrorStream(false);

            Process process = builder.start();

            if (stdin != null && !stdin.isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(stdin.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            }

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            Thread outThread = new Thread(() -> readStream(process.getInputStream(), stdout));
            Thread errThread = new Thread(() -> readStream(process.getErrorStream(), stderr));
            outThread.start();
            errThread.start();

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                outThread.join(500);
                errThread.join(500);
                return new ProcessResult(-1, stdout.toString(), stderr.toString(), true);
            }

            outThread.join(1000);
            errThread.join(1000);
            return new ProcessResult(process.exitValue(), stdout.toString(), stderr.toString(), false);
        } catch (Exception e) {
            return new ProcessResult(-1, "", e.getMessage(), false);
        }
    }

    private static void readStream(java.io.InputStream stream, StringBuilder target) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!target.isEmpty()) {
                    target.append('\n');
                }
                target.append(line);
            }
        } catch (Exception ignored) {
        }
    }

    public static List<String> buildCommand(String executable, String... args) {
        List<String> command = new ArrayList<>();
        command.add(executable);
        for (String arg : args) {
            command.add(arg);
        }
        return command;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
