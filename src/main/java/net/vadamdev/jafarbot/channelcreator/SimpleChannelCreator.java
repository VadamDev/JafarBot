package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.vadamdev.jafarbot.channelcreator.system.AbstractChannelCreator;
import net.vadamdev.jafarbot.channelcreator.system.ChannelCreatorManager;
import net.vadamdev.jafarbot.channelcreator.system.CreatedChannel;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author VadamDev
 * @since 31/03/2025
 */
public class SimpleChannelCreator extends AbstractChannelCreator {
    public static final String CHANNEL_INDEX_PLACEHOLDER = "%index%";

    protected final BiFunction<VoiceChannel, Member, CreatedChannel> channelCreatorFunction;
    protected final Function<Member, String> namingFunction;
    protected final Function<Member, Category> categoryFunction;

    public SimpleChannelCreator(String creatorId, BiFunction<VoiceChannel, Member, CreatedChannel> channelCreatorFunction,
                                Function<Member, String> namingFunction, Function<Member, Category> categoryFunction) {
        super(creatorId);

        this.channelCreatorFunction = channelCreatorFunction;
        this.namingFunction = namingFunction;
        this.categoryFunction = categoryFunction;
    }

    public SimpleChannelCreator(String creatorId, Function<Member, String> namingFunction, Function<Member, Category> categoryFunction) {
        this(creatorId, (channel, owner) -> new CreatedChannel(channel.getId(), owner.getId()), namingFunction, categoryFunction);
    }

    @Override
    public CompletableFuture<CreatedChannel> createChannel(ChannelCreatorManager manager, Guild guild, Member owner) {
        final String channelName = namingFunction.apply(owner)
                .replace(CHANNEL_INDEX_PLACEHOLDER, String.valueOf(manager.getActiveChannelAmount(creatorId) + 1));

        return guild.createVoiceChannel(channelName, categoryFunction.apply(owner)).submit()
                .thenApply(channel -> {
                    final CreatedChannel createdChannel = channelCreatorFunction.apply(channel, owner);
                    createdChannel.onChannelCreation(channel, owner);

                    guild.moveVoiceMember(owner, channel).queue();

                    return createdChannel;
                });
    }
}
