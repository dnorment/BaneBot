package norment.banebot.handler;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.bson.Document;

import java.util.HashMap;

public class KarmaHandler {
    public static HashMap<Guild, ReactionEmote> upvoteReactions = new HashMap<>();
    public static HashMap<Guild, ReactionEmote> downvoteReactions = new HashMap<>();

    public static void init(JDA jda) {
        MongoCollection<Document> karmaCollection = DatabaseHandler.karmaCollection;
    }

    public static void handleReaction(GuildMessageReactionAddEvent event) {

    }

    public static boolean isKarmaReaction(GuildMessageReactionAddEvent event) {
        ReactionEmote reactionEmote = event.getReactionEmote();
        Guild guild = event.getGuild();

        //check if reaction is registered for voting
        if (reactionEmote.equals(upvoteReactions.get(guild))) {
            return true;
        } else if (reactionEmote.equals(downvoteReactions.get(guild))) {
            return true;
        } else {
            return false;
        }
    }
}
