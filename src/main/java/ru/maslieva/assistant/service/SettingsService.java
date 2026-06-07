package ru.maslieva.assistant.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.maslieva.assistant.model.AppSettings;
import ru.maslieva.assistant.util.DataPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SettingsService {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private AppSettings settings = new AppSettings();

    public void load() {
        DataPaths.ensureDataDirExists();
        if (!Files.exists(DataPaths.getSettingsFile())) {
            settings = new AppSettings();
            return;
        }
        try {
            String json = Files.readString(DataPaths.getSettingsFile(), StandardCharsets.UTF_8);
            if (json.isBlank()) {
                settings = new AppSettings();
                return;
            }
            AppSettings loaded = gson.fromJson(json, AppSettings.class);
            settings = loaded != null ? loaded : new AppSettings();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить настройки", e);
        }
    }

    public void save() {
        DataPaths.ensureDataDirExists();
        try {
            String json = gson.toJson(settings);
            Files.writeString(DataPaths.getSettingsFile(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить настройки", e);
        }
    }

    public AppSettings getSettings() {
        return settings;
    }

    public void update(AppSettings newSettings) {
        this.settings = newSettings;
        save();
    }

    public boolean isDemoMode() {
        return settings.isDemoMode();
    }
}
