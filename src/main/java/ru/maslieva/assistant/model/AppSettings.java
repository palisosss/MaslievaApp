package ru.maslieva.assistant.model;

public class AppSettings {
    private String teacherEmail = "";
    private String solutionsFolderPath = "";
    private String csvExportPath = "";
    private boolean demoMode = false;

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail != null ? teacherEmail : "";
    }

    public String getSolutionsFolderPath() {
        return solutionsFolderPath;
    }

    public void setSolutionsFolderPath(String solutionsFolderPath) {
        this.solutionsFolderPath = solutionsFolderPath != null ? solutionsFolderPath : "";
    }

    public String getCsvExportPath() {
        return csvExportPath;
    }

    public void setCsvExportPath(String csvExportPath) {
        this.csvExportPath = csvExportPath != null ? csvExportPath : "";
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }
}
