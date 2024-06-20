package org.burrow_studios.bruno.listener;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.burrow_studios.bruno.Bruno;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class RefreshListener extends ListenerAdapter {
    public static final String REFRESH_BUTTON_ID = "refresh";

    private final Bruno bruno;

    public RefreshListener(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getChannel().equals(this.bruno.getDashboardService().getChannel())) return;
        if (!REFRESH_BUTTON_ID.equals(event.getButton().getId())) return;

        event.deferReply(true).complete();

        this.bruno.getDashboardService().update();

        event.getHook().deleteOriginal().queueAfter(1, TimeUnit.SECONDS);
    }
}
