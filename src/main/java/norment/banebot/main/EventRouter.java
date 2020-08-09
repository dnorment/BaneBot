package norment.banebot.main;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import norment.banebot.handler.CommandHandler;
import norment.banebot.handler.DatabaseHandler;
import norment.banebot.handler.KarmaHandler;
import norment.banebot.handler.ReactionHandler;

public class EventRouter extends ListenerAdapter {

    public EventRouter(JDA jda) {
        CommandHandler.init();
        DatabaseHandler.init();
        KarmaHandler.init(jda);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        //Ignore all bots
        if (event.getAuthor().isBot()) return;

        //Check if message starts with prefix, if so, treat the message as command
        if (event.getMessage().getContentRaw().startsWith(BaneBot.prefix)) {
            CommandHandler.handleCommand(event);
        }
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        //Ignore bot reactions
        if (event.getUser().isBot()) return;

        //Pass non-bot reactions to reaction handler
        ReactionHandler.handleAddReaction(event);
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        if (event.getUser() == null) return;

        //Ignore bot reaction removals
        if (event.getUser().isBot()) return;

        //Pass non-bot reaction removals to reaction handler
        ReactionHandler.handleRemoveReaction(event);

    }
}
