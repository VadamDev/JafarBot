package net.vadamdev.jafarbot.captaincy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.vadamdev.dbk.framework.interactive.api.components.InteractiveComponent;
import net.vadamdev.dbk.framework.interactive.api.registry.MessageRegistry;
import net.vadamdev.dbk.framework.interactive.entities.buttons.InteractiveButton;
import net.vadamdev.dbk.framework.interactive.entities.dropdowns.InteractiveStringSelectMenu;
import net.vadamdev.dbk.framework.menu.InteractiveComponentMenu;
import net.vadamdev.dbk.framework.utils.CachedMessage;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.channelcreator.LockeableCreatedChannel;
import net.vadamdev.jafarbot.channelcreator.system.CreatedChannel;
import net.vadamdev.jafarbot.profile.CaptainedFleet;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.Utils;
import org.jetbrains.annotations.Nullable;

/**
 * @author VadamDev
 * @since 12/04/2025
 */
public class BoatCreatedChannel extends CreatedChannel {
    private final CaptainedFleet fleet;

    private InteractiveComponentMenu configMenu;

    private String currentName;
    private boolean locked, heavyLocked;

    public BoatCreatedChannel(String channelId, String ownerId, CaptainedFleet fleet, String currentName) {
        super(channelId, ownerId);

        this.fleet = fleet;
        this.currentName = currentName;
    }

    @Override
    public void onChannelCreation(VoiceChannel channel, Member owner) {
        fleet.setOpenedChannelId(channel.getId());

        sendBoatTypeSelectMenu(channel);

        configMenu = createConfigMenu(owner);
        configMenu.display(channel).queue();
    }

    @Override
    public void onChannelDeletion(@Nullable VoiceChannel channel) {
        fleet.setOpenedChannelId(null);
    }

    private boolean isOwner(Member member, @Nullable IReplyCallback callback) {
        final boolean isOwner = member.getId().equals(ownerId);

        if(!isOwner && callback != null)
            callback.replyEmbeds(LockeableCreatedChannel.NOT_OWNER_MESSAGE).setEphemeral(true).queue();

        return isOwner;
    }

    /*
       Channel Locking
     */

    public void setLocked(Guild guild, boolean locked, boolean heavyLocked) {
        final VoiceChannel channel = guild.getVoiceChannelById(channelId);
        if(channel == null)
            return;

        final Member owner = guild.getMemberById(ownerId);
        if(owner == null)
            return;

        if(locked && !this.locked) {
            if(heavyLocked)
                this.heavyLocked = true;

            final int memberSize = channel.getMembers().size();
            channel.getManager().setUserLimit(Math.clamp(memberSize, 2, 4)).queue();
        }else if(!locked && this.locked) {
            this.heavyLocked = false;
            channel.getManager().setUserLimit(0).queue();
        }

        this.locked = locked;
        updateConfigMenu(owner);
    }

    /*
       Config Menu
     */

    //Menus

    private void updateConfigMenu(Member owner) {
        final CachedMessage cachedMessage = configMenu.getCachedMessage();

        cachedMessage.runIfExists(message -> {
            final InteractiveComponentMenu newMenu = createConfigMenu(owner);
            newMenu.display(message).queue();

            configMenu = newMenu;
        }, () -> {
            final InteractiveComponentMenu newMenu = createConfigMenu(owner);
            newMenu.display(cachedMessage.getChannel()).queue();

            this.configMenu = newMenu;
        });
    }

