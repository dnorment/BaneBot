package norment.banebot.game;

import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

public abstract class ReactionGame extends Game{
    public abstract void handleReaction(GuildMessageReactionAddEvent event);
}
