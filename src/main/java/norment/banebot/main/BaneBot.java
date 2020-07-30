package norment.banebot.main;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import norment.banebot.config.ReadConfig;

import javax.security.auth.login.LoginException;

public class BaneBot {

    public static String prefix;

    public static void main(String[] args) throws LoginException {
        //Read properties file
        ReadConfig cfg = new ReadConfig();

        //Get config from properties file
        String token = cfg.properties.getProperty("botToken");
        prefix = cfg.properties.getProperty("prefix");

        //Set bot token and allowed events
        JDABuilder builder = JDABuilder.create(
                token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES
        ).disableCache(
                CacheFlag.ACTIVITY,
                CacheFlag.EMOTE,
                CacheFlag.CLIENT_STATUS
        );

        //Set visible bot status
        builder.setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching("the fire rise"));

        //Listen for events and handle
        builder.addEventListeners(new EventRouter());

        //Launch
        builder.build();
    }

}
