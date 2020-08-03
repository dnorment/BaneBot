package norment.banebot.game.connectfour;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import norment.banebot.event.WinEvent;
import norment.banebot.game.ReactionGame;
import norment.banebot.handler.GameHandler;
import norment.banebot.handler.ScoreHandler;

import java.awt.*;
import java.util.List;

public class ConnectFourGame extends ReactionGame {
    public static final Color RED_CIRCLE = new Color(221, 46, 68);
    public static final Color BLUE_CIRCLE = new Color(85, 172, 238);
    private final ConnectFourBoard board = new ConnectFourBoard();
    private final User[] users = new User[2];
    private int turn = 0;

    public ConnectFourGame(User firstUser, User secondUser) {
        users[0] = firstUser;
        users[1] = secondUser;
    }

    public void handleReaction(GuildMessageReactionAddEvent event) {
        User user = event.getUser();
        Guild guild = event.getGuild();
        MessageReaction reaction = event.getReaction();
        TextChannel channel = event.getChannel();
        //Check if the reaction is from the active turn's user
        if (user.equals(users[turn % 2])) {
            //get color and column choice of current player, update turn #
            String color = user.equals(users[0]) ? "red" : "blue";
            int column = getColumnFromReaction(event);
            reaction.removeReaction(user).complete();
            //ignore non-existing number reactions
            if (column < 0 || column > 6) return;

            //check valid move, then do placement
            if (board.canPlace(column)) {
                board.place(color, column);
                //check player has won, if so end game and remove from active games
                if (board.hasWon(color)) {
                    //clear reactions and remove
                    updateEmbed(event);
                    channel.retrieveMessageById(event.getMessageId()).complete().clearReactions().queue();
                    GameHandler.removeGame(this);

                    //handle game win
                    User loser = users[0].equals(user) ? users[1] : users[0];
                    channel.sendMessage("Winner: " + user.getAsMention() + ", Loser: " + loser.getAsMention()).queue();
                    WinEvent winEvent = new WinEvent("connect4", guild, user, loser);
                    ScoreHandler.handleScore(winEvent);
                } else {
                    //move to next turn and update
                    turn++;
                    updateEmbed(event);
                }
            }

        } else {
            //remove non-active player reactions
            event.getReaction().removeReaction(event.getUser()).complete();
        }
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
        embed.setTitle(oldEmbed.getTitle());
        if (oldEmbed.getFooter() == null) return;
        embed.setFooter(oldEmbed.getFooter().getText());

        //update color to current player
        embed.setColor((turn % 2) == 0 ? RED_CIRCLE : BLUE_CIRCLE);

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
