package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VadamDev
 * @since 24/01/2023
 */
public final class ChannelCreatorManager {
    private final Map<String, ChannelCreator> channelCreators;
    private final Map<String, List<String>> channels;

    public ChannelCreatorManager() {
        this.channelCreators = new HashMap<>();
        this.channels = new HashMap<>();
    }

    public void handleVoiceUpdateEvent(GuildVoiceUpdateEvent event) {
        if(event.getChannelJoined() != null && event.getChannelJoined().getType().equals(ChannelType.VOICE)) {
            final String channelId = event.getChannelJoined().asVoiceChannel().getId();

            if(channelCreators.containsKey(channelId))
                channelCreators.get(channelId).createChannel(event.getGuild(), event.getMember());
        }

        if(event.getChannelLeft() != null && event.getChannelLeft().getType().equals(ChannelType.VOICE)) {
            final VoiceChannel voiceChannel = event.getChannelLeft().asVoiceChannel();
            if(!voiceChannel.getMembers().isEmpty())
                return;

            for (Map.Entry<String, List<String>> entry : channels.entrySet()) for(String value : entry.getValue()) {
                if(value.equals(voiceChannel.getId())) {
                    channels.get(entry.getKey()).remove(value);
                    voiceChannel.delete().queue();
                    return;
                }
            }
        }
    }

    public void handleChannelDelete(@Nonnull VoiceChannel voiceChannel) {
        for (Map.Entry<String, List<String>> entry : channels.entrySet()) for(String value : entry.getValue()) {
            if(value.equals(voiceChannel.getId())) {
                channels.get(entry.getKey()).remove(value);
                return;
            }
        }
    }

    public void registerChannelCreator(ChannelCreator channelCreator) {
        channelCreators.put(channelCreator.getId(), channelCreator);
    }

    public int getChannelsAmount(String channelCreatorId) {
        if(!channels.containsKey(channelCreatorId))
            return 0;

        return channels.get(channelCreatorId).size();
    }

    void addChannel(ChannelCreator channelCreator, String channelId) {
        channels.putIfAbsent(channelCreator.getId(), new ArrayList<>());
        channels.get(channelCreator.getId()).add(channelId);
    }
}
