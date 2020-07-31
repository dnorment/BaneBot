package norment.banebot.handler;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import norment.banebot.game.ReactionGame;

public class ReactionHandler {
    public static void handleReaction(GuildMessageReactionAddEvent event) {
        //Check if message embed contains an active game and pass to GameHandler
        String messageId = event.getMessageId();
        Message message = event.getChannel().retrieveMessageById(messageId).complete();
        MessageEmbed embed = message.getEmbeds().get(0);

        boolean hasFooter = embed.getFooter() != null;
        boolean hasFooterText = hasFooter && embed.getFooter().getText() != null;
        if (hasFooterText) {
            String footerText = embed.getFooter().getText();
            if (footerText.startsWith("Game: ")) {
                String gameId = footerText.split("\\(#")[1].replaceAll("\\)", "");
                ReactionGame game = GameHandler.getReactionGame(Integer.parseInt(gameId));
                if (game != null) game.handleReaction(event);
            }
        }

    }
}
