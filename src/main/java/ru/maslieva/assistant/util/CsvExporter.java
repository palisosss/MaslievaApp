package ru.maslieva.assistant.util;

import ru.maslieva.assistant.model.CheckResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvExporter {
    private static final String HEADER = "Дата;ФИО;Группа;Задание;Язык;Статус;Балл;Комментарий";

    private CsvExporter() {
    }

    public static void export(List<CheckResult> results, Path target) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER).append('\n');
        for (CheckResult result : results) {
            sb.append(escape(result.getFormattedDate())).append(';')
                    .append(escape(result.getStudentName())).append(';')
                    .append(escape(result.getGroup())).append(';')
                    .append(escape(result.getAssignmentTitle())).append(';')
                    .append(escape(result.getLanguage() != null ? result.getLanguage().getDisplayName() : "")).append(';')
                    .append(escape(result.getStatus() != null ? result.getStatus().getDisplayName() : "")).append(';')
                    .append(result.getScore()).append(';')
                    .append(escape(result.getComment()))
                    .append('\n');
        }
        Files.writeString(target, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
