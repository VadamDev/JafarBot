package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.vadamdev.jafarbot.Main;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

/**
 * @author VadamDev
 * @since 27/08/2023
 */
public abstract class ChannelCreator<T extends CreatedChannel> {
    private final Supplier<String> creatorId;
    private Constructor<T> constructor;

    public ChannelCreator(Supplier<String> creatorId, Class<T> clazz) {
        this.creatorId = creatorId;

        try {
            this.constructor = clazz.getConstructor(String.class, String.class);
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    void createChannel(Guild guild, Member owner) {
        final String creatorId = this.creatorId.get();

        final String categoryId = getCategoryId(owner);
        final String channelName = getChannelName(owner)
                .replace("%index%", (Main.jafarBot.getChannelCreatorManager().getActiveChannelAmount(creatorId) + 1) + "");

        final ChannelAction<VoiceChannel> action;
        if(categoryId != null && guild.getCategoryById(categoryId) != null)
            action = guild.createVoiceChannel(channelName, guild.getCategoryById(categoryId));
        else
            action = guild.createVoiceChannel(channelName);

        computeCreateAction(action).queue(channel -> {
            guild.moveVoiceMember(owner, channel).queue();

            try {
                final CreatedChannel createdChannel = constructor.newInstance(channel.getId(), owner.getId());
                createdChannel.onChannelCreation(channel, owner);
                Main.jafarBot.getChannelCreatorManager().registerCreatedChannel(creatorId, createdChannel);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    @Nonnull
    protected abstract String getChannelName(Member owner);

    @Nullable
    protected abstract String getCategoryId(Member owner);

    @Nonnull
    protected ChannelAction<VoiceChannel> computeCreateAction(ChannelAction<VoiceChannel> voiceAction) {
        return voiceAction;
    }

    String getCreatorId() {
        return creatorId.get();
    }
}
