package norment.banebot.handler;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import org.bson.Document;
import org.bson.conversions.Bson;

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

    public static void handleAddReaction(GuildMessageReactionAddEvent event) {
        //Get vote type and update karma
        ReactionEmote reactionEmote = event.getReactionEmote();
        Guild guild = event.getGuild();
        boolean isUpvote = reactionEmote.equals(upvoteReactions.get(guild));
        boolean isDownvote = reactionEmote.equals(downvoteReactions.get(guild));

        //Ignore if not a karma reaction
        if (!isUpvote && !isDownvote) return;

        //Ignore votes on self
        Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
        if (event.getUser().equals(message.getAuthor())) return;

        //Find document of message author
        MongoCollection<Document> karmaCollection = DatabaseHandler.karmaCollection;
        Document queryDocument = new Document()
                .append("guild", guild.getId())
                .append("user", message.getAuthor().getId());
        Document userDocument = karmaCollection.find(queryDocument).first();

        //Create user document if doesn't exist
        if (userDocument == null) createUser(event.getGuild(), message.getAuthor());

        //Create update operation and update karma
        Bson updateOp = Updates.inc("karma", isUpvote ? 1 : -1);
        karmaCollection.updateOne(queryDocument, updateOp);
    }

    public static void handleRemoveReaction(GuildMessageReactionRemoveEvent event) {
        if (event.getUser() == null) return;

        //Get vote type
        ReactionEmote reactionEmote = event.getReactionEmote();
        Guild guild = event.getGuild();
        boolean isUpvote = reactionEmote.equals(upvoteReactions.get(guild));
        boolean isDownvote = reactionEmote.equals(downvoteReactions.get(guild));

        //Ignore if not a karma reaction
        if (!isUpvote && !isDownvote) return;

        //Ignore votes on self
        Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
        if (event.getUser().equals(message.getAuthor())) return;

        //Find document of message author
        MongoCollection<Document> karmaCollection = DatabaseHandler.karmaCollection;
        Document queryDocument = new Document()
                .append("guild", guild.getId())
                .append("user", message.getAuthor().getId());

        //Create update operation and update karma
        Bson updateOp = Updates.inc("karma", isUpvote ? -1 : 1);
        karmaCollection.updateOne(queryDocument, updateOp);
    }

    private static void createUser(Guild guild, User user) {
        MongoCollection<Document> karmaCollection = DatabaseHandler.karmaCollection;
        Document doc = new Document()
                .append("guild", guild.getId())
                .append("user", user.getId());
        karmaCollection.insertOne(doc);
    }

    public static int getKarma(Guild guild, User user) {
        MongoCollection<Document> karmaCollection = DatabaseHandler.karmaCollection;
        Document queryDocument = new Document()
                .append("guild", guild.getId())
                .append("user", user.getId());
        Document userDocument = karmaCollection.find(queryDocument).first();

        int karma = 0;

        if (userDocument == null) {
            return karma;
        } else {
            return userDocument.getInteger("karma");
        }
    }
}
