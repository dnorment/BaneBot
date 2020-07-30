package norment.banebot.main;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import norment.banebot.command.Command;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class CommandHandler {
    private static final HashMap<String, Command> commands = new HashMap<>();

    public static void init() {
        loadCommands();
    }

    private static void loadCommands()  {
        //Retrieve all classes in command package
        Reflections reflections = new Reflections("norment.banebot.command");
        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);

        //Add valid commands to command map
        for (Class<? extends Command> c : classes) {
            try {
                //Skip abstract commands
                if (Modifier.isAbstract(c.getModifiers())) continue;

                //Add command to command map
                Command cmd = c.getConstructor().newInstance();
                if (!commands.containsKey(cmd.getCommand())) commands.put(cmd.getCommand(), cmd);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleCommand(GuildMessageReceivedEvent event) {
        //Split command into args
        Message message = event.getMessage();
        String[] words = message.getContentRaw().split("\\s+"); //split by spaces
        String command = words[0].replace(BaneBot.prefix, "");

        //Check if command exists
        if (commands.containsKey(command)) {
            //Execute command
            commands.get(command).run(event);
        }
    }
}
