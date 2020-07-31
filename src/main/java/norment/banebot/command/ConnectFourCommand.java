package norment.banebot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import norment.banebot.game.connectfour.ConnectFourGame;
import norment.banebot.handler.GameHandler;

public class ConnectFourCommand extends Command {
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
            //get users and create game then add to active games
            User firstUser = event.getAuthor();
            //check that user argument is a mentioned user (not bot)
            if (event.getMessage().getMentionedUsers().size() < 1 || event.getMessage().getMentionedUsers().get(0).isBot()) {
                Command.showUsage(event, this, true);
                return;
            }
            User secondUser = event.getMessage().getMentionedUsers().get(0);
            ConnectFourGame game = new ConnectFourGame(firstUser, secondUser);
            GameHandler.addGame(game);

            //create game embed
            embed.setTitle(String.format("Connect 4%n%s vs. %s", firstUser.getName(), secondUser.getName()))
                    .setColor(ConnectFourGame.RED_CIRCLE)
                    .setDescription(game.getBoard().toString())
                    .setFooter(String.format("Game: Connect 4 (#%d)", game.hashCode()));
        }

        Message gameMessage = channel.sendMessage(embed.build()).complete();

        //add the numbers 1-7 as emotes on the message
        for (int i = 1; i <= 7; i++) {
            gameMessage.addReaction(String.format("U+3%dU+fe0fU+20e3", i)).queue();
        }
    }
}
