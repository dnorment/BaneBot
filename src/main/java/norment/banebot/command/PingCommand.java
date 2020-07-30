package norment.banebot.command;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class PingCommand extends Command {
    @Override
    public String getCommand() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "Responds with 'Pong!'";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "ping::Ping the bot"
        };
    }

    @Override
    public void run(GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        channel.sendMessage("Pong!").queue();
    }
}
