package norment.banebot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

public class Bane {

    public static String prefix;

    public static void main(String[] args) throws LoginException {
        //Read properties file
        ReadConfig cfg = new ReadConfig();

        //Get config from properties file
        String token = cfg.properties.getProperty("botToken");
        prefix = cfg.properties.getProperty("prefix");

        //Set bot online
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("the fire rise"));

        //Listen for events and handle
        builder.addEventListeners(new Commands());

        //Launch
        builder.build();
    }

}
