package net.vadamdev.jafarbot.channelcreator.system;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.Nullable;

/**
 * @author VadamDev
 * @since 31/03/2025
 */
public class CreatedChannel {
    protected final String channelId, ownerId;

    public CreatedChannel(String channelId, String ownerId) {
        this.channelId = channelId;
        this.ownerId = ownerId;
    }

    public void onChannelCreation(VoiceChannel channel, Member owner) {}
    public void onChannelDeletion(@Nullable VoiceChannel channel) {}

    public String getChannelId() {
        return channelId;
    }

    @Nullable
    public VoiceChannel retrieveChannel(Guild guild) {
        return guild.getVoiceChannelById(channelId);
    }

    public String getOwnerId() {
        return ownerId;
    }

    @Nullable
    public Member retrieveOwner(Guild guild) {
        return guild.getMemberById(ownerId);
    }
}
