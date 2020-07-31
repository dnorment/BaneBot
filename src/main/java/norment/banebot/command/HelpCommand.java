package norment.banebot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import norment.banebot.handler.CommandHandler;

public class HelpCommand extends Command {
    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "List commands or show usage of a command";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "help::List possible commands",
                "help `<command>`::Show description and usage of `command`"
        };
    }

    @Override
    public void run(GuildMessageReceivedEvent event) {
        TextChannel channel = event.getChannel();
        String[] args = event.getMessage().getContentRaw().split("\\s+"); //split by spaces
        EmbedBuilder embed = new EmbedBuilder();

        if (args.length > 2) {
            //help command takes one argument
            Command.showUsage(event, this, true);
            return;
        } else if (args.length == 1) { //show all commands
            //initialize embed properties
            embed.setTitle("Help")
                    .setColor(Command.GREEN);

            //add each command as a field in the embed
            for (Command c : CommandHandler.getCommands()) {
                embed.addField(c.getCommand(), c.getDescription(), false);
            }
        } else { //show command usage
            boolean commandExists = false;
            String desiredCommand = args[1];

            //check if desired command exists
            Command[] commands = CommandHandler.getCommands();
            Command command = commands[0];
            for (Command c : CommandHandler.getCommands()) {
                if (c.getCommand().equals(desiredCommand)) {
                    commandExists = true;
                    command = c;
                }
            }

            //show command if it exists, otherwise show error
            if(commandExists) {
                Command.showUsage(event, command, false);
                return;
            } else {
                embed.setTitle("Error")
                        .setColor(Command.RED)
                        .setDescription(String.format("%s is not a valid command", desiredCommand));
            }
        }

        channel.sendMessage(embed.build()).queue();
    }
}
