package org.burrow_studios.bruno.listener;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.channel.forum.ForumTagAddEvent;
import net.dv8tion.jda.api.events.channel.forum.ForumTagRemoveEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateEmojiEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.burrow_studios.bruno.Bruno;
import org.burrow_studios.bruno.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PriorityTagObserver extends ListenerAdapter {
    private final Bruno bruno;

    public PriorityTagObserver(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.upsertTags();

        for (ThreadChannel channel : this.getForum().getThreadChannels())
            this.checkTags(channel);
    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent event) {
        this.upsertTags();

        for (ThreadChannel channel : this.getForum().getThreadChannels())
            this.checkTags(channel);
    }

    @Override
    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {
        this.upsertTags();

        for (ThreadChannel channel : this.getForum().getThreadChannels())
            this.checkTags(channel);
    }

    @Override
    public void onForumTagAdd(@NotNull ForumTagAddEvent event) {
        this.upsertTags();
    }

    @Override
    public void onForumTagRemove(@NotNull ForumTagRemoveEvent event) {
        this.upsertTags();
    }

    @Override
    public void onForumTagUpdateName(@NotNull ForumTagUpdateNameEvent event) {
        this.upsertTags();
    }

    @Override
    public void onForumTagUpdateEmoji(@NotNull ForumTagUpdateEmojiEvent event) {
        this.upsertTags();
    }

    @Override
    public void onChannelUpdateAppliedTags(@NotNull ChannelUpdateAppliedTagsEvent event) {
        this.checkTags(event.getChannel().asThreadChannel(), event.getAddedTags());
    }

    private void upsertTags() {
        String prefix = this.bruno.getTextProvider().get("forum.tags.priority.prefix");

        ForumChannel forum = this.getForum();

        List<BaseForumTag> tags = new ArrayList<>();
        List<ForumTag> oldTags = forum.getAvailableTags();

        priorities:
        for (Priority priority : Priority.values()) {
            String name = prefix + this.bruno.getTextProvider().get("forum.tags.priority." + priority.name().toLowerCase());
            Emoji emoji = Emoji.fromFormatted(this.bruno.getEmojiProvider().getPriority(priority));

            for (ForumTag oldTag : oldTags) {
                if (!oldTag.getName().equals(name)) continue;

                if (emoji.equals(oldTag.getEmoji())) {
                    // old tag can be kept
                    tags.add(oldTag);
                    continue priorities;
                } else {
                    // remake tag
                    break;
                }
            }

            ForumTagData tagData = new ForumTagData(name).setEmoji(emoji);
            tags.add(tagData);
        }

        outer:
        for (ForumTag oldTag : oldTags) {
            for (BaseForumTag newTag : tags)
                if (newTag.getName().equals(oldTag.getName()))
                    continue outer;
            tags.add(oldTag);
        }

        forum.getManager().setAvailableTags(tags).queue();
    }

    private void checkTags(@NotNull ThreadChannel thread) {
        this.checkTags(thread, List.of());
    }

    private void checkTags(@NotNull ThreadChannel thread, @NotNull List<ForumTag> addedTags) {
        final long expectedForum = this.bruno.getConfig().forumChannel();
        final long   actualForum = thread.getParentChannel().getIdLong();

        if (expectedForum != actualForum) return;

        List<ForumTag> tags = new ArrayList<>();

        Priority priority = null;

        for (ForumTag oldTag : thread.getAppliedTags()) {
            Priority p = this.getPriority(oldTag);

            if (p == null) {
                // non-priority tag; can be kept
                tags.add(oldTag);
                continue;
            }

            // overwrite currently cached priority if this one is higher
            if (priority == null || priority.ordinal() < p.ordinal())
                priority = p;
        }

        boolean addedTagsContainPriority = false;

        for (ForumTag addedTag : addedTags) {
            Priority p = this.getPriority(addedTag);

            if (p == null) {
                // non-priority tag; can be kept
                tags.add(addedTag);
                continue;
            }

            if (priority == null) {
                priority = p;
                addedTagsContainPriority = true;
            } else if (!addedTagsContainPriority || priority.ordinal() < p.ordinal()) {
                /*
                 * This part is reached if one of the following is true:
                 * - The added tags did not contain another priority tag before this one, in which case the old
                 *   priority should be overwritten.
                 * - The added tags did contain another priority tag before this one (2 were added at the same time)
                 *   but this one is higher and should therefore take precedence.
                 */
                priority = p;
            }
        }

        if (priority == null)
            priority = Priority.MID;

        if (!thread.isArchived()) {
            /*
             * If the required priority tag does not exist the bot will ignore priority. All tags SHOULD exist at this point
             * since calling upsertTags() would ensure that but in case that fails this would also dail, so we'll ignore it.
             */
            for (ForumTag availableTag : this.getForum().getAvailableTags()) {
                Priority p = this.getPriority(availableTag);

                if (priority == p)
                    tags.add(availableTag);
            }
        }

        // apply changes
        thread.getManager()
                .setAppliedTags(tags)
                // filter: ignore 'thread is archived' error (sync issue)
                .onErrorMap(throwable -> {
                    if (!(throwable instanceof ErrorResponseException e))
                        return false;

                    ErrorResponse error = e.getErrorResponse();
                    return error.equals(ErrorResponse.ILLEGAL_OPERATION_ARCHIVED_THREAD);
                }, throwable -> null)
                .queue();
    }

    private @Nullable Priority getPriority(@NotNull BaseForumTag tag) {
        String prefix = this.bruno.getTextProvider().get("forum.tags.priority.prefix");

        if (!tag.getName().startsWith(prefix)) return null;

        for (Priority priority : Priority.values()) {
            String name = prefix + this.bruno.getTextProvider().get("forum.tags.priority." + priority.name().toLowerCase());

            if (tag.getName().equals(name)) return priority;
        }

        return null;
    }

    private @NotNull ForumChannel getForum() {
        ForumChannel channel = this.bruno.getJDA().getForumChannelById(this.bruno.getConfig().forumChannel());
        if (channel == null)
            throw new NullPointerException("Forum channel does not exist or is not reachable");
        return channel;
    }
}
