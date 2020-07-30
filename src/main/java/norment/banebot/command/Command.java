package norment.banebot.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class Command {
    public abstract String getCommand();
    public abstract String getDescription();
    public abstract String[] getUsage();
    public abstract void run(GuildMessageReceivedEvent event);
}
