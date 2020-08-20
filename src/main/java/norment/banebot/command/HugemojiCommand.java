package norment.banebot.command;

import net.dv8tion.jda.api.entities.Activity.Emoji;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class HugemojiCommand extends Command {
    @Override
    public String getCommand() {
        return "hugemoji";
    }

    @Override
    public String getDescription() {
        return "Shows the large-size image version of emotes and emojis";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "hugemoji `<@reaction>`::Show the full-size image of the `reaction` emoji/emote"
        };
    }

    @Override
    public void run(GuildMessageReceivedEvent event) {
        List<Emote> emotes = event.getMessage().getEmotes();
        String[] args = event.getMessage().getContentRaw().split("\\s+"); //split by spaces

        //parse as ReactionEmote to get image
        String url = null;
        if (emotes.isEmpty()) {
            //not custom emote, try to parse as unicode
            ReactionEmote reaction = ReactionEmote.fromUnicode(args[1], event.getJDA());
            String e = reaction.getEmoji();
            Emoji emoji = new Emoji(e);
            
        } else {
            Emote emote = emotes.get(0);
            ReactionEmote reaction = ReactionEmote.fromCustom(emote);
            url = reaction.getEmote().getImageUrl();
        }

        if (url != null) event.getChannel().sendMessage(url).queue();
    }
}
