package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.entities.Member;
import net.vadamdev.jafarbot.channelcreator.system.AbstractChannelCreator;
import net.vadamdev.jafarbot.channelcreator.system.CreatedChannel;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author VadamDev
 * @since 17/03/2024
 */
public class SimpleChannelCreator<T extends CreatedChannel> extends AbstractChannelCreator<T> {
    private final Function<Member, String> channelName;
    private final Supplier<String> categoryId;

    public SimpleChannelCreator(Supplier<String> creatorId, Supplier<String> categoryId, Function<Member, String> channelName, Class<T> clazz) {
        super(creatorId, clazz);

        this.categoryId = categoryId;
        this.channelName = channelName;
    }

    @Nonnull
    @Override
    protected String getChannelName(Member owner) {
        return channelName.apply(owner);
    }

    @Nullable
    @Override
    protected String getCategoryId(Member owner) {
        return categoryId.get();
    }
}
