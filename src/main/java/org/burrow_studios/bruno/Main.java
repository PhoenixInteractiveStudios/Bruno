package org.burrow_studios.bruno;

public class Main {
    static {
        System.out.print("Starting Bruno");
    }

    public static Bruno singleton;

    /** JVM entrypoint. */
    public static void main(String[] args) throws Exception {
        if (singleton != null)
            throw new IllegalStateException("Cannot initialize multiple times.");

        singleton = new Bruno();
    }
}
