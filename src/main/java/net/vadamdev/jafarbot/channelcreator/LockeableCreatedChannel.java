package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.vadamdev.dbk.framework.interactive.api.registry.MessageRegistry;
import net.vadamdev.dbk.framework.interactive.entities.buttons.InteractiveButton;
import net.vadamdev.dbk.framework.menu.InteractiveComponentMenu;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.channelcreator.system.CreatedChannel;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.Utils;
import org.jetbrains.annotations.Nullable;

/**
 * @author VadamDev
 * @since 01/04/2025
 */
public class LockeableCreatedChannel extends CreatedChannel {
    public static MessageEmbed NOT_OWNER_MESSAGE = EmbedUtils.defaultEmbed(EmbedUtils.ERROR_COLOR)
            .setTitle("Salon Personnalisé")
            .setDescription("Vous pouvez interagir avec ces boutons seulement si vous êtes le propriétaire de ce salon !").build();

    protected InteractiveComponentMenu menu;
    private boolean locked;

    public LockeableCreatedChannel(String channelId, String ownerId) {
        super(channelId, ownerId);
    }

    @Override
    public void onChannelCreation(VoiceChannel channel, Member owner) {
        createOrUpdateConfigMenu(channel, owner);
    }

    private void createOrUpdateConfigMenu(VoiceChannel voiceChannel, Member owner) {
        if(menu == null) {
            menu = createConfigMenu(owner).build();
            menu.display(voiceChannel).queue();
        }else {
            final JDA jda = voiceChannel.getJDA();

            menu.getCachedMessage().runIfExists(message -> {
                menu.invalidate(jda);

                final InteractiveComponentMenu newMenu = createConfigMenu(owner).build();
                newMenu.display(message).queue();
                menu = newMenu;
            });

        }
    }

    /*
       Utility
     */

    public void setLocked(Guild guild, boolean locked) {
        final VoiceChannel voiceChannel = retrieveChannel(guild);
        final Member owner = retrieveOwner(guild);

        if(voiceChannel == null || owner == null)
            return;

        if(locked && !this.locked) {
            final int memberSize = voiceChannel.getMembers().size();
            voiceChannel.getManager().setUserLimit(Math.max(memberSize, 2)).complete();
        }else if(!locked && this.locked)
            voiceChannel.getManager().setUserLimit(0).complete();
        else
            return;

        this.locked = locked;

        createOrUpdateConfigMenu(voiceChannel, owner);
    }

    public boolean isOwner(Member member, @Nullable IReplyCallback replyCallback) {
        final boolean isOwner = member.getId().equals(ownerId);

        if(!isOwner && replyCallback != null)
            replyCallback.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();

        return isOwner;
    }

    /*
       Menu
     */

    protected MessageRegistry<ActionComponent>[] createComponents(Member owner) {
        return new MessageRegistry[] {
                InteractiveButton.of(ButtonStyle.SECONDARY)
                        .emoji(Emoji.fromUnicode(locked ? "\uD83D\uDD13" : "\uD83D\uDD12"))
                        .action((event, invalidatable) -> {
                            if(!isOwner(event.getMember(), event))
                                return;

                            event.deferEdit().queue();
                            setLocked(event.getGuild(), !locked);
                        }).build(),
                InteractiveButton.of(ButtonStyle.SECONDARY)
                        .emoji(Emoji.fromUnicode("\uD83D\uDDD1"))
                        .action((event, invalidatable) -> {
                            if(!isOwner(event.getMember(), event))
                                return;

                            Utils.createDefaultConfirmationRequest(callback -> {
                                if(!isOwner(callback.getMember(), callback))
                                    return;

                                callback.deferEdit().queue();
                                JafarBot.get().getChannelCreatorManager().deleteCreatedChannel(event.getGuild(), channelId);
                            }).send(event);
                        }).build()
        };
    }

    protected MessageEmbed createConfigMenuEmbed(Member owner) {
        final String lockEmoji = locked ? "\uD83D\uDD12" : "\uD83D\uDD13";

        return EmbedUtils.defaultEmbed()
                .setTitle("Salon de " + owner.getEffectiveName())
                .setDescription(String.format(
                        """
                        **Informations:**
                        > Status: %s
                        
                        **Boutons**
                        > %s *: %s le salon*
                        > \uD83D\uDDD1 *: Fermer le salon*
                        """,

                        lockEmoji,
                        lockEmoji,
                        locked ? "Déverrouiller" : "Verrouiller"
                )).build();
    }

    protected InteractiveComponentMenu.Builder createConfigMenu(Member owner) {
        return InteractiveComponentMenu.builder()
                .onInvalidate(null)
                .addEmbed(createConfigMenuEmbed(owner))
                .addActionRow(createComponents(owner));
    }

    /*
       Getters
     */

    public boolean isLocked() {
        return locked;
    }
}
