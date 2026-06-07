package ru.maslieva.assistant.model;

public enum ProgrammingLanguage {
    JAVA("Java"),
    HASKELL("Haskell");

    private final String displayName;

    ProgrammingLanguage(String displayName) {
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
