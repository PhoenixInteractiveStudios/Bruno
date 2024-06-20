package org.burrow_studios.bruno.forum;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.BaseForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import org.burrow_studios.bruno.Bruno;
import org.burrow_studios.bruno.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForumManager {
    private final Bruno bruno;

    private final TagHelper tagHelper;

    public ForumManager(@NotNull Bruno bruno) {
        this.bruno = bruno;

        this.tagHelper = new TagHelper(this);
    }

    public @NotNull Bruno getBruno() {
        return this.bruno;
    }

    public void checkTags() {
        for (ThreadChannel thread : this.bruno.getForum().getThreadChannels())
            this.checkTags(thread);
    }

    public void checkTags(@NotNull ThreadChannel thread) {
        this.checkTags(thread, List.of());
    }

    public void checkTags(@NotNull ThreadChannel thread, @NotNull List<ForumTag> addedTags) {
        final long expectedForum = this.bruno.getConfig().forumChannel();
        final long   actualForum = thread.getParentChannel().getIdLong();

        if (expectedForum != actualForum) return;

        this.tagHelper.checkTags(thread, addedTags);
    }

    public void upsertTags() {
        this.tagHelper.upsertTags();
    }

    @Nullable Priority getPriority(@NotNull BaseForumTag tag) {
        String prefix = this.bruno.getTextProvider().get("forum.tags.priority.prefix");

        if (!tag.getName().startsWith(prefix)) return null;

        for (Priority priority : Priority.values()) {
            String name = prefix + this.bruno.getTextProvider().get("forum.tags.priority." + priority.name().toLowerCase());

            if (tag.getName().equals(name)) return priority;
        }

        return null;
    }
}
