package org.burrow_studios.bruno.listener;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.events.thread.ThreadHiddenEvent;
import net.dv8tion.jda.api.events.thread.ThreadRevealedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.burrow_studios.bruno.Bruno;
import org.jetbrains.annotations.NotNull;

public class DashboardUpdater extends ListenerAdapter {
    private final Bruno bruno;

    public DashboardUpdater(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        for (ThreadChannel channel : this.bruno.getForum().getThreadChannels())
            channel.join().queue();

        this.bruno.getDashboardService().update();
    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent event) {
        for (ThreadChannel channel : this.bruno.getForum().getThreadChannels())
            channel.join().queue();

        this.bruno.getDashboardService().update();
    }

    @Override
    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {
        for (ThreadChannel channel : this.bruno.getForum().getThreadChannels())
            channel.join().queue();

        this.bruno.getDashboardService().update();
    }

    @Override
    public void onChannelCreate(@NotNull ChannelCreateEvent event) {
        this.onChannelEvent(event.getChannel());
    }

    @Override
    public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
        this.onChannelEvent(event.getChannel());
    }

    @Override
    public void onThreadRevealed(@NotNull ThreadRevealedEvent event) {
        this.onChannelEvent(event.getThread());
    }

    @Override
    public void onThreadHidden(@NotNull ThreadHiddenEvent event) {
        this.onChannelEvent(event.getThread());
    }

    @Override
    public void onChannelUpdateArchived(@NotNull ChannelUpdateArchivedEvent event) {
        if (Boolean.TRUE.equals(event.getNewValue())) return;

        this.onChannelEvent(event.getChannel());
    }

    @Override
    public void onChannelUpdateAppliedTags(@NotNull ChannelUpdateAppliedTagsEvent event) {
        ThreadChannel thread = event.getChannel().asThreadChannel();

        final long expectedForum = this.bruno.getConfig().forumChannel();
        final long   actualForum = thread.getParentChannel().getIdLong();

        if (expectedForum != actualForum) return;

        this.bruno.getDashboardService().update();
    }

    @Override
    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent event) {
        if (!event.getChannelType().isThread()) return;

        IThreadContainerUnion threadContainer = event.getChannel().asThreadChannel().getParentChannel();

        final long expectedForum = this.bruno.getConfig().forumChannel();
        final long   actualForum = threadContainer.getIdLong();

        if (expectedForum != actualForum) return;

        this.bruno.getDashboardService().update();
    }

    private void onChannelEvent(@NotNull ChannelUnion channel) {
        if (!channel.getType().isThread()) return;
        ThreadChannel thread = channel.asThreadChannel();

        this.onChannelEvent(thread);
    }

    private void onChannelEvent(@NotNull ThreadChannel channel) {
        IThreadContainerUnion container = channel.getParentChannel();
        if (!container.getType().equals(ChannelType.FORUM)) return;

        final long expectedForum = this.bruno.getConfig().forumChannel();
        final long   actualForum = container.asForumChannel().getIdLong();

        if (expectedForum != actualForum) return;

        channel.join().queue();

        this.bruno.getDashboardService().update();
    }
}
