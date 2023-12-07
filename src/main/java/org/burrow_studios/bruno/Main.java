package org.burrow_studios.bruno;

import java.util.Properties;

public class Main {
    static {
        System.out.print("Starting Bruno");
    }

    public static Bruno singleton;
    /** Static application version. */
    public static final String VERSION = retrieveVersion();

    /** JVM entrypoint. */
    public static void main(String[] args) throws Exception {
        if (singleton != null)
            throw new IllegalStateException("Cannot initialize multiple times.");

        if (VERSION == null)
            throw new AssertionError("Unknown version");
        System.out.printf(" version %s...%n", VERSION);

        singleton = new Bruno();
    }

    /**
     * Attempts to read the application version from the {@code version.properties} resource.
     * @return Application version.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    private static String retrieveVersion() {
        try {
            Properties properties = new Properties();
            properties.load(Main.class.getResourceAsStream("version.properties"));
            return properties.getProperty("version");
        } catch (Exception e) {
            System.out.println("\nCould not load version from resources.");
            e.printStackTrace();
            return null;
        }
    }
}
