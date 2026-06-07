package ru.maslieva.assistant.checker;

import ru.maslieva.assistant.model.Assignment;
import ru.maslieva.assistant.model.CheckResult;

import java.io.File;

public interface SolutionChecker {
    CheckResult check(Assignment assignment, File solutionFile, String studentName, String group);
}
