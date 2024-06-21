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
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.burrow_studios.bruno.Bruno;
import org.burrow_studios.bruno.Priority;
import org.burrow_studios.bruno.emoji.EmojiProvider;
import org.burrow_studios.bruno.listener.RefreshListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        String boardTitle = this.bruno.getTextProvider().get("board.title");

        ForumChannel  forum = this.bruno.getForum();
        ThreadChannel board = this.getThread();

        Button refreshButton = Button.of(ButtonStyle.SECONDARY, RefreshListener.REFRESH_BUTTON_ID, this.bruno.getTextProvider().get("board.refresh"));

        if (board == null) {
            // board post does not exist yet; create one
            board = forum.createForumPost(boardTitle, MessageCreateData.fromContent(report.getContent()))
                    .setActionRow(refreshButton)
                    .complete().getThreadChannel();
        } else {
            // board post exists; update it

            Message message = board.retrieveStartMessage().complete();

            message.editMessage(report.getContent())
                    .setActionRow(refreshButton)
                    .queue();
        }

        board.getManager().setPinned(true).queue();
    }

    public @Nullable ThreadChannel getThread() {
        String boardTitle = this.bruno.getTextProvider().get("board.title");

        ForumChannel  forum = this.bruno.getForum();
        ThreadChannel board = null;

        for (ThreadChannel thread : forum.getThreadChannels()) {
            if (thread.getOwnerIdLong() != forum.getJDA().getSelfUser().getIdLong()) continue;
            if (!thread.getName().equals(boardTitle)) continue;

            board = thread;
        }

        return board;
    }
}
