package norment.banebot.handler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import norment.banebot.game.ReactionGame;

public class ReactionHandler {
    public static void handleAddReaction(GuildMessageReactionAddEvent event) {
        String messageId = event.getMessageId();
        Message message = event.getChannel().retrieveMessageById(messageId).complete();

        //Check if message has an embed from Bane
        if (hasBaneEmbed(message)) {
            MessageEmbed embed = message.getEmbeds().get(0);

            //Check footer for embed type
            boolean hasFooter = embed.getFooter() != null;
            boolean hasFooterText = hasFooter && embed.getFooter().getText() != null;
            if (hasFooterText) {
                String footerText = embed.getFooter().getText();
                //Pass to game handler if embed contains an active game
                if (footerText.startsWith("Game: ")) {
                    String gameId = footerText.split("\\(#")[1].replaceAll("\\)", "");
                    ReactionGame game = GameHandler.getReactionGame(Integer.parseInt(gameId));
                    if (game != null) game.handleReaction(event);
                }
            }
        } else {
            KarmaHandler.handleAddReaction(event);
        }
    }

    public static void handleRemoveReaction(GuildMessageReactionRemoveEvent event) {
        KarmaHandler.handleRemoveReaction(event);
    }

    private static boolean hasBaneEmbed(Message message) {
        boolean hasEmbed = !message.getEmbeds().isEmpty();
        boolean isFromSelf = message.getAuthor().equals(message.getJDA().getSelfUser());
        return hasEmbed && isFromSelf;
    }
}
