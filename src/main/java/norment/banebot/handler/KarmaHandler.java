package norment.banebot.handler;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.entities.Activity.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.bson.Document;

import java.util.HashMap;

public class KarmaHandler {
    public static HashMap<Guild, Emoji> upvoteEmojis;
    public static HashMap<Guild, Emoji> downvoteEmojis;

    public static void init() {
        MongoCollection<Document> karmaCollection = DatabaseHandler.karmaCollection;
    }

    public static void handleReaction(GuildMessageReactionAddEvent event) {

    }

    public static boolean isKarmaReaction(GuildMessageReactionAddEvent event) {
        return false;
    }
}
