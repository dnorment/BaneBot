package norment.banebot.game.connectfour;

import net.dv8tion.jda.api.entities.Activity.Emoji;

public class ConnectFourBoard {
    public static final Emoji redCircle = new Emoji("U+1F534");
    public static final Emoji blueCircle = new Emoji("U+1F535");
    public static final Emoji noCircle = new Emoji("U+26AB");

    private final Emoji[][] board = new Emoji[6][7]; //6 rows, 7 columns

    public ConnectFourBoard() {
        clearBoard();
    }

    public void clearBoard() {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                board[row][col] = noCircle;
            }
        }
    }

    public boolean canPlace(int col) {
        return board[6][col] == noCircle;
    }

    public Emoji getColorCircle(String color) {
        return color.equals("red") ? redCircle : blueCircle;
    }

    public void place(String color, int col) {
        Emoji colorCircle = getColorCircle(color);
        for (int row = 0; row <= 6; row++) {
            if (board[row][col] == noCircle) {
                board[row][col] = colorCircle;
                return;
            }
        }
    }

    public boolean hasWon(String color) {
        Emoji colorCircle = getColorCircle(color);
        return false; //TODO check win condition
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                if (board[row][col] == redCircle) {
                    sb.append("\uD83D\uDD34");
                } else if (board[row][col] == blueCircle) {
                    sb.append("\uD83D\uDD35");
                } else {
                    sb.append("\u2B24");

                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
