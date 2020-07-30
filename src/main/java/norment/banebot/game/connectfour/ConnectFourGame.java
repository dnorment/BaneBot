package norment.banebot.game.connectfour;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import norment.banebot.game.Game;

public class ConnectFourGame extends Game {
    private final ConnectFourBoard board = new ConnectFourBoard();
    private final User[] users = new User[2];
    private User userTurn;

    public ConnectFourGame(User firstUser, User secondUser) {
        users[0] = firstUser;
        users[1] = secondUser;

        userTurn = firstUser;
    }

    public void handleReaction(GuildMessageReactionAddEvent event) {
        //Check if the reaction is from the active turn's user
        if (event.getUser() == userTurn) {
            //TODO parse user action
        } else {
            //remove non-active player reactions
            event.getReaction().removeReaction().complete();
        }

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
