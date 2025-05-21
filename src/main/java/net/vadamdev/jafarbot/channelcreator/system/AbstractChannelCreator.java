package net.vadamdev.jafarbot.channelcreator.system;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

/**
 * @author VadamDev
 * @since 31/03/2025
 */
public abstract class AbstractChannelCreator {
    protected final String creatorId;

    public AbstractChannelCreator(String creatorId) {
        this.creatorId = creatorId;
    }

    public abstract CompletableFuture<CreatedChannel> createChannel(ChannelCreatorManager manager, Guild guild, Member owner);

    public String getCreatorId() {
        return creatorId;
    }
}
