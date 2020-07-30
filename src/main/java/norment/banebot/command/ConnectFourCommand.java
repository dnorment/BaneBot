package norment.banebot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ConnectFourCommand extends Command{
    @Override
    public String getCommand() {
        return "connect4";
    }

    @Override
    public String getDescription() {
        return "Play a game of Connect 4 with another user";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "connect4 `<user>`::Starts a game with `user`"
        };
    }

    @Override
    public void run(GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        String[] args = event.getMessage().getContentRaw().split("\\s+"); //split by spaces
        EmbedBuilder embed = new EmbedBuilder();

        if (args.length != 2) {
            Command.showUsage(event, this, true);
            return;
        } else {
            //TODO create game
        }
    }
}
