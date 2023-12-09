package org.burrow_studios.bruno;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.util.Scanner;

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

    private JsonObject idCache;

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

        // Create idcache
        Gson gson = new Gson();
        ResourceUtil.createDefault("idcache.json");
        File idcacheFile = new File(DIR, "idcache.json");
        idCache = gson.fromJson(new FileReader(idcacheFile), JsonObject.class);


        // Set bot token
        jdaBuilder.setToken(config.getProperty("token"));

        jda = jdaBuilder.build();

        jdaBuilder.setToken(null);


        // Shut down on user input
        Scanner scanner = new Scanner(System.in);
        scanner.hasNextLine();

        jda.shutdown();
    }

    public JsonObject getIdCache() {
        return idCache;
    }
}
