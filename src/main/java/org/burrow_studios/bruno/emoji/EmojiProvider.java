package org.burrow_studios.bruno.emoji;

import com.google.gson.*;
import org.burrow_studios.bruno.Priority;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmojiProvider {
    private static final Logger LOG = LoggerFactory.getLogger(EmojiProvider.class);

    private static final Gson GSON = new Gson();

    private final File file;

    private final Map<Long, String> usersOn;
    private final Map<Long, String> usersOff;
    private final String[] priorities;
    private String defaultUserOn;
    private String defaultUserOff;
    private String assign;

    public EmojiProvider(@NotNull File file) {
        this.file = file;
        this.usersOn  = new ConcurrentHashMap<>();
        this.usersOff = new ConcurrentHashMap<>();
        this.priorities = new String[Priority.values().length];

        this.load();
    }

    private void load() {
        JsonObject json = this.read();

        this.usersOn.clear();
        this.usersOff.clear();

        entries:
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue().getAsString();

            if (key.equals("assign")) {
                this.assign = val;
                continue;
            }

            if (key.startsWith("priority")) {
                for (Priority value : Priority.values()) {
                    if (!key.equals("priority." + value.name().toLowerCase())) continue;

                    this.priorities[value.ordinal()] = val;

                    continue entries;
                }
            }

            if (key.equals("user.default.on")) {
                this.defaultUserOn = val;
                continue;
            }

            if (key.equals("user.default.off")) {
                this.defaultUserOff = val;
                continue;
            }

            if (key.matches("user\\.\\d+\\.(on|off)")) {
                final String userStr = key.substring("user.".length(), key.lastIndexOf("."));
                final long   user    = Long.parseLong(userStr);

                if (key.endsWith("on"))
                    this.usersOn.put(user, val);
                else
                    this.usersOff.put(user, val);
            }
        }
    }

    private @NotNull JsonObject read() {
        try {
            return GSON.fromJson(new FileReader(file), JsonObject.class);
        } catch (FileNotFoundException e) {
            LOG.debug("emojis.json does not exist yet");
            throw new RuntimeException(e);
        } catch (JsonIOException | JsonSyntaxException e) {
            LOG.warn("Failed to read file or parse JSON", e);
            throw new RuntimeException(e);
        }
    }

    /* - - - */

    public @NotNull String getUser(long user, boolean on) {
        String emoji;

        if (on)
            emoji = this.usersOn.get(user);
        else
            emoji = this.usersOff.get(user);

        if (emoji == null)
            emoji = on ? defaultUserOn : defaultUserOff;

        return emoji;
    }

    public @NotNull String getPriority(@NotNull Priority priority) {
        return this.priorities[priority.ordinal()];
    }

    public @NotNull String getAssign() {
        return this.assign;
    }
}
