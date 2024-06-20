package org.burrow_studios.bruno.forum;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.burrow_studios.bruno.Priority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class TagHelper {
    private final ForumManager forumManager;

    public TagHelper(@NotNull ForumManager forumManager) {
        this.forumManager = forumManager;
    }

    public void checkTags(@NotNull ThreadChannel thread, @NotNull List<ForumTag> addedTags) {
        List<ForumTag> tags = new ArrayList<>();

        Priority priority = null;

        for (ForumTag oldTag : thread.getAppliedTags()) {
            Priority p = this.forumManager.getPriority(oldTag);

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
            Priority p = this.forumManager.getPriority(addedTag);

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
            for (ForumTag availableTag : this.forumManager.getForum().getAvailableTags()) {
                Priority p = this.forumManager.getPriority(availableTag);

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

    public void upsertTags() {
        String prefix = this.forumManager.getBruno().getTextProvider().get("forum.tags.priority.prefix");

        ForumChannel forum = this.forumManager.getForum();

        List<BaseForumTag> tags = new ArrayList<>();
        List<ForumTag> oldTags = forum.getAvailableTags();

        priorities:
        for (Priority priority : Priority.values()) {
            String name = prefix + this.forumManager.getBruno().getTextProvider().get("forum.tags.priority." + priority.name().toLowerCase());
            Emoji emoji = Emoji.fromFormatted(this.forumManager.getBruno().getEmojiProvider().getPriority(priority));

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
}
