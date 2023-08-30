package net.vadamdev.jafarbot.channelcreator.impl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.channelcreator.CreatedChannel;
import net.vadamdev.jafarbot.channelcreator.InteractionListener;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * @author VadamDev
 * @since 29/08/2023
 */
public class LockeableCreatedChannel extends CreatedChannel implements InteractionListener {
    private String configMessageId;
    private boolean locked;

    public LockeableCreatedChannel(String channelId, String ownerId) {
        super(channelId, ownerId);
    }

    @Override
    public void handleButtonInteractionEvent(@Nonnull ButtonInteractionEvent event) {
        final Member member = event.getMember();

        switch(event.getComponentId()) {
            case "jafarBot-LockeableChannel-Lock":
                if(!isOwner(member.getId(), event))
                    return;

                setLocked(event.getGuild(), !locked);
                event.deferEdit().queue();

                break;
            case "jafarBot-LockeableChannel-Delete":
                if(!isOwner(member.getId(), event))
                    return;

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Salon de " + member.getEffectiveName())
                        .setDescription("Êtes-vous sur(e) de vouloir supprimer ce salon ?\n*Cela déconnectera toutes les personnes présentent à l'intérieur !*")
                        .setColor(Color.ORANGE)
                        .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).setActionRow(
                                Button.danger("jafarBot-LockeableChannel-ConfirmDelete", "Confirmer")
                        ).setEphemeral(true).queue();

                break;
            case "jafarBot-LockeableChannel-ConfirmDelete":
                if(!isOwner(member.getId(), event))
                    return;

                event.deferEdit().queue();
                Main.jafarBot.getChannelCreatorManager().deleteCreatedChannel(event.getGuild(), channelId);

                break;
            default:
                break;
        }
    }

    @Override
    protected void onChannelCreation(VoiceChannel voiceChannel, Member owner) {
        updateOrCreateConfigMessage(voiceChannel, owner);
    }

    private void updateOrCreateConfigMessage(VoiceChannel voiceChannel, Member owner) {
        if(configMessageId != null) {
            voiceChannel.retrieveMessageById(configMessageId).queue(message ->
                    message.editMessageEmbeds(createConfigMessage(owner))
                            .setActionRow(getComponents())
                            .queue()
            );
        }else {
            voiceChannel.sendMessageEmbeds(createConfigMessage(owner))
                    .setActionRow(getComponents())
                    .queue(message -> configMessageId = message.getId());
        }
    }

    protected void setLocked(Guild guild, boolean locked) {
        final VoiceChannel voiceChannel = guild.getVoiceChannelById(channelId);
        final Member owner = guild.getMemberById(ownerId);

        if(voiceChannel == null || owner == null)
            return;

        if(locked && !this.locked) {
            final int memberSize = voiceChannel.getMembers().size();
            voiceChannel.getManager().setUserLimit(memberSize == 1 ? 2 : memberSize).complete();
        }else if(!locked && this.locked)
            voiceChannel.getManager().setUserLimit(0).complete();

        this.locked = locked;

        updateOrCreateConfigMessage(voiceChannel, owner);
    }

    @Nonnull
    protected MessageEmbed createConfigMessage(Member owner) {
        return new EmbedBuilder()
                .setTitle("Salon de " + owner.getEffectiveName())
                .setDescription(
                        ">>> Status: " + (locked ? "\uD83D\uDD12" : "\uD83D\uDD13") + "\n")
                .setColor(Color.ORANGE)
                .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build();
    }

    protected ItemComponent[] getComponents() {
        return new ItemComponent[] {
                Button.primary("jafarBot-LockeableChannel-Lock", Emoji.fromUnicode(locked ? "\uD83D\uDD13" : "\uD83D\uDD12")),
                Button.danger("jafarBot-LockeableChannel-Delete", Emoji.fromUnicode("\uD83D\uDDD1️"))
        };
    }

    public boolean isLocked() {
        return locked;
    }
}
