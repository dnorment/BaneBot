package norment.banebot.command;

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
        return "Returns your karma or sets up the reactions for the karma system";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "karma::Get your current karma",
                "karma `<@user>``::Get `user`'s current karma",
                "karma setupvote `<reaction>`::Sets `reaction` as the upvote reaction",
                "karma setdownvote `<reaction>`::Sets `reaction` as the downvote reaction"
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
            //get mentioned user
            List<User> mentionedUsers = event.getMessage().getMentionedUsers();
            if (mentionedUsers.size() != 1) {
                showUsage(event, this, true);
                return;
            }
            int userKarma = KarmaHandler.getKarma(event.getGuild(), mentionedUsers.get(0));
            channel.sendMessage("" + userKarma).queue();
        } else if (args.length == 3) {
            if (args[1].equals("setupvote")) {
                if (KarmaHandler.setUpvoteReaction(event)) channel.sendMessage("Set as upvote").queue();
            } else if (args[1].equals("setdownvote")) {
                if (KarmaHandler.setDownvoteReaction(event)) channel.sendMessage("Set as downvote").queue();
            } else {
                showUsage(event, this, true);
            }
        } else {
            showUsage(event, this, true);
        }
    }
}
