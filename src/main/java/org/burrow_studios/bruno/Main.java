package org.burrow_studios.bruno;

import org.burrow_studios.bruno.util.ResourceUtil;
import org.burrow_studios.bruno.util.logging.LogUtil;

public class Main {
    static {
        System.out.print("Starting Bruno");
    }

    public static Bruno singleton;
    /** Static application version. */
    public static final String VERSION = ResourceUtil.getVersion();

    /** JVM entrypoint. */
    public static void main(String[] args) throws Exception {
        if (singleton != null)
            throw new IllegalStateException("Cannot initialize multiple times.");

        if (VERSION == null)
            throw new AssertionError("Unknown version");
        System.out.printf(" version %s...%n", VERSION);

        LogUtil.init();

        singleton = new Bruno();
        singleton.start();
    }
}
