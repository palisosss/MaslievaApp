package ru.maslieva.assistant.model;

public enum CheckStatus {
    SUCCESS("Успешно"),
    WRONG_ANSWER("Неверный ответ"),
    COMPILE_ERROR("Ошибка компиляции"),
    RUNTIME_ERROR("Ошибка выполнения"),
    TIME_LIMIT("Превышено время выполнения"),
    RUNTIME_NOT_FOUND("Среда выполнения не найдена");

    private final String displayName;

    CheckStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
