package norment.banebot.main;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventRouter extends ListenerAdapter {

    public EventRouter() {
        CommandHandler.init();
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
}
