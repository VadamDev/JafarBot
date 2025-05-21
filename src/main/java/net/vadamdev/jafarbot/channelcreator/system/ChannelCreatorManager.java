package net.vadamdev.jafarbot.channelcreator.system;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.vadamdev.dbk.framework.tuple.ImmutablePair;

import java.util.*;

/**
 * @author VadamDev
 * @since 31/03/2025
 */
public class ChannelCreatorManager extends ListenerAdapter {
    private final Map<String, AbstractChannelCreator> channelCreators;
    private final Map<String, List<CreatedChannel>> createdChannels;

    private final ChannelCreatorManager instance;

    public ChannelCreatorManager(JDA jda) {
        this.channelCreators = new HashMap<>();
        this.createdChannels = new HashMap<>();

        this.instance = this;

        jda.addEventListener(new Listener());
    }

    /*
       Channel Creator Registry
     */

    public void registerChannelCreator(AbstractChannelCreator channelCreator) {
        channelCreators.put(channelCreator.getCreatorId(), channelCreator);
    }

    public void registerChannelCreators(AbstractChannelCreator... channelCreators) {
        for(AbstractChannelCreator channelCreator : channelCreators)
            registerChannelCreator(channelCreator);
    }

    public void removeChannelCreator(String creatorId) {
        channelCreators.remove(creatorId);
    }

    /*
       Created Channels Manipulation
     */

    public void deleteCreatedChannel(Guild guild, String channelId) {
        findCreatedChannel(channelId).ifPresent(pair -> {
            final CreatedChannel createdChannel = pair.getRight();

            final VoiceChannel voiceChannel = guild.getVoiceChannelById(channelId);
            createdChannel.onChannelDeletion(voiceChannel);

            if(voiceChannel != null)
                voiceChannel.delete().queue();

            createdChannels.computeIfPresent(pair.getLeft(), (k, channels) -> {
                channels.remove(createdChannel);
                return channels.isEmpty() ? null : channels;
            });
        });
    }

    public Optional<ImmutablePair<String, CreatedChannel>> findCreatedChannel(String channelId) {
        return createdChannels.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .filter(channel -> channel.getChannelId().equals(channelId))
                        .map(channel -> ImmutablePair.of(entry.getKey(), channel))
                ).findFirst();
    }

    public int getActiveChannelAmount(String creatorId) {
        return createdChannels.containsKey(creatorId) ? createdChannels.get(creatorId).size() : 0;
    }

    /*
        Listener
     */

    private final class Listener extends ListenerAdapter {
        @Override
        public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
            final Guild guild = event.getGuild();

            final AudioChannelUnion joined = event.getChannelJoined();
            if(joined != null && joined.getType().isAudio()) {
                final String channelId = joined.getId();

                if(channelCreators.containsKey(channelId)) {
                    channelCreators.get(channelId)
                            .createChannel(instance, guild, event.getMember())
                            .whenComplete((channel, throwable) -> {
                                if(throwable != null) {
                                    throwable.printStackTrace();
                                    return;
                                }

                                if(channel == null)
                                    return;

                                createdChannels.computeIfAbsent(channelId, k -> new ArrayList<>()).add(channel);
                            });
                }
            }

            final AudioChannelUnion left = event.getChannelLeft();
            if(left != null && left.getType().isAudio()) {
                final VoiceChannel voiceChannel = left.asVoiceChannel();

                if(voiceChannel.getMembers().isEmpty())
                    deleteCreatedChannel(guild, voiceChannel.getId());
            }
        }

        @Override
        public void onChannelDelete(ChannelDeleteEvent event) {
            if(!event.getChannelType().isAudio())
                return;

            deleteCreatedChannel(event.getGuild(), event.getChannel().getId());
        }
    }
}
