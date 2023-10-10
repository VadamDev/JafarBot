package net.vadamdev.jafarbot.channelcreator.impl;

import net.dv8tion.jda.api.entities.Member;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.channelcreator.ChannelCreator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VadamDev
 * @since 10/10/2023
 */
public class JafarMcChannelCreator extends ChannelCreator<LockeableCreatedChannel> {
    public JafarMcChannelCreator() {
        super(() -> Main.jafarBot.mainConfig.JAFARMC_CREATOR, LockeableCreatedChannel.class);
    }

    @Nonnull
    @Override
    protected String getChannelName(Member owner) {
        return "\uD83E\uDDF1┃Vocal #%index%";
    }

    @Nullable
    @Override
    protected String getCategoryId(Member owner) {
        return Main.jafarBot.mainConfig.JAFARMC_CREATOR_CATEGORY;
    }
}
