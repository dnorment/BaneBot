package norment.banebot.game.connectfour;

import net.dv8tion.jda.api.entities.Activity.Emoji;

public class ConnectFourBoard {
    public static final Emoji redCircle = new Emoji("U+1F534");
    public static final Emoji blueCircle = new Emoji("U+1F535");
    public static final Emoji noCircle = new Emoji("U+26AB");

    private final int ROWS = 6;
    private final int COLS = 7;
    private final Emoji[][] board = new Emoji[ROWS][COLS]; //6 rows, 7 columns

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
        return board[board.length - 1][col].equals(noCircle);
    }

    public Emoji getColorCircle(String color) {
        return color.equals("red") ? redCircle : blueCircle;
    }

    public void place(String color, int col) {
        Emoji colorCircle = getColorCircle(color);
        for (int row = 0; row < board.length; row++) {
            if (board[row][col].equals(noCircle)) {
                board[row][col] = colorCircle;
                return;
            }
        }
    }

    public boolean hasWon(String color) {
        Emoji colorCircle = getColorCircle(color);

        //check horizontal win
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                if (board[row][col].equals(colorCircle) &&
                        board[row][col + 1].equals(colorCircle) &&
                        board[row][col + 2].equals(colorCircle) &&
                        board[row][col + 3].equals(colorCircle)) {
                    return true;
                }
            }
        }

        //check vertical win
        for (int row = 0; row < ROWS - 3; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col].equals(colorCircle) &&
                        board[row + 1][col].equals(colorCircle) &&
                        board[row + 2][col].equals(colorCircle) &&
                        board[row + 3][col].equals(colorCircle)) {
                    return true;
                }
            }
        }

        //check ascending diagonal win
        for (int row = 0; row < ROWS - 3; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                if (board[row][col].equals(colorCircle) &&
                        board[row + 1][col + 1].equals(colorCircle) &&
                        board[row + 2][col + 2].equals(colorCircle) &&
                        board[row + 3][col + 3].equals(colorCircle)) {
                    return true;
                }
            }
        }

        //check descending diagonal win
        for (int row = 3; row < ROWS; row++) {
            for (int col = 0; col < COLS - 3; col++) {
                if (board[row][col].equals(colorCircle) &&
                        board[row - 1][col + 1].equals(colorCircle) &&
                        board[row - 2][col + 2].equals(colorCircle) &&
                        board[row - 3][col + 3].equals(colorCircle)) {
                    return true;
                }
            }
        }

        //no win condition met
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        //create string from board with unicode circles
        for (int row = board.length - 1; row >= 0; row--) {
            for (int col = 0; col < board[0].length; col++) {
                if (board[row][col].equals(redCircle)) {
                    sb.append("\uD83D\uDD34");
                } else if (board[row][col].equals(blueCircle)) {
                    sb.append("\uD83D\uDD35");
                } else {
                    sb.append("\u26AB");
                }
            }
            sb.append("\n");
        }
        //append 1-7 below grid
        for (int i = 1; i <= 7; i++) {
            sb.append(i).append("\uFE0F\u20E3");
        }

        return sb.toString();

    }
}
