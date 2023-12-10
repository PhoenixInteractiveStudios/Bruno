package org.burrow_studios.bruno.listeners;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.burrow_studios.bruno.Bruno;
import org.burrow_studios.bruno.tags.TagHelper;
import org.jetbrains.annotations.NotNull;

public class ForumListener extends ListenerAdapter {
    private final Bruno bruno;

    public ForumListener(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    @Override
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {
        if (!event.isFromType(ChannelType.GUILD_PUBLIC_THREAD)) return;

        ThreadChannel channel = event.getChannel().asThreadChannel();

        IThreadContainerUnion parentChannel = channel.getParentChannel();
        if (!parentChannel.getType().equals(ChannelType.FORUM)) return;
        if (parentChannel.getIdLong() != bruno.getForumId()) return;

        TagHelper.checkTags(channel);
    }

    @Override
    public void onChannelUpdateAppliedTags(@NotNull ChannelUpdateAppliedTagsEvent event) {
        // No checks required since ChannelUpdateAppliedTagsEvents are limited to thread channels inside forum channels
        TagHelper.checkTags(event.getChannel().asThreadChannel(), event.getAddedTags());
    }
}
