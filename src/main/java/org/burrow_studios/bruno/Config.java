package org.burrow_studios.bruno;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public record Config(
        String token,
        long forumChannel,
        long boardChannel
) {
    public static @NotNull Config fromFile(@NotNull File file) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(file));

        return fromProperties(properties);
    }

    public static @NotNull Config fromProperties(@NotNull Properties properties) {
        String token = properties.getProperty("token");
        if (token == null || token.isBlank() || token.equals("null"))
            throw new IllegalArgumentException("Token may not be null");

        String forumChannelStr = properties.getProperty("channel.forum");
        if (forumChannelStr == null)
            throw new IllegalArgumentException("channel.forum may not be null");
        long forumChannel;
        try {
            forumChannel = Long.parseLong(forumChannelStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("channel.forum must be a valid id", e);
        }

        String boardChannelStr = properties.getProperty("channel.board");
        if (boardChannelStr == null)
            throw new IllegalArgumentException("channel.board may not be null");
        long boardChannel;
        try {
            boardChannel = Long.parseLong(boardChannelStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("channel.board must be a valid id", e);
        }

        return new Config(token, forumChannel, boardChannel);
    }
}
