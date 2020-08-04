package norment.banebot.game;

public class WinLossData {
    private final int wins;
    private final int losses;

    public WinLossData(int wins, int losses) {
        this.wins = wins;
        this.losses = losses;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }
}
