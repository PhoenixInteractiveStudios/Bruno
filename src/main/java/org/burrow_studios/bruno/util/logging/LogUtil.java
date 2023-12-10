package org.burrow_studios.bruno.util.logging;

import org.burrow_studios.bruno.Bruno;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.logging.*;

public class LogUtil {
    private LogUtil() { }

    public static void init() throws IOException {
        SimpleFormatter formatter = new SimpleFormatter();

        Logger rootLogger = Logger.getLogger("");

        for (Handler handler : rootLogger.getHandlers())
            if (handler instanceof ConsoleHandler)
                handler.setFormatter(formatter);

        try {
            rootLogger.addHandler(getFileHandler(formatter));
        } catch (IOException e) {
            throw new IOException("Could not create logging file handler", e);
        }
    }

    public static @NotNull FileHandler getFileHandler(@NotNull Formatter formatter) throws IOException {
        String datePrefix = new SimpleDateFormat("yyyy-MM-dd'T'").format(Date.from(Instant.now()));

        // retrieve all existing log files from today
        File[] files = getLogDir().listFiles((dir, name) -> name.matches("^\\d\\d\\d\\d-\\d\\d-\\d\\dT_\\d+\\.log$") && name.startsWith(datePrefix));
        if (files == null)
            throw new NotDirectoryException(getLogDir().getName());

        String fileName = datePrefix + "_" + files.length + ".log";

        // create File
        File logFile = new File(getLogDir(), fileName);


        // create FileHandler
        FileHandler fileHandler = new FileHandler(logFile.getPath(), true);
        fileHandler.setFormatter(formatter);
        return fileHandler;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getLogDir() {
        File file = new File(Bruno.DIR, "logs");
        file.mkdir();
        return file;
    }
}
