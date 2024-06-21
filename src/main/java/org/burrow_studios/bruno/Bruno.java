package org.burrow_studios.bruno;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.burrow_studios.bruno.dashboard.DashboardService;
import org.burrow_studios.bruno.emoji.EmojiProvider;
import org.burrow_studios.bruno.forum.ForumManager;
import org.burrow_studios.bruno.listener.DashboardUpdater;
import org.burrow_studios.bruno.listener.PriorityTagObserver;
import org.burrow_studios.bruno.listener.RefreshListener;
import org.burrow_studios.bruno.text.TextProvider;
import org.burrow_studios.bruno.util.ResourceTools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Bruno {
    private static final Logger LOG = LoggerFactory.getLogger(Bruno.class);

    private DashboardService dashboardService;
    private EmojiProvider emojiProvider;
    private ForumManager forumManager;
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

        LOG.debug("Creating default emojis.json");
        ResourceTools.get(Main.class).createDefault(Main.DIR, "emojis.json");

        LOG.debug("Reading config");
        this.config = Config.fromFile(new File(Main.DIR, "config.properties"));

        LOG.info("Parsing text.json");
        this.textProvider = new TextProvider(new File(Main.DIR, "text.json"));

        LOG.info("Initializing EmojiProvider");
        this.emojiProvider = new EmojiProvider(new File(Main.DIR, "emojis.json"));

        LOG.info("Initializing ForumManager");
        this.forumManager = new ForumManager(this);

        LOG.info("Initializing DashboardService");
        this.dashboardService = new DashboardService(this);

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
                .addEventListeners(new DashboardUpdater(this))
                .addEventListeners(new PriorityTagObserver(this))
                .addEventListeners(new RefreshListener(this))
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

    public @NotNull ForumManager getForumManager() {
        return this.forumManager;
    }

    public @NotNull EmojiProvider getEmojiProvider() {
        return this.emojiProvider;
    }

    public @NotNull DashboardService getDashboardService() {
        return this.dashboardService;
    }

    public @NotNull JDA getJDA() {
        return this.jda;
    }

    public @NotNull Config getConfig() {
        return this.config;
    }

    public @NotNull ForumChannel getForum() {
        ForumChannel channel = this.jda.getForumChannelById(this.config.forumChannel());
        if (channel == null)
            throw new NullPointerException("Forum channel does not exist or is not reachable");
        return channel;
    }

    public @Nullable Priority getPriority(@NotNull BaseForumTag tag) {
        String prefix = this.textProvider.get("forum.tags.priority.prefix");

        if (!tag.getName().startsWith(prefix)) return null;

        for (Priority priority : Priority.values()) {
            String name = prefix + this.textProvider.get("forum.tags.priority." + priority.name().toLowerCase());

            if (tag.getName().equals(name)) return priority;
        }

        return null;
    }
}
