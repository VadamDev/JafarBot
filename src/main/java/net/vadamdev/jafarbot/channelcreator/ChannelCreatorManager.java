package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author VadamDev
 * @since 27/08/2023
 */
public final class ChannelCreatorManager {
    private final Map<String, ChannelCreator<?>> channelCreators;
    private final Map<String, List<CreatedChannel>> createdChannels;

    public ChannelCreatorManager() {
        this.channelCreators = new HashMap<>();
        this.createdChannels = new HashMap<>();
    }

    /*
       Handle Events
     */

    public void handleVoiceUpdateEvent(@Nonnull GuildVoiceUpdateEvent event) {
        final AudioChannelUnion joined = event.getChannelJoined();
        final AudioChannelUnion left = event.getChannelLeft();

        if(joined != null && joined.getType().equals(ChannelType.VOICE)) {
            final String channelId = joined.asVoiceChannel().getId();

            if(channelCreators.containsKey(channelId))
                channelCreators.get(channelId).createChannel(event.getGuild(), event.getMember());
        }

        if(left != null && left.getType().equals(ChannelType.VOICE)) {
            final VoiceChannel voiceChannel = left.asVoiceChannel();

            if(voiceChannel.getMembers().isEmpty()) {
                findCreatedChannel(voiceChannel.getId())
                        .ifPresent(tuple -> {
                            createdChannels.get(tuple.getLeft()).remove(tuple.getRight());
                            voiceChannel.delete().queue();
                        });
            }
        }
    }

    public void handleButtonInteractionEvent(@Nonnull ButtonInteractionEvent event) {
        findCreatedChannel(event.getChannel().getId())
                .filter(pair -> pair.getRight() instanceof InteractionListener)
                .ifPresent(pair -> ((InteractionListener) pair.getRight()).handleButtonInteractionEvent(event));
    }

    public void handleSelectInteractionEvent(@Nonnull StringSelectInteractionEvent event) {
        findCreatedChannel(event.getChannel().getId())
                .filter(pair -> pair.getRight() instanceof InteractionListener)
                .ifPresent(pair -> ((InteractionListener) pair.getRight()).handleSelectInteractionEvent(event));
    }

    public void handleModalInteractionEvent(@Nonnull ModalInteractionEvent event) {
        findCreatedChannel(event.getChannel().getId())
                .filter(pair -> pair.getRight() instanceof InteractionListener)
                .ifPresent(pair -> ((InteractionListener) pair.getRight()).handleModalInteractionEvent(event));
    }

    public void handleChannelDelete(@Nonnull VoiceChannel voiceChannel) {
        findCreatedChannel(voiceChannel.getId())
                .ifPresent(pair -> createdChannels.get(pair.getLeft()).remove(pair.getRight()));
    }

    /*
       Utils
     */

    public void registerChannelCreator(ChannelCreator<?> channelCreator) {
        channelCreators.put(channelCreator.getCreatorId(), channelCreator);
    }

    public void deleteCreatedChannel(Guild guild, String channelId) {
        findCreatedChannel(channelId).ifPresent(pair -> {
            createdChannels.get(pair.getLeft()).remove(pair.getRight());

            final VoiceChannel voiceChannel = guild.getVoiceChannelById(channelId);
            if(voiceChannel != null)
                voiceChannel.delete().queue();
        });
    }

    private Optional<ImmutablePair<String, CreatedChannel>> findCreatedChannel(String channelId) {
        return createdChannels.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(channel -> channel.getChannelId().equals(channelId)))
                .map(entry -> new ImmutablePair<>(entry.getKey(), entry.getValue().stream().filter(channel -> channel.getChannelId().equals(channelId)).findFirst().get()))
                .findFirst();
    }

    int getActiveChannelAmount(String creatorId) {
        return createdChannels.containsKey(creatorId) ? createdChannels.get(creatorId).size() : 0;
    }

    void registerCreatedChannel(String creatorId, CreatedChannel activeChannel) {
        createdChannels.computeIfAbsent(creatorId, k -> new ArrayList<>()).add(activeChannel);
    }
}
