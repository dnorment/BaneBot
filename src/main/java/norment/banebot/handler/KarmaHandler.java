package norment.banebot.handler;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.HashMap;
import java.util.List;

public class KarmaHandler {
    public static HashMap<Guild, ReactionEmote> upvoteReactions = new HashMap<>();
    public static HashMap<Guild, ReactionEmote> downvoteReactions = new HashMap<>();

    public static void init(JDA jda) {
        loadReactions(jda);
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

    public static boolean setUpvoteReaction(GuildMessageReceivedEvent event) {
        return setReaction(event, upvoteReactions);
    }

    public static boolean setDownvoteReaction(GuildMessageReceivedEvent event) {
        return setReaction(event, downvoteReactions);
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

        MongoCollection<Document> karmaCollection = DatabaseHandler.karmaCollection;
        Document queryDocument = new Document("guild", guild.getId());

        //get list of users in guild and sort by descending karma
        var topKarmaCollection = karmaCollection.find(queryDocument).sort(new Document("karma", -1));

        //skip leaderboard creation if no users have any karma
        if (topKarmaCollection.first() == null) return "No users with karma found in this server";

        //add each user to return string
        StringBuilder sb = new StringBuilder("**Top karma for " + guild.getName() + "**\n");
        sb.append("```");
        for (Document doc : topKarmaCollection) {
            String userId = doc.get("user").toString();
            User user = null;
            for (Member member : guild.getMembers()) {
                if (member.getId().equals(userId)) {
                    user = member.getUser();
                    break;
                }
            }
            if (user == null) continue;
            sb.append(String.format("%5d\t%s#%s%n",
                    doc.getInteger("karma"),
                    user.getName(),
                    user.getDiscriminator())
            );
        }
        sb.append("```");

        return sb.toString();
    }

    private static void loadReactions(JDA jda) {
        MongoCollection<Document> reactionsCollection = DatabaseHandler.reactionsCollection;
        for (Document doc : reactionsCollection.find()) {
            String guildId = doc.get("guild").toString();

            Guild guild = jda.getGuildById(guildId);
            if (guild == null) continue;

            boolean upvoteRegistered = false;
            boolean downvoteRegistered = false;

            //check if upvote reaction is a custom emote from guild
            if (doc.get("upvote") != null) {
                String upvoteId = doc.get("upvote").toString();
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
            }

            //repeat for downvote
            if (doc.get("downvote") != null) {
                String downvoteId = doc.get("downvote").toString();
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
                    downvoteReactions.put(guild, downvoteReaction);
                }
            }
        }
    }

    private static void writeReactions(Guild guild) {
        MongoCollection<Document> reactionsCollection = DatabaseHandler.reactionsCollection;

        //find guild document
        Document queryDocument = new Document()
                .append("guild", guild.getId());

        if (reactionsCollection.find(queryDocument).first() == null) reactionsCollection.insertOne(queryDocument);

        Document doc = reactionsCollection.find(queryDocument).first();

        //append the emoji if it is emoji or emote ID if it is emote
        if (upvoteReactions.containsKey(guild)) {
            ReactionEmote upvoteReaction = upvoteReactions.get(guild);
            if (upvoteReaction.isEmoji()) {
                doc.append("upvote", upvoteReaction.getEmoji());
            } else {
                doc.append("upvote", upvoteReaction.getId());
            }
        }
        if (downvoteReactions.containsKey(guild)) {
            ReactionEmote downvoteReaction = downvoteReactions.get(guild);
            if (downvoteReaction.isEmoji()) {
                doc.append("downvote", downvoteReaction.getEmoji());
            } else {
                doc.append("downvote", downvoteReaction.getId());
            }
        }

        //update guild document with current reactions
        reactionsCollection.replaceOne(queryDocument, doc);
    }

    private static void createUser(Guild guild, User user) {
        MongoCollection<Document> karmaCollection = DatabaseHandler.karmaCollection;
        Document doc = new Document()
                .append("guild", guild.getId())
                .append("user", user.getId());
        karmaCollection.insertOne(doc);
    }

    private static boolean setReaction(GuildMessageReceivedEvent event, HashMap<Guild, ReactionEmote> map) {
        List<Emote> emotes = event.getMessage().getEmotes();
        Guild guild = event.getGuild();
        String[] args = event.getMessage().getContentRaw().split("\\s+"); //split by spaces

        ReactionEmote reaction = null;
        if (emotes.isEmpty()) {
            //not custom emote, try to parse as unicode
            try {
                reaction = ReactionEmote.fromUnicode(args[2], event.getJDA());
            } catch (Exception e) {
                return false;
            }
        } else {
            boolean registered = false;
            for (int i = 0; i < guild.getEmotes().size(); i++) {
                Emote emote = guild.getEmotes().get(i);
                if (emote.getId().equals(emotes.get(0).getId())) {
                    reaction = ReactionEmote.fromCustom(emote);
                    registered = true;
                    break;
                }
            }
            if (!registered) return false;
        }
        map.put(guild, reaction);
        writeReactions(guild);
        return true;
    }
}
