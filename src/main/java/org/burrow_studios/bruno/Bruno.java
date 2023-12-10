package org.burrow_studios.bruno;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.burrow_studios.bruno.listeners.ForumListener;
import org.burrow_studios.bruno.tags.TagHelper;
import org.burrow_studios.bruno.util.ResourceUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bruno extends Thread {
    /** Directory iun which the JAR ist located. */
    public static final File DIR;
    static {
        File f;
        try {
            f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        } catch (URISyntaxException e) {
            System.out.println("Failed to declare directory");
            throw new RuntimeException(e);
        }
        DIR = f;
    }

    private final Logger logger = Logger.getLogger("Bruno");

    private final JDA jda;
    private final Properties config;
    private final long forumId;

    Bruno() throws InvalidTokenException, IllegalArgumentException, IOException {
        logger.log(Level.INFO, "Creating missing default files.");
        ResourceUtil.createDefault("config.properties");

        // Import config
        this.config = new Properties();
        this.config.load(new FileReader(new File(DIR, "config.properties")));

        try {
            this.forumId = Long.parseLong(config.getProperty("channel"));
        } catch (NumberFormatException e) {
            this.logger.log(Level.SEVERE, "Invalid channel id. Please check your config!");
            throw e;
        }
        logger.log(Level.INFO, "Forum channel id is " + forumId);

        // Instantiate JDA
        logger.log(Level.INFO, "Building JDA.");
        String token = config.getProperty("token");
        this.jda= JDABuilder.create(token,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_EMOJIS_AND_STICKERS
                )
                .addEventListeners(
                        new ForumListener(this)
                )
                .build();
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Awaiting JDA.");

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted while waiting for JDA.", e);
            jda.shutdown();
            return;
        }

        ForumChannel forum = jda.getForumChannelById(forumId);
        if (forum == null) {
            logger.log(Level.SEVERE, "Forum does not exist");
            return;
        }

        logger.log(Level.INFO, "Upserting tags.");
        TagHelper.upsertTags(forum);
        logger.log(Level.INFO, "Checking existing channels.");
        for (ThreadChannel channel : forum.getThreadChannels())
            TagHelper.checkTags(channel);

        logger.log(Level.INFO, "OK!");

        // Shut down on user input
        Scanner scanner = new Scanner(System.in);
        scanner.hasNextLine();

        logger.log(Level.INFO, "Shutting down...");

        jda.shutdown();

        logger.log(Level.INFO, "OK bye");
    }

    public long getForumId() {
        return this.forumId;
    }
}
