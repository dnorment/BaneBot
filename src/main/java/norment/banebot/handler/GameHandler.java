package norment.banebot.handler;

import norment.banebot.game.Game;
import norment.banebot.game.ReactionGame;

import java.util.HashMap;

public class GameHandler {
    private static final HashMap<Integer, Game> games = new HashMap<>();

    public static void addGame(Game game) {
        games.put(game.hashCode(), game);
    }

    public static void removeGame(Game game) {
        games.remove(game.hashCode());
    }

    public static Game getGame(int id) {
        return games.get(id);
    }

    public static ReactionGame getReactionGame(int id) {
        if (games.get(id) instanceof ReactionGame) {
            return (ReactionGame) games.get(id);
        }
        return null;
    }

}
