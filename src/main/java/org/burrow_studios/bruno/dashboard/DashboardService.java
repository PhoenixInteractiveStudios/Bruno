package org.burrow_studios.bruno.dashboard;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import org.burrow_studios.bruno.Bruno;
import org.burrow_studios.bruno.Priority;
import org.jetbrains.annotations.NotNull;

public class DashboardService {
    private final Bruno bruno;

    public DashboardService(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    public void update() {
        final String pPrefix = this.bruno.getTextProvider().get("forum.tags.priority.prefix");
        final String pLow     = pPrefix + this.bruno.getTextProvider().get("forum.tags.priority.low");
        final String pHigh    = pPrefix + this.bruno.getTextProvider().get("forum.tags.priority.high");
        final String pHighest = pPrefix + this.bruno.getTextProvider().get("forum.tags.priority.highest");

        ForumChannel forum = this.getForum();

        DashboardReport report = new DashboardReport(this.bruno);

        for (ThreadChannel post : forum.getThreadChannels()) {
            Priority priority = Priority.MID;

            for (ForumTag tag : post.getAppliedTags()) {
                if (tag.getName().equals(pLow)) {
                    priority = Priority.LOW;
                    break;
                }
                if (tag.getName().equals(pHigh)) {
                    priority = Priority.HIGH;
                    break;
                }
                if (tag.getName().equals(pHighest)) {
                    priority = Priority.HIGHEST;
                    break;
                }
            }

            report.addEntry(post, priority);
        }

        this.bruno.getDashboardService().update(report);
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

    private @NotNull ForumChannel getForum() {
        ForumChannel channel = this.bruno.getJDA().getForumChannelById(this.bruno.getConfig().forumChannel());
        if (channel == null)
            throw new NullPointerException("Forum channel does not exist or is not reachable");
        return channel;
    }

    private @NotNull TextChannel getChannel() {
        long channelId = this.bruno.getConfig().boardChannel();

        TextChannel channel = this.bruno.getJDA().getTextChannelById(channelId);

        if (channel == null)
            throw new NullPointerException("Board channel does not exist or is not reachable");

        return channel;
    }
}
