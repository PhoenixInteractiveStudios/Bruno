package org.burrow_studios.bruno;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.burrow_studios.bruno.util.ResourceUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class Bruno {
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

    private final JDABuilder jdaBuilder;
    private JDA jda;

    Bruno() throws InvalidTokenException, IllegalArgumentException {
        jdaBuilder= JDABuilder.create(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS
        );
    }

    void run() throws IOException {
        // Shutdown
        if (jda != null && !jda.getStatus().equals(JDA.Status.SHUTDOWN))
            jda.shutdown();

        // Write default config
        ResourceUtil.createDefault("config.properties");

        // Import config
        Properties config = new Properties();
        config.load(new FileReader(new File(DIR, "config.properties")));

        // Set bot token
        jdaBuilder.setToken(config.getProperty("token"));

        jda = jdaBuilder.build();

        jdaBuilder.setToken(null);
    }
}
