package norment.banebot.handler;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import norment.banebot.event.WinEvent;
import norment.banebot.game.WinLossData;
import org.bson.Document;

public class ScoreHandler {
    public static void handleScore(WinEvent event) {
        String game = event.getGame();
        Guild guild = event.getGuild();
    }
}
