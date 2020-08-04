package norment.banebot.command;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import norment.banebot.game.WinLossData;
import norment.banebot.handler.ScoreHandler;

public class ScoreCommand extends Command {
    @Override
    public String getCommand() {
        return "score";
    }

    @Override
    public String getDescription() {
        return "Checks your score for a game";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "score `<game>`::Check your score in `game`, ex. `connect4`"
        };
    }

    @Override
    public void run(GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        String[] args = event.getMessage().getContentRaw().split("\\s+"); //split by spaces

        if (args.length != 2) {
            showUsage(event, this, true);
            return;
        }

        if (args[1].equals("connect4")) {
            WinLossData data = ScoreHandler.getWinsAndLosses(args[1], event.getGuild(), event.getAuthor());
            channel.sendMessage(String.format("%d wins, %d losses", data.getWins(), data.getLosses())).queue();
        } else {
            showUsage(event, this, true);
        }
    }
}
