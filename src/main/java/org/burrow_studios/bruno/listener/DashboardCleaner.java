package org.burrow_studios.bruno.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.burrow_studios.bruno.Bruno;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardCleaner extends ListenerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(DashboardCleaner.class);

    private final Bruno bruno;

    public DashboardCleaner(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final long expectedChannel = this.bruno.getConfig().boardChannel();
        final long   actualChannel = event.getChannel().getIdLong();

        if (expectedChannel != actualChannel) return;

        if (event.getAuthor().equals(event.getJDA().getSelfUser())) return;

        LOG.warn("Received a message in the board channel");

        event.getMessage().delete().queue();
    }
}