    @SuppressWarnings("unchecked")
    private InteractiveComponentMenu createConfigMenu(Member owner) {
        final InteractiveButton.Builder forceLockButton = InteractiveButton.of(ButtonStyle.SECONDARY)
                .emoji(Emoji.fromUnicode("⛔"))
                .action((event, invalidatable) -> {
                    if(!isOwner(event.getMember(), event))
                        return;

                    event.deferEdit().queue();
                    setLocked(event.getGuild(), true, true);
                });

        return InteractiveComponentMenu.builder()
                .addEmbed(createConfigMenuEmbed(owner))
                .addActionRow(
                        //Lock button
                        InteractiveButton.of(ButtonStyle.SECONDARY)
                                .emoji(Emoji.fromUnicode(locked ? "\uD83D\uDD12" : "\uD83D\uDD13"))
                                .action((event, invalidatable) -> {
                                    if(!isOwner(event.getMember(), event))
                                        return;

                                    event.deferEdit().queue();
                                    setLocked(event.getGuild(), !locked, false);
                                }).build(),

                        //Force lock button
                        owner.hasPermission(Permission.ADMINISTRATOR) && !locked && !heavyLocked
                                ? forceLockButton.build()
                                : forceLockButton.disabled().build(),

                        //Delete button
                        InteractiveButton.of(ButtonStyle.SECONDARY)
                                .emoji(Emoji.fromUnicode("\uD83D\uDDD1"))
                                .action((event, invalidatable) -> {
                                    if(!isOwner(event.getMember(), event))
                                        return;

                                    Utils.createDefaultConfirmationRequest(e -> {
                                        e.deferEdit().queue();
                                        JafarBot.get().getChannelCreatorManager().deleteCreatedChannel(e.getGuild(), channelId);
                                    }).send(event);
                                }).build(),

                        //Settings button
                        InteractiveButton.of(ButtonStyle.SECONDARY)
                                .emoji(Emoji.fromUnicode("⚙️"))
                                .action((event, invalidatable) -> {
                                    if(!isOwner(event.getMember(), event))
                                        return;

                                    BoatSettingsMenu.open(fleet, event);
                                }).build()
                ).build();
    }

    private void sendBoatTypeSelectMenu(VoiceChannel channel) {
        final MessageRegistry<StringSelectMenu> selectMenu = InteractiveStringSelectMenu.of(
                StringSelectMenu.create(InteractiveComponent.generateComponentUID()).addOptions(fleet.createNameSelectOptions()).build(),

                (event, invalidatable) -> {
                    final Member member = event.getMember();
                    if(!isOwner(member, event))
                        return;

                    event.deferEdit().queue();

                    final String newName = fleet.getNameByBoatType(BoatType.valueOf(event.getValues().get(0)));
                    if(newName == null)
                        return;

                    event.getGuild().getVoiceChannelById(channelId).getManager().setName(newName).queue();
                    currentName = newName;

                    updateConfigMenu(member);
                }
        );

        channel.sendMessageEmbeds(createBoatConfigMenuEmbed()).setActionRow(selectMenu.get()).queue(selectMenu::register);
    }

    //Embeds

    private MessageEmbed createConfigMenuEmbed(Member owner) {
        final String lockEmoji = locked ? "\uD83D\uDD12" : "\uD83D\uDD13";

        return EmbedUtils.defaultEmbed()
                .setTitle(currentName.split("┃")[1] + " (Commandé par " + owner.getEffectiveName() + ")")
                .setDescription(String.format(
                        """
                        **Informations:**
                        > Status: %s
                        > ForceLock: %s
                        
                        **Boutons:**
                        > %s *: %s le salon*
                        > ⛔ *: Force lock le salon __(Admin uniquement)__*
                        > \uD83D\uDDD1 *: Fermer le salon*
                        > ⚙️ *: Paramètres*
                        """,

                        lockEmoji,
                        Utils.formatBoolean(heavyLocked),
                        lockEmoji,
                        locked ? "Déverrouiller" : "Verrouiller"
                )).build();
    }

    private MessageEmbed createBoatConfigMenuEmbed() {
        return EmbedUtils.defaultEmbed()
                .setTitle("Type de bateau")
                .setDescription("Choisissez dans la liste déroulante le type de bateau que vous souhaitez utiliser.")
                .build();
    }

    /*
       Getters
     */

    public boolean isLocked() {
        return locked;
    }

    public boolean isHeavyLocked() {
        return heavyLocked;
    }
}
