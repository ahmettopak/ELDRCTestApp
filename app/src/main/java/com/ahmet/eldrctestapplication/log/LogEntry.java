package com.ahmet.eldrctestapplication.log;

/**
 * @author Ahmet TOPAK
 * @version 1.0
 * @since 7/23/2024
 */

public class LogEntry {
    public enum LogType {
        INFO, ERROR, DEBUG
    }

    private LogType type;
    private String message;

    public LogEntry(LogType type, String message) {
        this.type = type;
        this.message = message;
    }

    public LogType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
