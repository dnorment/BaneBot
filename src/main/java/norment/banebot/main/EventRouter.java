package norment.banebot.main;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import norment.banebot.handler.CommandHandler;
import norment.banebot.handler.DatabaseHandler;
import norment.banebot.handler.ReactionHandler;

public class EventRouter extends ListenerAdapter {

    public EventRouter() {
        CommandHandler.init();
        DatabaseHandler.init();
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

        //Check if reacting to an embed message from this bot and pass to ReactionHandler
        String messageId = event.getMessageId();
        Message message = event.getChannel().retrieveMessageById(messageId).complete();

        if (!message.getEmbeds().isEmpty() && message.getAuthor() == event.getJDA().getSelfUser()) {
            ReactionHandler.handleReaction(event);
        }
    }
}
