package ru.maslieva.assistant.service;

import ru.maslieva.assistant.model.CheckResult;
import ru.maslieva.assistant.model.CheckStatus;
import ru.maslieva.assistant.model.ProgrammingLanguage;
import ru.maslieva.assistant.util.CsvExporter;
import ru.maslieva.assistant.util.DataPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ResultService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final String HEADER = "Дата;ФИО;Группа;Задание;Язык;Статус;Балл;Комментарий";

    private final List<CheckResult> results = new ArrayList<>();

    public void load() {
        results.clear();
        DataPaths.ensureDataDirExists();
        if (!Files.exists(DataPaths.getResultsFile())) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(DataPaths.getResultsFile(), StandardCharsets.UTF_8);
            if (lines.size() <= 1) {
                return;
            }
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }
                CheckResult result = parseLine(line);
                if (result != null) {
                    results.add(result);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить результаты", e);
        }
    }

    public List<CheckResult> getAll() {
        return List.copyOf(results);
    }

    public void add(CheckResult result) {
        results.add(0, result);
        save();
    }

    public void clearAll() {
        results.clear();
        save();
    }

    public void save() {
        DataPaths.ensureDataDirExists();
        try {
            CsvExporter.export(results, DataPaths.getResultsFile());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить результаты", e);
        }
    }

    public void exportTo(Path target) throws IOException {
        CsvExporter.export(results, target);
    }

    private CheckResult parseLine(String line) {
        String[] parts = splitCsvLine(line);
        if (parts.length < 8) {
            return null;
        }
        CheckResult result = new CheckResult();
        try {
            result.setDateTime(LocalDateTime.parse(parts[0], FORMATTER));
        } catch (Exception e) {
            result.setDateTime(LocalDateTime.now());
        }
        result.setStudentName(parts[1]);
        result.setGroup(parts[2]);
        result.setAssignmentTitle(parts[3]);
        result.setLanguage(parseLanguage(parts[4]));
        result.setStatus(parseStatus(parts[5]));
        try {
            result.setScore(Integer.parseInt(parts[6]));
        } catch (NumberFormatException e) {
            result.setScore(0);
        }
        result.setComment(parts[7]);
        return result;
    }

    private String[] splitCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ';' && !inQuotes) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }

    private ProgrammingLanguage parseLanguage(String value) {
        for (ProgrammingLanguage lang : ProgrammingLanguage.values()) {
            if (lang.getDisplayName().equalsIgnoreCase(value)) {
                return lang;
            }
        }
        return ProgrammingLanguage.JAVA;
    }

    private CheckStatus parseStatus(String value) {
        for (CheckStatus status : CheckStatus.values()) {
            if (status.getDisplayName().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return CheckStatus.RUNTIME_ERROR;
    }
}
