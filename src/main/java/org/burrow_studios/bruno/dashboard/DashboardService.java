package org.burrow_studios.bruno.dashboard;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.burrow_studios.bruno.Bruno;
import org.jetbrains.annotations.NotNull;

public class DashboardService {
    private final Bruno bruno;

    public DashboardService(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    public void update(@NotNull DashboardReport report) {
        TextChannel channel = this.getChannel();

        MessageHistory history = channel.getHistoryFromBeginning(2).complete();

        if (history.size() > 1)
            throw new IllegalArgumentException("Board channel is not empty");

        if (history.size() == 1) {
            Message existingMessage = history.getRetrievedHistory().get(0);

            if (!existingMessage.getAuthor().equals(this.bruno.getJDA().getSelfUser()))
                throw new IllegalArgumentException("Board channel contains foreign message");

            report.applyEdit(existingMessage.editMessage("")).queue();
            return;
        }

        report.applyCreate(channel.sendMessage("")).queue();
    }

    private @NotNull TextChannel getChannel() {
        long channelId = this.bruno.getConfig().boardChannel();

        TextChannel channel = this.bruno.getJDA().getTextChannelById(channelId);

        if (channel == null)
            throw new NullPointerException("Board channel does not exist or is not reachable");

        return channel;
    }
}
