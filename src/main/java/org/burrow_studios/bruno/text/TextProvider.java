package org.burrow_studios.bruno.text;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class TextProvider {
    private final ConcurrentHashMap<String, String> text;

    public TextProvider(@NotNull File source) throws FileNotFoundException {
        this.text = new ConcurrentHashMap<>();

        JsonObject json = new Gson().fromJson(new FileReader(source), JsonObject.class);

        for (Map.Entry<String, JsonElement> entry : json.entrySet())
            text.put(entry.getKey(), entry.getValue().getAsString());
    }

    public @NotNull String get(@NotNull String key) throws NoSuchElementException {
        String str = this.text.get(key);
        if (str == null)
            throw new NoSuchElementException(key);
        return str;
    }

    public @NotNull String get(@NotNull String key, @NotNull String... params) throws NoSuchElementException, IllegalArgumentException {
        if (params.length % 2 != 0)
            throw new IllegalArgumentException("Must provide an even amount of params (identifier + argument)");
        String str = this.get(key);

        for (int i = 0; i < params.length; i = i + 2) {
            String identifier = params[i];
            String argument   = params[i + 1];

            str = str.replaceAll("%" + identifier + "%", argument);
        }

        return str;
    }
}
