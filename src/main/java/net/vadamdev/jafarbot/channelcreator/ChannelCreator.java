package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.vadamdev.jafarbot.Main;

/**
 * @author VadamDev
 * @since 24/01/2023
 */
public class ChannelCreator {
    private final String id, categoryId, channelName;

    public ChannelCreator(String id, String categoryId, String channelName) {
        this.id = id;
        this.categoryId = categoryId;
        this.channelName = channelName;
    }

    void createChannel(Guild guild, Member member) {
        computeChannel(guild.getCategoryById(categoryId)
                .createVoiceChannel(channelName.replace("%index%", (Main.jafarBot.getChannelCreatorManager().getChannelsAmount(id) + 1) + "")))
                .queue(channel -> {
                    Main.jafarBot.getChannelCreatorManager().addChannel(this, channel.getId());
                    guild.moveVoiceMember(member, channel).queue();
                });
    }

    protected ChannelAction<VoiceChannel> computeChannel(ChannelAction<VoiceChannel> channel) {
        return channel;
    }

    String getId() {
        return id;
    }
}
