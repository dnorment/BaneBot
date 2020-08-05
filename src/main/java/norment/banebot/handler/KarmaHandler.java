package norment.banebot.handler;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.bson.Document;

import java.util.HashMap;

public class KarmaHandler {
    public static HashMap<Guild, ReactionEmote> upvoteReactions = new HashMap<>();
    public static HashMap<Guild, ReactionEmote> downvoteReactions = new HashMap<>();

    public static void init(JDA jda) {
        loadReactions(jda);
    }

    private static void loadReactions(JDA jda) {
        MongoCollection<Document> reactionsCollection = DatabaseHandler.reactionsCollection;
        for (Document doc : reactionsCollection.find()) {
            String guildId = doc.get("guild").toString();
            String upvoteId = doc.get("upvote").toString();
            String downvoteId = doc.get("downvote").toString();

            Guild guild = jda.getGuildById(guildId);
            if (guild == null) continue;

            boolean upvoteRegistered = false;
            boolean downvoteRegistered = false;

            //check if upvote reaction is a custom emote from guild
            for (int i = 0; i < guild.getEmotes().size(); i++) {
                Emote emote = guild.getEmotes().get(i);
                if (emote.getId().equals(upvoteId)) {
                    ReactionEmote upvoteReaction = ReactionEmote.fromCustom(emote);
                    upvoteReactions.put(guild, upvoteReaction);
                    upvoteRegistered = true;
                    break;
                }
            }
            //is not a custom reaction, treat as unicode emoji
            if (!upvoteRegistered) {
                ReactionEmote upvoteReaction = ReactionEmote.fromUnicode(upvoteId, jda);
                upvoteReactions.put(guild, upvoteReaction);
            }

            //repeat for downvote
            for (int i = 0; i < guild.getEmotes().size(); i++) {
                Emote emote = guild.getEmotes().get(i);
                if (emote.getId().equals(downvoteId)) {
                    ReactionEmote downvoteReaction = ReactionEmote.fromCustom(emote);
                    downvoteReactions.put(guild, downvoteReaction);
                    downvoteRegistered = true;
                    break;
                }
            }
            if (!downvoteRegistered) {
                ReactionEmote downvoteReaction = ReactionEmote.fromUnicode(downvoteId, jda);
                upvoteReactions.put(guild, downvoteReaction);
            }
        }
    }

    public static void handleReaction(GuildMessageReactionAddEvent event) {

    }

    public static boolean isKarmaReaction(GuildMessageReactionAddEvent event) {
        ReactionEmote reactionEmote = event.getReactionEmote();
        Guild guild = event.getGuild();

        //check if reaction is registered for voting
        boolean isUpvote = reactionEmote.equals(upvoteReactions.get(guild));
        boolean isDownvote = reactionEmote.equals(downvoteReactions.get(guild));
        return isUpvote || isDownvote;
    }
}
