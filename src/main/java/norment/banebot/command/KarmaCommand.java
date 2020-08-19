package norment.banebot.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import norment.banebot.handler.KarmaHandler;

import java.util.List;

public class KarmaCommand extends Command {
    @Override
    public String getCommand() {
        return "karma";
    }

    @Override
    public String getDescription() {
        return "Displays karma or sets up the reactions for the karma system";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "karma::Get your current karma",
                "karma `<@user>`::Get `user`'s current karma",
                "karma leaderboard::Show a karma leaderboard for this server",
                "karma ignore `<@user>`::Ignores or unignores all karma reactions from `user`",
                "karma setupvote `<reaction>`::Sets `reaction` emoji/emote as the upvote reaction",
                "karma setdownvote `<reaction>`::Sets `reaction` emoji/emote as the downvote reaction"
        };
    }

    @Override
    public void run(GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        String[] args = event.getMessage().getContentRaw().split("\\s+"); //split by spaces

        if (args.length == 1) {
            //get self karma
            int userKarma = KarmaHandler.getKarma(event.getGuild(), event.getAuthor());
            channel.sendMessage("" + userKarma).queue();
        } else if (args.length == 2) {
            if (args[1].equals("leaderboard")) {
                String leaderboard = KarmaHandler.getKarmaLeaderboard(event.getGuild());
                channel.sendMessage(leaderboard).queue();
            } else {
                //get mentioned user
                List<User> mentionedUsers = event.getMessage().getMentionedUsers();
                if (mentionedUsers.size() != 1) {
                    showUsage(event, this, true);
                    return;
                }
                int userKarma = KarmaHandler.getKarma(event.getGuild(), mentionedUsers.get(0));
                channel.sendMessage("" + userKarma).queue();
            }
        } else if (args.length == 3) {
            //parse command
            switch (args[1]) {
                case "ignore":
                    //check that only 1 user is mentioned
                    List<User> mentionedUsers = event.getMessage().getMentionedUsers();
                    if (mentionedUsers.size() != 1) {
                        showUsage(event, this, true);
                        return;
                    }
                    //negate ignore boolean of user
                    KarmaHandler.setIgnored(event);
                    channel.sendMessage("Changing ignore of user for all karma reactions").queue();
                    break;
                case "setupvote":
                    if (KarmaHandler.setUpvoteReaction(event)) channel.sendMessage("Set as upvote").queue();
                    break;
                case "setdownvote":
                    if (KarmaHandler.setDownvoteReaction(event)) channel.sendMessage("Set as downvote").queue();
                    break;
                default:
                    showUsage(event, this, true);
                    break;
            }
        } else {
            showUsage(event, this, true);
        }
    }
}
