package org.burrow_studios.bruno.util.logging;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** A simple Logger that always has a parent {@link Logger}. */
public class NestedLogger extends Logger {
    /** The parent logger. */
    private Logger parentLogger;

    /** Creates a new {@link NestedLogger} with the provided name and saves the parent. */
    public NestedLogger(String name, @NotNull Logger parent) {
        super(name, null);
        super.setUseParentHandlers(false);
        // pass on all logs to the parent (level should be filtered there)
        this.setLevel(Level.ALL);
        this.parentLogger = parent;
    }

    /**
     * This method differs from {@link Logger#getParent()} in that it returns the set parent of this Logger without
     * meddling with Javas internals, since the internal parent is used by the LogManager.
     * @return The parent of this Logger.
     */
    public @NotNull Logger getParentLogger() {
        return parentLogger;
    }

    /**
     * This method differs from {@link Logger#getParent()} in that it returns the set parent of this Logger without
     * meddling with Javas internals, since the internal parent is used by the LogManager.
     * @param parent The parent of this Logger.
     */
    public void setParentLogger(@NotNull Logger parent) {
        this.parentLogger = parent;
    }

    @Override
    public void setUseParentHandlers(boolean useParentHandlers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(LogRecord record) {
        record.setLoggerName(parentLogger.getName() + "/" + record.getLoggerName());
        parentLogger.log(record);
    }
}