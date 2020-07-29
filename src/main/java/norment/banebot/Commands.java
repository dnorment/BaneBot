package norment.banebot;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        //Check if message starts with prefix
        if (event.getMessage().getContentRaw().startsWith(Bane.prefix)) {
            //Send command to handler
            handleCommand(event);
        }
    }

    public void handleCommand(GuildMessageReceivedEvent event) {
        //Split command into args
        String[] args = event.getMessage().getContentRaw().split("\\s+"); //split by spaces
        String command = args[0].replace(Bane.prefix, "");

        //Handle command
        switch (command) {
            case "ping":
                event.getChannel().sendMessage("pong").queue();
                break;
            default:
                break;
        }
    }

}
