package org.burrow_studios.bruno.util;

import org.burrow_studios.bruno.Bruno;
import org.burrow_studios.bruno.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Properties;

public class ResourceUtil {
    private ResourceUtil() { }

    public static @Nullable String getProperty(@NotNull String filename, @NotNull String key) throws IOException {
        Properties properties = new Properties();
        properties.load(getResource(filename + ".properties"));
        return properties.getProperty(key);
    }

    /**
     * Attempts to read the application version from the {@code version.properties} resource.
     * @return Application version.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public static @Nullable String getVersion() {
        try {
            return getProperty("version", "version");
        } catch (Exception e) {
            System.out.println("\nCould not load version from resources.");
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void createDefault(@NotNull String resource) throws IOException {
        File file = new File(Bruno.DIR, resource);
        if (file.exists()) return;

        file.createNewFile();

        try(
                InputStream   inStream = getResource(resource);
                OutputStream outStream = new FileOutputStream(file)
        ) {
            if (inStream == null)
                throw new IllegalArgumentException("No such resource: " + resource);

            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, readBytes);
            }
        }
    }

    private static @Nullable InputStream getResource(@NotNull String name) {
        return Main.class.getClassLoader().getResourceAsStream(name);
    }
}
