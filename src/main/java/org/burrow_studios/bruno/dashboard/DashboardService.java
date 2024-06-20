package org.burrow_studios.bruno.dashboard;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import org.burrow_studios.bruno.Bruno;
import org.burrow_studios.bruno.Priority;
import org.burrow_studios.bruno.emoji.EmojiProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DashboardService {
    private final Bruno bruno;

    public DashboardService(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    public void update() {
        ForumChannel forum = this.bruno.getForum();

        EmojiUnion assignEmoji = Emoji.fromFormatted(this.bruno.getEmojiProvider().getAssign());

        DashboardReport report = new DashboardReport(this.bruno);

        for (ThreadChannel post : forum.getThreadChannels()) {
            Priority priority = Priority.MID;

            for (ForumTag tag : post.getAppliedTags()) {
                Priority p = this.bruno.getPriority(tag);

                if (p == null) continue;

                priority = p;
                break;
            }

            ArrayList<Long> assignees = new ArrayList<>();

            MessageHistory history = post.getHistoryFromBeginning(1).complete();
            if (!history.isEmpty()) {
                Message initialMessage = history.getRetrievedHistory().get(0);

                MessageReaction reaction = initialMessage.getReaction(assignEmoji);

                if (reaction != null) {
                    List<User> users = reaction.retrieveUsers().complete();

                    for (User user : users)
                        assignees.add(user.getIdLong());
                }
            }

            report.addEntry(post, priority, assignees);
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

    public @NotNull TextChannel getChannel() {
        long channelId = this.bruno.getConfig().boardChannel();

        TextChannel channel = this.bruno.getJDA().getTextChannelById(channelId);

        if (channel == null)
            throw new NullPointerException("Board channel does not exist or is not reachable");

        return channel;
    }
}
