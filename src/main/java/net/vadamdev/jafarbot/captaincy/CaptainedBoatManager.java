package net.vadamdev.jafarbot.captaincy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.captaincy.menu.BoatSettingsMainMenu;
import net.vadamdev.jafarbot.profile.Profile;
import net.vadamdev.jafarbot.profile.ProfileManager;
import net.vadamdev.jafarbot.utils.JafarEmbed;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CaptainedBoatManager {
    private final MessageEmbed NOT_OWNER_MESSAGE = new JafarEmbed()
            .setTitle("Bateaux Capitainés")
            .setDescription("Vous pouvez interagir avec ces boutons seulement si vous êtes le propriétaire de ce bateau.")
            .setColor(JafarEmbed.ERROR_COLOR).build();

    private final ProfileManager profileManager;

    public CaptainedBoatManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    /*
       Handle Events
     */

    public void handleVoiceUpdateEvent(@Nonnull GuildVoiceUpdateEvent event) {
        final AudioChannelUnion joined = event.getChannelJoined();
        final AudioChannelUnion left = event.getChannelLeft();

        if(joined != null && joined.getType().equals(ChannelType.VOICE)) {
            final VoiceChannel voiceChannel = joined.asVoiceChannel();
            final String channelId = voiceChannel.getId();
            final Member member = event.getMember();
            final Guild guild = event.getGuild();

            if(channelId.equals(Main.jafarBot.mainConfig.CAPTAINED_BOAT_CREATOR)) {
                final Profile profile = profileManager.getProfile(member.getId());

                if(profile.getCaptainedBoat() != null)
                    profile.getCaptainedBoat().createChannel(guild);
                else
                    guild.moveVoiceMember(member, null).queue();
            }else if(!member.hasPermission(Permission.ADMINISTRATOR) && voiceChannel.getMembers().size() >= voiceChannel.getUserLimit())
                getCaptainedBoats().stream()
                        .filter(captainedBoat -> channelId.equals(captainedBoat.getChannelId()))
                        .filter(CaptainedBoat::isHeavyLocked)
                        .findFirst().ifPresent(captainedBoat -> {
                            guild.moveVoiceMember(member, event.getChannelLeft()).queue();

                            member.getUser().openPrivateChannel()
                                    .flatMap(channel -> channel.sendMessageEmbeds(new JafarEmbed()
                                            .setTitle("Bateaux Capitainés")
                                            .setDescription("Ce channel a été bloquer par son capitaine. **Vous ne pouvez pas le rejoindre** !")
                                            .setColor(JafarEmbed.ERROR_COLOR).build())).queue();
                        });
        }

        if(left != null && left.getType().equals(ChannelType.VOICE)) {
            final VoiceChannel voiceChannel = left.asVoiceChannel();
            if(voiceChannel.getMembers().isEmpty()) {
                getCaptainedBoats().stream()
                        .filter(CaptainedBoat::isAlive)
                        .filter(boat -> boat.getChannelId().equals(voiceChannel.getId()))
                        .findFirst().ifPresent(boat -> boat.deleteChannel(event.getGuild(), false));
            }
        }
    }

    public void handleButtonInteractionEvent(@Nonnull ButtonInteractionEvent event) {
        final String channelId = event.getChannel().getId();
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final String memberId = member.getId();

        switch(event.getComponentId()) {
            case "JafarBot-CaptainedBoat-Lock":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    captainedBoat.setLocked(guild, !captainedBoat.isLocked(), false);
                    event.deferEdit().queue();
                });

                break;
            case "JafarBot-CaptainedBoat-ForceLock":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    captainedBoat.setLocked(guild, true, true);
                    event.deferEdit().queue();
                });

                break;
            case "JafarBot-CaptainedBoat-Delete":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    event.replyEmbeds(new JafarEmbed()
                            .setTitle("Bateau de " + member.getEffectiveName())
                            .setDescription("Êtes-vous sur(e) de vouloir supprimer ce salon ?\n*Cela déconnectera toutes les personnes présentent à l'intérieur !*")
                            .setColor(JafarEmbed.NEUTRAL_COLOR).build()).setActionRow(
                                    Button.danger("JafarBot-CaptainedBoat-ConfirmDelete", "Confirmer")
                            ).setEphemeral(true).queue();
                });

                break;
            case "JafarBot-CaptainedBoat-Settings":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    BoatSettingsMainMenu.MAIN_MENU.open(event);
                });

                break;
            case "JafarBot-CaptainedBoat-ConfirmDelete":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    event.deferEdit().queue();
                    captainedBoat.deleteChannel(guild, false);
                });

                break;
            default:
                break;
        }
    }

    public void handleSelectInteractionEvent(@Nonnull StringSelectInteractionEvent event) {
        if(!event.getComponentId().equals("JafarBot-CaptainedBoat-ChooseBoatType"))
            return;

        getCaptainedBoatByChannel(event.getChannel().getId()).ifPresent(captainedBoat -> {
            if(!captainedBoat.getOwnerId().equals(event.getMember().getId())) {
                event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                return;
            }

            event.deferEdit().queue();
            captainedBoat.setNameByBoatType(event.getGuild(), BoatType.valueOf(event.getValues().get(0)));
        });
    }

    public void handleChannelDelete(@Nonnull Guild guild, @Nonnull VoiceChannel voiceChannel) {
        getCaptainedBoatByChannel(voiceChannel.getId())
                .ifPresent(captainedBoat -> captainedBoat.deleteChannel(guild, true));
    }

    /*
       Utils
     */

    @Nonnull
    public CaptainedBoat getCaptainedBoatByUser(String userId) {
        final Profile profile = profileManager.getProfile(userId);

        if(profile.getCaptainedBoat() == null)
            profile.setCaptainedBoat(new CaptainedBoat(userId));

        return profile.getCaptainedBoat();
    }

    public Optional<CaptainedBoat> getCaptainedBoatByChannel(String channelId) {
        return profileManager.getProfiles().stream()
                .map(Profile::getCaptainedBoat)
                .filter(Objects::nonNull)
                .filter(CaptainedBoat::isAlive)
                .filter(captainedBoat -> captainedBoat.getChannelId().equals(channelId))
                .findFirst();
    }

    public List<CaptainedBoat> getCaptainedBoats() {
        return profileManager.getProfiles().stream()
                .map(Profile::getCaptainedBoat)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
