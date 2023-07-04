package net.vadamdev.jafarbot.captaincy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.profile.Profile;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class CaptainedBoatManager {
    private final MessageEmbed NOT_OWNER_MESSAGE = new EmbedBuilder()
            .setTitle("Bateaux Capitainés")
            .setDescription("Vous pouvez interagir avec ces boutons seulement si vous êtes le propriétaire de ce bateau..")
            .setColor(Color.RED)
            .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build();

    private final JafarBot jafarBot;

    public CaptainedBoatManager() {
        this.jafarBot = Main.jafarBot;
    }

    /*
       Handle Events
     */

    public void handleVoiceUpdateEvent(@Nonnull GuildVoiceUpdateEvent event) {
        if(event.getChannelJoined() != null && event.getChannelJoined().getType().equals(ChannelType.VOICE)) {
            final VoiceChannel voiceChannel = event.getChannelJoined().asVoiceChannel();
            final String channelId = voiceChannel.getId();
            final Member member = event.getMember();
            final Guild guild = event.getGuild();

            if(channelId.equals(Main.jafarBot.mainConfig.CAPTAINED_BOAT_CREATOR)) {
                final Profile profile = jafarBot.getProfileManager().getProfile(member.getId());

                if(profile.getCaptainedBoat() != null)
                    profile.getCaptainedBoat().createChannel(guild);
                else
                    guild.moveVoiceMember(member, null).queue();
            }else if(!member.hasPermission(Permission.ADMINISTRATOR) && voiceChannel.getMembers().size() >= voiceChannel.getUserLimit())
                getCaptainedBoats().stream()
                        .filter(captainedBoat -> channelId.equals(captainedBoat.getChannelId()))
                        .findFirst().ifPresent(captainedBoat -> {
                            if(!captainedBoat.isHeavyLocked())
                                return;

                            guild.moveVoiceMember(member, event.getChannelLeft()).queue();

                            member.getUser().openPrivateChannel()
                                    .flatMap(channel -> channel.sendMessageEmbeds(new EmbedBuilder()
                                            .setTitle("Bateaux Capitainés")
                                            .setDescription("Ce channel a été bloquer par son capitaine. **Vous ne pouvez pas le rejoindre** !")
                                            .setColor(Color.RED)
                                            .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build())
                                    ).queue();
                        });
        }

        if(event.getChannelLeft() != null && event.getChannelLeft().getType().equals(ChannelType.VOICE)) {
            final VoiceChannel voiceChannel = event.getChannelLeft().asVoiceChannel();
            if(!voiceChannel.getMembers().isEmpty())
                return;

            getCaptainedBoats().stream()
                    .filter(boat -> boat.isAlive() && boat.getChannelId().equals(voiceChannel.getId()))
                    .findFirst().ifPresent(boat -> boat.deleteChannel(event.getGuild()));
        }
    }

    public void handleButtonInteractionEvent(@Nonnull ButtonInteractionEvent event) {
        final String channelId = event.getChannel().getId();
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final String memberId = member.getId();

        switch(event.getComponentId()) {
            case "JafarBot-lock":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    captainedBoat.setLocked(guild, !captainedBoat.isLocked(), false);
                    event.deferEdit().queue();
                });

                break;
            case "JafarBot-forceLock":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    captainedBoat.setLocked(guild, true, true);
                    event.deferEdit().queue();
                });

                break;
            case "JafarBot-deleteBoat":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Bateau de " + member.getEffectiveName())
                            .setDescription("Êtes-vous sur(e) de vouloir supprimer ce salon ?\n*Cela déconnectera toutes les personnes présentent à l'intérieur !*")
                            .setColor(Color.ORANGE)
                            .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).setActionRow(
                                    Button.primary("JafarBot-cancelDeleteBoat", "Annuler"),
                                    Button.danger("JafarBot-confirmDeleteBoat", "Confirmer")
                            ).setEphemeral(true).queue();
                });

                break;
            case "JafarBot-cancelDeleteBoat":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    event.getMessage().delete().queue();
                    event.deferEdit().queue();
                });

                break;
            case "JafarBot-confirmDeleteBoat":
                getCaptainedBoatByChannel(channelId).ifPresent(captainedBoat -> {
                    if(!captainedBoat.getOwnerId().equals(memberId)) {
                        event.replyEmbeds(NOT_OWNER_MESSAGE).setEphemeral(true).queue();
                        return;
                    }

                    event.deferEdit().queue();
                    captainedBoat.deleteChannel(guild);
                });

                break;
            default:
                break;
        }
    }

    public void handleSelectInteractionEvent(@Nonnull StringSelectInteractionEvent event) {
        if(!event.getComponentId().equals("JafarBot-ChooseBoatType"))
            return;

        getCaptainedBoatByChannel(event.getChannel().getId()).ifPresent(captainedBoat -> {
            captainedBoat.setNameByBoatType(event.getGuild(), BoatType.valueOf(event.getValues().get(0)));
            event.deferEdit().queue();
        });
    }

    public void handleChannelDelete(@Nonnull Guild guild, @Nonnull VoiceChannel voiceChannel) {
        getCaptainedBoatByChannel(voiceChannel.getId())
                .ifPresent(captainedBoat -> captainedBoat.deleteChannel(guild));
    }

    /*
       Utils
     */

    @Nonnull
    public CaptainedBoat getCaptainedBoatByUser(String userId) {
        final Profile profile = jafarBot.getProfileManager().getProfile(userId);

        if(profile.getCaptainedBoat() == null)
            profile.setCaptainedBoat(new CaptainedBoat(userId));

        return profile.getCaptainedBoat();
    }

    public Optional<CaptainedBoat> getCaptainedBoatByChannel(String channelId) {
        return getCaptainedBoats().stream()
                .filter(CaptainedBoat::isAlive)
                .filter(captainedBoat -> captainedBoat.getChannelId().equals(channelId))
                .findFirst();
    }

    public List<CaptainedBoat> getCaptainedBoats() {
        return jafarBot.getProfileManager().getProfiles().stream()
                .filter(profile -> profile.getCaptainedBoat() != null)
                .collect(new CaptainedBoatCollector());
    }

    /*
       Custom Collector
     */

   private static class CaptainedBoatCollector implements Collector<Profile, List<CaptainedBoat>, List<CaptainedBoat>>  {
        @Override
        public Supplier<List<CaptainedBoat>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<CaptainedBoat>, Profile> accumulator() {
            return (list, profile) -> list.add(profile.getCaptainedBoat());
        }

        @Override
        public BinaryOperator<List<CaptainedBoat>> combiner() {
            return (list1, list2) -> {
                list1.addAll(list2);
                return list1;
            };
        }

        @Override
        public Function<List<CaptainedBoat>, List<CaptainedBoat>> finisher() {
            return Collections::unmodifiableList;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.singleton(Characteristics.UNORDERED);
        }
    }
}
