package org.burrow_studios.bruno.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Properties;

public class ResourceTools {
    private final ClassLoader classLoader;

    private ResourceTools(@NotNull ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static ResourceTools get(@NotNull Class<?> clazz) {
        return new ResourceTools(clazz.getClassLoader());
    }

    public @Nullable String getProperty(@NotNull String filename, @NotNull String key) throws IOException {
        Properties properties = new Properties();
        properties.load(getResource(filename + ".properties"));
        return properties.getProperty(key);
    }

    /**
     * Attempts to read the application version from the {@code meta.properties} resource.
     * @return Application version.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public @Nullable String getVersion() {
        try {
            return getProperty("meta", "version");
        } catch (Exception e) {
            System.out.println("\nCould not load version from resources.");
            e.printStackTrace();
            return null;
        }
    }

    public @Nullable InputStream getResource(@NotNull String name) {
        return classLoader.getResourceAsStream(name);
    }
}