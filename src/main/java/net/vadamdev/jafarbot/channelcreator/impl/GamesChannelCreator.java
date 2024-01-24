package net.vadamdev.jafarbot.channelcreator.impl;

import net.dv8tion.jda.api.entities.Member;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.channelcreator.ChannelCreator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VadamDev
 * @since 27/08/2023
 */
public class GamesChannelCreator extends ChannelCreator<LockeableCreatedChannel> {
    public GamesChannelCreator() {
        super(() -> Main.jafarBot.mainConfig.GAMES_CREATOR, LockeableCreatedChannel.class);
    }

    @Nonnull
    @Override
    protected String getChannelName(Member owner) {
        return "\uD83C\uDFAE┃Vocal #%index%";
    }

    @Nullable
    @Override
    protected String getCategoryId(Member owner) {
        return Main.jafarBot.mainConfig.GAMES_CREATOR_CATEGORY;
    }
}
