package norment.banebot.event;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class WinEvent {
    private final String game;
    private final Guild guild;
    private final User winner;
    private final User loser;

    public WinEvent(String game, Guild guild, User winner, User loser) {
        this.game = game;
        this.guild = guild;
        this.winner = winner;
        this.loser = loser;
    }

    public String getGame() {
        return game;
    }

    public Guild getGuild() {
        return guild;
    }

    public User getWinner() {
        return winner;
    }

    public User getLoser() {
        return loser;
    }

    @Override
    public String toString() {
        return "Winner: " + winner.getAsMention() + ", Loser: " + loser.getAsMention();
    }
}
