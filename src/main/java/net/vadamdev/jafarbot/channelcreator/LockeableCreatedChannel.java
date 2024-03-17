package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.channelcreator.system.CreatedChannel;
import net.vadamdev.jafarbot.utils.JafarEmbed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VadamDev
 * @since 29/08/2023
 */
public class LockeableCreatedChannel extends CreatedChannel {
    protected static MessageEmbed NOT_OWNER_MESSAGE = new JafarEmbed()
            .setTitle("Salon Personnalisé")
            .setDescription("Vous pouvez interagir avec ces boutons seulement si vous êtes le propriétaire de ce salon !")
            .setColor(JafarEmbed.ERROR_COLOR).build();

    private String configMessageId;
    private boolean locked;

    public LockeableCreatedChannel(String channelId, String ownerId) {
        super(channelId, ownerId);
    }

    /*
       Events
     */

    @Override
    protected void onChannelCreation(VoiceChannel voiceChannel, Member owner) {
        updateOrCreateConfigMessage(voiceChannel, owner);
    }

    @Override
    protected void handleButtonInteractionEvent(@Nonnull ButtonInteractionEvent event) {
        final Member member = event.getMember();

        switch(event.getComponentId()) {
            case "JafarBot-LockeableChannel-Lock":
                if(!isOwner(member.getId(), event))
                    return;

                event.deferEdit().queue();
                setLocked(event.getGuild(), !locked);

                break;
            case "JafarBot-LockeableChannel-Delete":
                if(!isOwner(member.getId(), event))
                    return;

                event.replyEmbeds(new JafarEmbed()
                        .setTitle("Salon de " + member.getEffectiveName())
                        .setDescription("Êtes-vous sur(e) de vouloir supprimer ce salon ?\n*Cela déconnectera toutes les personnes présentent à l'intérieur !*")
                        .setColor(JafarEmbed.NEUTRAL_COLOR).build()).setActionRow(
                                Button.danger("JafarBot-LockeableChannel-ConfirmDelete", "Confirmer")
                        ).setEphemeral(true).queue();

                break;
            case "JafarBot-LockeableChannel-ConfirmDelete":
                if(!isOwner(member.getId(), event))
                    return;

                event.deferEdit().queue();
                Main.jafarBot.getChannelCreatorManager().deleteCreatedChannel(event.getGuild(), channelId);

                break;
            default:
                break;
        }
    }

    /*
       Utility
     */

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

    public boolean isOwner(String memberId, @Nullable IReplyCallback replyCallback) {
        final boolean isOwner = memberId.equals(ownerId);

        if(!isOwner && replyCallback != null)
            replyCallback.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();

        return isOwner;
    }

    /*
       Config Message
     */

    protected void updateOrCreateConfigMessage(VoiceChannel voiceChannel, Member owner) {
        if(configMessageId != null) {
            voiceChannel.retrieveMessageById(configMessageId).queue(message ->
                    message.editMessageEmbeds(createConfigEmbed(owner))
                            .setActionRow(getComponents())
                            .queue()
            );
        }else {
            voiceChannel.sendMessageEmbeds(createConfigEmbed(owner))
                    .setActionRow(getComponents())
                    .queue(message -> configMessageId = message.getId());
        }
    }

    @Nonnull
    protected MessageEmbed createConfigEmbed(Member owner) {
        return new JafarEmbed()
                .setTitle("Salon de " + owner.getEffectiveName())
                .setDescription(
                        "**Informations:**\n" +
                        "> Status: " + (locked ? "\uD83D\uDD12" : "\uD83D\uDD13") + "\n" +
                        "\n" +
                        "**Boutons**\n" +
                        "> " + (!locked ? "\uD83D\uDD12" : "\uD83D\uDD13") + " *: " + (locked ? "Déverrouiller" : "Verrouiller") + " le salon*\n" +
                        "> \uD83D\uDDD1 *: Fermer le salon*"
                )
                .setColor(JafarEmbed.NEUTRAL_COLOR).build();
    }

    @Nonnull
    protected ItemComponent[] getComponents() {
        return new ItemComponent[] {
                Button.secondary("JafarBot-LockeableChannel-Lock", Emoji.fromUnicode(locked ? "\uD83D\uDD13" : "\uD83D\uDD12")),
                Button.secondary("JafarBot-LockeableChannel-Delete", Emoji.fromUnicode("\uD83D\uDDD1️"))
        };
    }

    /*
       Getters
     */

    public boolean isLocked() {
        return locked;
    }
}
