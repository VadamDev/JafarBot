package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.vadamdev.jafarbot.Main;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * @author VadamDev
 * @since 28/08/2023
 */
public class CreatedChannel {
    protected MessageEmbed NOT_OWNER_MESSAGE = new EmbedBuilder()
            .setTitle("Salon Personnalisé")
            .setDescription("Vous pouvez interagir avec ces boutons seulement si vous êtes le propriétaire de ce salon !")
            .setColor(Color.RED)
            .setFooter("JafarBot",Main.jafarBot.getAvatarURL()).build();

    protected final String channelId;
    protected final String ownerId;

    public CreatedChannel(String channelId, String ownerId) {
        this.channelId = channelId;
        this.ownerId = ownerId;
    }

    protected void onChannelCreation(VoiceChannel voiceChannel, Member owner) {}

    public boolean isOwner(String memberId, @Nullable IReplyCallback replyCallback) {
        final boolean isOwner = memberId.equals(ownerId);

        if(!isOwner && replyCallback != null)
            replyCallback.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();

        return isOwner;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getOwnerId() {
        return ownerId;
    }
}
