package ru.maslieva.assistant.model;

import java.util.UUID;

public class Assignment {
    private String id;
    private String title;
    private ProgrammingLanguage language;
    private String description;
    private String testInput;
    private String expectedOutput;
    private int maxScore;

    public Assignment() {
        this.id = UUID.randomUUID().toString();
    }

    public Assignment(String title, ProgrammingLanguage language, String description,
                      String testInput, String expectedOutput, int maxScore) {
        this();
        this.title = title;
        this.language = language;
        this.description = description;
        this.testInput = testInput;
        this.expectedOutput = expectedOutput;
        this.maxScore = maxScore;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ProgrammingLanguage getLanguage() {
        return language;
    }

    public void setLanguage(ProgrammingLanguage language) {
        this.language = language;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTestInput() {
        return testInput;
    }

    public void setTestInput(String testInput) {
        this.testInput = testInput;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    @Override
    public String toString() {
        if (title == null || title.isBlank()) {
            return "Без названия";
        }
        return title;
    }
}
