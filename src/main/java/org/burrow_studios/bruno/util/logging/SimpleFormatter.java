package org.burrow_studios.bruno.util.logging;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        if (record == null) return null;

        String time  = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").format(record.getInstant().atZone(ZoneId.of("UTC")));
        String level = String.format("%7s", record.getLevel().getName());

        StringBuilder str = new StringBuilder();

        str.append("[")
                .append(time)
                .append(" ")
                .append(level)
                .append("]: ")
                .append("[")
                .append(record.getLoggerName())
                .append("] ")
                .append(record.getMessage())
                .append("\n");

        if (record.getThrown() != null) {
            str.append(" ".repeat(time.length() + 2)).append(level).append("   ").append(record.getThrown().toString()).append("\n");

            StackTraceElement[] stackTrace = record.getThrown().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                str.append(" ".repeat(time.length() + level.length() + 5))
                        .append("  ")
                        .append(stackTraceElement)
                        .append("\n");
            }

            if (record.getThrown().getCause() != null) {
                LogRecord subRecord = new LogRecord(record.getLevel(), null);
                subRecord.setThrown(record.getThrown().getCause());
                str.append("\n").append(" ".repeat(time.length() + 3)).append("CAUSED BY:\n").append(this.format(subRecord));
            }
        }

        return str.toString();
    }
}