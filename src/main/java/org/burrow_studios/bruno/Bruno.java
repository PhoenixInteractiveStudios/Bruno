package org.burrow_studios.bruno;

import org.burrow_studios.bruno.util.ResourceTools;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Bruno {
    private static final Logger LOG = LoggerFactory.getLogger(Bruno.class);

    private Config config;

    public Bruno() { }

    public void start() throws IOException {
        LOG.info("Starting Bruno...");

        LOG.debug("Creating default config file");
        ResourceTools.get(Main.class).createDefault(Main.DIR, "config.properties");

        LOG.debug("Reading config");
        this.config = Config.fromFile(new File(Main.DIR, "config.properties"));

        LOG.info("All done.");
    }

    public void stop() {
        LOG.info("Stopping...");

        LOG.info("OK bye");
    }

    public @NotNull Config getConfig() {
        return this.config;
    }
}
