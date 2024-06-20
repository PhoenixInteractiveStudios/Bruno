package org.burrow_studios.bruno.listener;

import net.dv8tion.jda.api.events.channel.forum.ForumTagAddEvent;
import net.dv8tion.jda.api.events.channel.forum.ForumTagRemoveEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateEmojiEvent;
import net.dv8tion.jda.api.events.channel.forum.update.ForumTagUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.burrow_studios.bruno.Bruno;
import org.jetbrains.annotations.NotNull;

public class PriorityTagObserver extends ListenerAdapter {
    private final Bruno bruno;

    public PriorityTagObserver(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.bruno.getForumManager().upsertTags();
        this.bruno.getForumManager().checkTags();
    }

    @Override
    public void onSessionResume(@NotNull SessionResumeEvent event) {
        this.bruno.getForumManager().upsertTags();
        this.bruno.getForumManager().checkTags();
    }

    @Override
    public void onSessionRecreate(@NotNull SessionRecreateEvent event) {
        this.bruno.getForumManager().upsertTags();
        this.bruno.getForumManager().checkTags();
    }

    @Override
    public void onForumTagAdd(@NotNull ForumTagAddEvent event) {
        this.bruno.getForumManager().upsertTags();
    }

    @Override
    public void onForumTagRemove(@NotNull ForumTagRemoveEvent event){
        this.bruno.getForumManager().upsertTags();
    }

    @Override
    public void onForumTagUpdateName(@NotNull ForumTagUpdateNameEvent event){
        this.bruno.getForumManager().upsertTags();
    }

    @Override
    public void onForumTagUpdateEmoji(@NotNull ForumTagUpdateEmojiEvent event){
        this.bruno.getForumManager().upsertTags();
    }

    @Override
    public void onChannelUpdateAppliedTags(@NotNull ChannelUpdateAppliedTagsEvent event) {
        this.bruno.getForumManager().checkTags(event.getChannel().asThreadChannel(), event.getAddedTags());
    }
}
