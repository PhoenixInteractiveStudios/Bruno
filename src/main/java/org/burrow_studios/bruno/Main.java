package org.burrow_studios.bruno;

import org.burrow_studios.bruno.util.ResourceTools;

public class Main {
    static {
        System.out.print("Starting Bruno");
    }

    public static final String VERSION = ResourceTools.get(Main.class).getVersion();

    public static void main(String[] args) {
        if (VERSION == null)
            throw new Error("Unknown version");
        System.out.printf(" version %s...%n", VERSION);
    }
}
