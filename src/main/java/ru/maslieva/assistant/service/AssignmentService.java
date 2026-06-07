package ru.maslieva.assistant.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.maslieva.assistant.model.Assignment;
import ru.maslieva.assistant.util.DataPaths;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssignmentService {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<Assignment> assignments = new ArrayList<>();

    public void load() {
        assignments.clear();
        DataPaths.ensureDataDirExists();
        if (!Files.exists(DataPaths.getAssignmentsFile())) {
            return;
        }
        try {
            String json = Files.readString(DataPaths.getAssignmentsFile(), StandardCharsets.UTF_8);
            if (json.isBlank()) {
                return;
            }
            Type type = new TypeToken<List<Assignment>>() {}.getType();
            List<Assignment> loaded = gson.fromJson(json, type);
            if (loaded != null) {
                assignments.addAll(loaded);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить задания", e);
        }
    }

    public void save() {
        DataPaths.ensureDataDirExists();
        try {
            String json = gson.toJson(assignments);
            Files.writeString(DataPaths.getAssignmentsFile(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить задания", e);
        }
    }

    public List<Assignment> getAll() {
        return List.copyOf(assignments);
    }

    public void add(Assignment assignment) {
        assignments.add(assignment);
        save();
    }

    public void remove(Assignment assignment) {
        assignments.removeIf(a -> a.getId().equals(assignment.getId()));
        save();
    }

    public Optional<Assignment> findById(String id) {
        return assignments.stream().filter(a -> a.getId().equals(id)).findFirst();
    }
}
