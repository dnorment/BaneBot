package norment.banebot.main;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import norment.banebot.handler.CommandHandler;
import norment.banebot.handler.DatabaseHandler;
import norment.banebot.handler.KarmaHandler;
import norment.banebot.handler.ReactionHandler;

public class EventRouter extends ListenerAdapter {

    public EventRouter() {
        CommandHandler.init();
        DatabaseHandler.init();
        KarmaHandler.init();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        //Ignore all bots
        if (event.getAuthor().isBot()) return;

        //Check if message starts with prefix, treat as command
        if (event.getMessage().getContentRaw().startsWith(BaneBot.prefix)) {
            CommandHandler.handleCommand(event);
        }
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        //Ignore bot reactions
        if (event.getUser().isBot()) return;

        //Pass non-bot reactions to reaction handler
        ReactionHandler.handleReaction(event);
    }
}
