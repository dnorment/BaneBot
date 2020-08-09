package norment.banebot.handler;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import norment.banebot.event.WinEvent;
import norment.banebot.game.WinLossData;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ScoreHandler {
    public static void handleScore(WinEvent event) {
        updateOutcomes(event);
    }

    public static void updateOutcomes(WinEvent event) {
        MongoCollection<Document> scoresCollection = DatabaseHandler.scoresCollection;

        Document winnerQueryDocument = new Document()
                .append("game", event.getGame())
                .append("guild", event.getGuild().getId())
                .append("user", event.getWinner().getId());

        Document loserQueryDocument = new Document()
                .append("game", event.getGame())
                .append("guild", event.getGuild().getId())
                .append("user", event.getLoser().getId());

        Document winnerDoc = scoresCollection.find(winnerQueryDocument).first();
        Document loserDoc = scoresCollection.find(loserQueryDocument).first();

        //Create documents for users if they don't exist
        if (winnerDoc == null) createUser(event.getGame(), event.getGuild(), event.getWinner());
        if (loserDoc == null) createUser(event.getGame(), event.getGuild(), event.getLoser());

        //Create update operation BSON and update fields
        Bson winnerUpdateOp = Updates.inc("wins", 1);
        Bson loserUpdateOp = Updates.inc("losses", 1);
        scoresCollection.updateOne(winnerQueryDocument, winnerUpdateOp);
        scoresCollection.updateOne(loserQueryDocument, loserUpdateOp);
    }

    public static WinLossData getWinsAndLosses(String game, Guild guild, User user) {
        MongoCollection<Document> scoresCollection = DatabaseHandler.scoresCollection;

        Document queryDocument = new Document()
                .append("game", game)
                .append("guild", guild.getId())
                .append("user", user.getId());

        Document doc = scoresCollection.find(queryDocument).first();

        int wins = 0;
        int losses = 0;

        if (doc == null) {
            createUser(game, guild, user);
        } else {
            wins = (int) doc.get("wins");
            losses = (int) doc.get("losses");
        }
        return new WinLossData(wins, losses);
    }

    private static void createUser(String game, Guild guild, User user) {
        MongoCollection<Document> scoresCollection = DatabaseHandler.scoresCollection;
        Document doc = new Document()
                .append("game", game)
                .append("guild", guild.getId())
                .append("user", user.getId());
        scoresCollection.insertOne(doc);
    }
}
