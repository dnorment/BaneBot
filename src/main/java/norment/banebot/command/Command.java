package norment.banebot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.Color;

public abstract class Command {

    //The colors for Discord's online statuses, use for embed statuses
    public static final Color GREEN = new Color(67, 181, 129);
    public static final Color YELLOW = new Color(250, 166, 26);
    public static final Color RED = new Color(240, 71, 71);
    public static final Color PURPLE = new Color(100, 61, 167);

    public abstract String getCommand();

    public abstract String getDescription();

    public abstract String[] getUsage();

    public abstract void run(GuildMessageReceivedEvent event);

    public static void showUsage(GuildMessageReceivedEvent event, Command cmd, boolean error) {
        EmbedBuilder embed = new EmbedBuilder();
        String command = cmd.getCommand();
        
        //set title and description for command info
        embed.setTitle(String.format("%s%s", command.substring(0, 1).toUpperCase(), command.substring(1))) //capitalize first letter
                .setDescription(cmd.getDescription())
                .setColor(!error ? GREEN : RED);
        
        //add description of usage(s)
        for (String s : cmd.getUsage()) {
            String use = s.split("::")[0];
            String desc = s.split("::")[1];
            embed.addField(use, desc, false);
        }

        //send embed
        event.getChannel().sendMessage(embed.build()).queue();
    }
}
