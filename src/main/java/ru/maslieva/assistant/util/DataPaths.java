package ru.maslieva.assistant.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DataPaths {
    private static final Path DATA_DIR = Paths.get(System.getProperty("user.dir"), "data");

    private DataPaths() {
    }

    public static Path getDataDir() {
        return DATA_DIR;
    }

    public static Path getAssignmentsFile() {
        return DATA_DIR.resolve("assignments.json");
    }

    public static Path getResultsFile() {
        return DATA_DIR.resolve("results.csv");
    }

    public static Path getSettingsFile() {
        return DATA_DIR.resolve("settings.json");
    }

    public static void ensureDataDirExists() {
        try {
            Files.createDirectories(DATA_DIR);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать каталог данных: " + DATA_DIR, e);
        }
    }
}
