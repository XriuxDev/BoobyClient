package com.boobyclient.launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public final class LauncherLog {
    private static final String LOG_FILE = System.getProperty("user.home") + "/.boobyclient/launcher.log";
    private static final String FALLBACK_LOG_FILE = System.getProperty("java.io.tmpdir") + "boobyclient-launcher.log";

    private LauncherLog() {
    }

    public static void info(String message) {
        write("INFO", message, null);
    }

    public static void warn(String message) {
        write("WARN", message, null);
    }

    public static void error(String message, Throwable error) {
        write("ERROR", message, error);
    }

    private static void write(String level, String message, Throwable error) {
        try {
            File logFile = new File(LOG_FILE);
            if (logFile.getParentFile() != null) {
                logFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(String.format("%s [%s] %s%n", LocalDateTime.now(), level, message));
                if (error != null) {
                    writer.write(error.toString() + System.lineSeparator());
                }
            }
        } catch (IOException ignored) {
            writeFallback(level, message, error);
        }
    }

    private static void writeFallback(String level, String message, Throwable error) {
        try (FileWriter writer = new FileWriter(FALLBACK_LOG_FILE, true)) {
            writer.write(String.format("%s [%s] %s%n", LocalDateTime.now(), level, message));
            if (error != null) {
                writer.write(error.toString() + System.lineSeparator());
            }
        } catch (IOException ignored) {
            // Avoid crashing if fallback logging fails.
        }
    }
}
