package org.burrow_studios.bruno.util;

import org.burrow_studios.bruno.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Properties;

public class ResourceUtil {
    private ResourceUtil() { }

    public static @Nullable String getProperty(@NotNull String filename, @NotNull String key) throws IOException {
        Properties properties = new Properties();
        properties.load(Main.class.getResourceAsStream(filename + ".properties"));
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
}
