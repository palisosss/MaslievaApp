package ru.maslieva.assistant.util;

public final class OutputNormalizer {
    private OutputNormalizer() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    public static boolean matches(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }
}
