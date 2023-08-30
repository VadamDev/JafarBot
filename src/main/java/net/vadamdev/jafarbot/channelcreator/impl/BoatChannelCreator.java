package net.vadamdev.jafarbot.channelcreator.impl;

import net.dv8tion.jda.api.entities.Member;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.channelcreator.ChannelCreator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VadamDev
 * @since 29/08/2023
 */
public class BoatChannelCreator extends ChannelCreator<LockeableCreatedChannel> {
    public BoatChannelCreator() {
        super(() -> Main.jafarBot.mainConfig.BOAT_CREATOR, LockeableCreatedChannel.class);
    }

    @Nonnull
    @Override
    protected String getChannelName(Member owner) {
        return "⛵┃Bateau #%index%";
    }

    @Nullable
    @Override
    protected String getCategoryId(Member owner) {
        return Main.jafarBot.mainConfig.BOAT_CREATOR_CATEGORY;
    }
}
