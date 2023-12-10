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

    private final JDA jda;
    private final Properties config;
    private final JsonObject idCache;

    Bruno() throws InvalidTokenException, IllegalArgumentException, IOException {
        // Write default config
        ResourceUtil.createDefault("config.properties");

        // Import config
        this.config = new Properties();
        this.config.load(new FileReader(new File(DIR, "config.properties")));

        // Create idcache
        Gson gson = new Gson();
        ResourceUtil.createDefault("idcache.json");
        File idcacheFile = new File(DIR, "idcache.json");
        this.idCache = gson.fromJson(new FileReader(idcacheFile), JsonObject.class);

        // Instantiate JDA
        this.jda= JDABuilder.create(
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_EMOJIS_AND_STICKERS
                )
                .setToken(config.getProperty("token"))
                .build();
    }

    @Override
    public void run() {
        // Shut down on user input
        Scanner scanner = new Scanner(System.in);
        scanner.hasNextLine();

        jda.shutdown();
    }

    public JsonObject getIdCache() {
        return idCache;
    }
}
