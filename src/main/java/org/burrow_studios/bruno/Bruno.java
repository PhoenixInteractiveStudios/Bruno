package org.burrow_studios.bruno;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.burrow_studios.bruno.listener.DashboardCleaner;
import org.burrow_studios.bruno.text.TextProvider;
import org.burrow_studios.bruno.util.ResourceTools;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Bruno {
    private static final Logger LOG = LoggerFactory.getLogger(Bruno.class);

    private TextProvider textProvider;
    private JDA jda;

    private Config config;

    public Bruno() { }

    public void start() throws IOException, InterruptedException {
        LOG.info("Starting Bruno...");

        LOG.debug("Creating default config file");
        ResourceTools.get(Main.class).createDefault(Main.DIR, "config.properties");

        LOG.debug("Creating default text.json");
        ResourceTools.get(Main.class).createDefault(Main.DIR, "text.json");

        LOG.debug("Reading config");
        this.config = Config.fromFile(new File(Main.DIR, "config.properties"));

        LOG.info("Parsing text.json");
        this.textProvider = new TextProvider(new File(Main.DIR, "text.json"));


        LOG.info("Initializing JDA");
        this.jda = JDABuilder.create(config.token(),
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.GUILD_MEMBERS)
                .disableCache(CacheFlag.ACTIVITY)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .disableCache(CacheFlag.SCHEDULED_EVENTS)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .addEventListeners(new DashboardCleaner(this))
                .build();

        this.jda.awaitReady();

        this.jda.getPresence().setStatus(OnlineStatus.ONLINE);

        LOG.info("All done.");
    }

    public void stop() {
        LOG.info("Stopping...");

        if (this.jda != null) {
            try {
                this.jda.shutdown();
                this.jda.awaitShutdown();
            } catch (InterruptedException e) {
                LOG.warn("Could not properly shut down JDA", e);
            }
        }

        LOG.info("OK bye");
    }

    public @NotNull TextProvider getTextProvider() {
        return this.textProvider;
    }

    public @NotNull Config getConfig() {
        return this.config;
    }
}
