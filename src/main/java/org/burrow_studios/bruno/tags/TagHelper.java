package org.burrow_studios.bruno.tags;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagData;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TagHelper {
    private TagHelper() { }

    public static void upsertTags(@NotNull ForumChannel forum) {
        List<ForumTag>     availableTags = forum.getAvailableTags();
        List<BaseForumTag>       newTags = new ArrayList<>(availableTags);

        // will be set to true if any changes should be applied
        boolean update = false;

        priorities:
        for (Priority priority : Priority.values()) {
            for (ForumTag tag : availableTags) {
                if (!tag.getName().equals(priority.getFullName())) continue;

                // Skip priority if tag already exists
                continue priorities;
            }

            // Priority does not exist as a tag yet.

            // Create new tag
            newTags.add(
                    new ForumTagData(priority.getFullName())
                            .setEmoji(Emoji.fromFormatted(priority.getEmote()))
            );

            // make sure the forum tags are updated
            update = true;
        }

        // return immediately if all required tags are available
        if (!update) return;

        // apply changes (blocking)
        forum.getManager().setAvailableTags(newTags).complete();
    }

    public static void checkTags(@NotNull ThreadChannel thread) {
        checkTags(thread, List.of());
    }

    public static void checkTags(@NotNull ThreadChannel thread, @NotNull List<ForumTag> addedTags) {
        if (!(thread.getParentChannel() instanceof ForumChannel forum))
            throw new IllegalArgumentException("thread is not in a forum channel");

        if (thread.isArchived()) return;

        // blocks until all required tags are available
        upsertTags(forum);

        List<ForumTag> availableTags = forum.getAvailableTags();
        List<ForumTag>       oldTags = thread.getAppliedTags();
        List<ForumTag>       newTags = new ArrayList<>();

        Priority issuePriority = null;

        tags:
        for (ForumTag oldTag : oldTags) {
            for (Priority priority : Priority.values()) {
                if (!oldTag.getName().equals(priority.getFullName())) continue;

                // overwrite currently cached priority if this one is higher
                if (issuePriority == null || issuePriority.ordinal() < priority.ordinal())
                    issuePriority = priority;

                // don't add priority tags yet to ensure only one priority will be used later
                continue tags;
            }

            newTags.add(oldTag);
        }

        boolean addedTagsContainPriority = false;

        tags:
        for (ForumTag addedTag : addedTags) {
            for (Priority priority : Priority.values()) {
                if (!addedTag.getName().equals(priority.getFullName())) continue;

                // overwrite currently cached priority if this one is higher
                if (issuePriority == null) {
                    issuePriority = priority;
                    addedTagsContainPriority = true;
                } else if (!addedTagsContainPriority || issuePriority.ordinal() < priority.ordinal()) {
                    /*
                     * This part is reached if one of the following is true:
                     * - The added tags did not contain another priority tag before this one, in which case the old
                     *   priority should be overwritten.
                     * - The added tags did contain another priority tag before this one (2 were added at the same time)
                     *   but this one is higher and should therefore take precedence.
                     */
                    issuePriority = priority;
                }

                // don't add priority tags yet to ensure only one priority will be used later
                continue tags;
            }

            newTags.add(addedTag);
        }

        if (issuePriority == null)
            issuePriority = Priority.NORMAL;

        if (!thread.isArchived()) {
            /*
             * If the required priority tag does not exist the bot will ignore priority. All tags SHOULD exist at this point
             * since calling upsertTags() would ensure that but in case that fails this would also fail, so we'll ignore it.
             */
            for (ForumTag availableTag : availableTags) {
                if (!availableTag.getName().equals(issuePriority.getFullName())) continue;

                newTags.add(availableTag);
                break;
            }
        }

        // apply changes
        thread.getManager()
                .setAppliedTags(newTags)
                // filter: ignore 'thread is archived' error (synchronization issue)
                .onErrorMap(throwable -> {
                    if (!(throwable instanceof ErrorResponseException ex))
                        return false;

                    ErrorResponse error = ex.getErrorResponse();
                    return error.equals(ErrorResponse.ILLEGAL_OPERATION_ARCHIVED_THREAD);
                    }, throwable -> null
                ).queue();
    }

    public static ThreadChannelManager removePriorities(ThreadChannel thread) {
        List<ForumTag> oldTags = thread.getAppliedTags();
        List<ForumTag> newTags = new ArrayList<>();

        priorities:
        for (ForumTag tag : oldTags) {
            for (Priority priority : Priority.values()) {
                if (!tag.getName().equals(priority.getFullName())) continue;

                continue priorities;
            }

            newTags.add(tag);
        }

        if (newTags.size() == oldTags.size()) return thread.getManager();

        return thread.getManager().setAppliedTags(newTags);
    }
}
