package norment.banebot.game.connectfour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import norment.banebot.game.ReactionGame;

import java.util.List;

public class ConnectFourGame extends ReactionGame {
    private final ConnectFourBoard board = new ConnectFourBoard();
    private final User[] users = new User[2];
    private int turn = 0;

    public ConnectFourGame(User firstUser, User secondUser) {
        users[0] = firstUser;
        users[1] = secondUser;
    }

    public void handleReaction(GuildMessageReactionAddEvent event) {
        //Check if the reaction is from the active turn's user
        User user = event.getUser();
        if (user.equals(users[turn % 2])) {
            //get color and column choice of current player, update turn #
            String color = user.equals(users[0]) ? "red" : "blue";
            int column = getColumnFromReaction(event);
            event.getReaction().removeReaction(event.getUser()).complete();
            //ignore non-existing number reactions
            if (column < 0 || column > 6) return;

            //check valid move, then do placement
            if (board.canPlace(column)) {
                board.place(color, column);
                turn++;
            }
            //TODO check wincond
        } else {
            //remove non-active player reactions
            event.getReaction().removeReaction(event.getUser()).complete();
        }
        updateEmbed(event);
    }

    private int getColumnFromReaction(GuildMessageReactionAddEvent event) {
        Message message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
        List<MessageReaction> reactions = message.getReactions();
        MessageReaction reaction = event.getReaction();

        int column = -1;
        for (int i = 0; i < reactions.size(); i++) {
            if (reaction.getReactionEmote().equals(reactions.get(i).getReactionEmote())) {
                column = i;
            }
        }
        return column;
    }

    private void updateEmbed(GuildMessageReactionAddEvent event) {
        String messageId = event.getMessageId();
        Message message = event.getChannel().retrieveMessageById(messageId).complete();
        MessageEmbed oldEmbed = message.getEmbeds().get(0);

        EmbedBuilder embed = new EmbedBuilder();
        //copy title and footer to new embed
        embed.setTitle(oldEmbed.getTitle())
                .setColor(oldEmbed.getColor());
        if (oldEmbed.getFooter() == null) return;
        embed.setFooter(oldEmbed.getFooter().getText());

        //update board state
        embed.setDescription(board.toString());

        //edit message to reflect update
        message.editMessage(embed.build()).queue();
    }

    public ConnectFourBoard getBoard() {
        return board;
    }

    @Override
    public String getGame() {
        return "Connect 4";
    }

    @Override
    public int getGameId() {
        return this.hashCode();
    }
}
