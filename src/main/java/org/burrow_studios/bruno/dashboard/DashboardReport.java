package org.burrow_studios.bruno.dashboard;

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.burrow_studios.bruno.Bruno;
import org.burrow_studios.bruno.Priority;
import org.burrow_studios.bruno.listener.RefreshListener;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class DashboardReport {
    private final Bruno bruno;

    private final SortedSet<Entry> entries;

    public DashboardReport(@NotNull Bruno bruno) {
        this.bruno = bruno;

        this.entries = new TreeSet<>(Comparator.comparing(entry -> entry.channel().getTimeCreated().toInstant()));
    }

    public void addEntry(@NotNull ThreadChannel post, @NotNull Priority priority) {
        this.entries.add(new Entry(post, priority));
    }

    @NotNull MessageCreateAction applyCreate(@NotNull MessageCreateAction action) {
        action.setContent(this.getContent());
        action.setActionRow(Button.of(ButtonStyle.SECONDARY, RefreshListener.REFRESH_BUTTON_ID, this.bruno.getTextProvider().get("board.refresh")));
        return action;
    }

    @NotNull MessageEditAction applyEdit(@NotNull MessageEditAction action) {
        action.setContent(this.getContent());
        action.setActionRow(Button.of(ButtonStyle.SECONDARY, RefreshListener.REFRESH_BUTTON_ID, this.bruno.getTextProvider().get("board.refresh")));
        return action;
    }

    private record Entry(
            @NotNull ThreadChannel channel,
            @NotNull Priority priority
    ) { }

    public @NotNull String getContent() {
        StringBuilder builder = new StringBuilder();

        builder.append("# ");
        builder.append(this.bruno.getTextProvider().get("board.header"));

        entries.forEach(entry -> {
            builder.append("\n");
            builder.append(this.bruno.getEmojiProvider().getPriority(entry.priority()));
            builder.append("   ");
            builder.append(entry.channel().getAsMention());
        });

        return builder.toString();
    }
}
