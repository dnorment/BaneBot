package norment.banebot.game.connectfour;

import net.dv8tion.jda.api.entities.Activity.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import norment.banebot.game.ReactionGame;

public class ConnectFourGame extends ReactionGame {
    private final ConnectFourBoard board = new ConnectFourBoard();
    private final User[] users = new User[2];
    private int turn = 0;

    public ConnectFourGame(User firstUser, User secondUser) {
        users[0] = firstUser;
        users[1] = secondUser;
    }

    public void handleReaction(GuildMessageReactionAddEvent event) {
        //Check if the reaction is from the active turn's user
        User user = event.getUser();
        if (user == users[turn%2]) {
            //get circle emoji of current player
            Emoji colorCircle = board.getColorCircle(user == users[0] ? "red" : "blue");

            //TODO parse user action
        } else {
            //remove non-active player reactions
            event.getReaction().removeReaction(event.getUser()).complete();
        }
        updateEmbed(event);
    }

    public void updateEmbed(GuildMessageReactionAddEvent event) {
        String messageId = event.getMessageId();
        Message message = event.getChannel().retrieveMessageById(messageId).complete();
        MessageEmbed embed = message.getEmbeds().get(0);
        //TODO update the embed
    }

    public ConnectFourBoard getBoard() {
        return board;
    }

    @Override
    public String getGame() {
        return "Connect 4";
    }

    @Override
    public int getGameId() {
        return this.hashCode();
    }
}
