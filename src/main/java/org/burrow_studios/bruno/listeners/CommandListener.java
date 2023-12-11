package org.burrow_studios.bruno.listeners;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.burrow_studios.bruno.Bruno;
import org.burrow_studios.bruno.tags.TagHelper;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {
    public static final String COMMAND_CLOSE = "close";

    private final Bruno bruno;

    public CommandListener(@NotNull Bruno bruno) {
        this.bruno = bruno;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getInteraction().getName().equals(COMMAND_CLOSE)) return;

        if (!event.getChannelType().equals(ChannelType.GUILD_PUBLIC_THREAD)) {
            event.reply(":warning: This command can only be used in forum posts.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        ThreadChannel channel = event.getChannel().asThreadChannel();

        if (!channel.getParentChannel().getType().equals(ChannelType.FORUM)) {
            event.reply(":warning: This command can only be used in forum posts.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        ForumChannel forum = channel.getParentChannel().asForumChannel();

        if (forum.getIdLong() != bruno.getForumId()) {
            event.reply(":warning: This thread is not handled by the bot.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        RestAction.allOf(
                event.deferReply(false),
                TagHelper.removePriorities(channel),
                event.getHook().sendMessage("Issue closed"),
                channel.getManager().setArchived(true)
        ).queue();
    }
}
