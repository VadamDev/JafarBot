package net.vadamdev.jafarbot.captaincy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.vadamdev.dbk.framework.tuple.ImmutablePair;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.channelcreator.SimpleChannelCreator;
import net.vadamdev.jafarbot.channelcreator.system.ChannelCreatorManager;
import net.vadamdev.jafarbot.channelcreator.system.CreatedChannel;
import net.vadamdev.jafarbot.profile.CaptainedFleet;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author VadamDev
 * @since 12/04/2025
 */
public class PortChannelCreator extends SimpleChannelCreator {
    public PortChannelCreator(String creatorId, BiFunction<VoiceChannel, Member, CreatedChannel> channelCreatorFunction, Function<Member, String> namingFunction, Function<Member, Category> categoryFunction) {
        super(creatorId, channelCreatorFunction, namingFunction, categoryFunction);
    }

    @Override
    public CompletableFuture<CreatedChannel> createChannel(ChannelCreatorManager manager, Guild guild, Member owner) {
        final CaptainedFleet fleet = retrieveFleet(owner);
        if(fleet == null)
            return super.createChannel(manager, guild, owner);

        if(fleet.isInSea() && fleet.getOpenedChannelId() != null) {
            final AudioChannel channel = guild.getChannelById(AudioChannel.class, fleet.getOpenedChannelId());

            if(channel != null) {
                guild.moveVoiceMember(owner, channel).queue();
                return CompletableFuture.completedFuture(null);
            }
        }

        final BoatType preferredBoatType = fleet.getPreferredBoatType();

        final int userLimit = fleet.isDefaultLocked() ? preferredBoatType.getCrewSize() : 0;
        final String channelName = fleet.getNameByPreference();

        return guild.createVoiceChannel(channelName, categoryFunction.apply(owner)).setUserlimit(userLimit).submit()
                .thenApply(channel -> {
                    final CreatedChannel createdChannel = new BoatCreatedChannel(channel.getId(), owner.getId(), fleet, channelName);
                    createdChannel.onChannelCreation(channel, owner);

                    guild.moveVoiceMember(owner, channel).queue();

                    return createdChannel;
                });
    }

    public static void handleGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        final AudioChannelUnion joined = event.getChannelJoined();
        if(joined == null)
            return;

        final Member member = event.getMember();
        if(member.hasPermission(Permission.ADMINISTRATOR))
            return;

        final VoiceChannel voiceChannel = joined.asVoiceChannel();
        if(voiceChannel.getMembers().size() < voiceChannel.getUserLimit())
            return;

        final Guild guild = event.getGuild();

        JafarBot.get().getChannelCreatorManager().findCreatedChannel(joined.getId())
                .map(ImmutablePair::getRight)
                .filter(BoatCreatedChannel.class::isInstance)
                .map(BoatCreatedChannel.class::cast)
                .filter(BoatCreatedChannel::isHeavyLocked)
                .ifPresent(createdChannel -> {
                    guild.moveVoiceMember(member, event.getChannelLeft()).queue();

                    member.getUser().openPrivateChannel().flatMap(channel ->
                            channel.sendMessageEmbeds(
                                    EmbedUtils.defaultEmbed(EmbedUtils.ERROR_COLOR)
                                            .setTitle("Bateaux Capitainés")
                                            .setDescription("Ce channel a été bloquer par son capitaine. **Vous ne pouvez pas le rejoindre** !")
                                            .build()
                            )).queue();
                });
    }

    @Nullable
    private static CaptainedFleet retrieveFleet(Member member) {
        return JafarBot.get().getProfileManager().getOrCreateProfile(member.getId()).getCaptainedFleet();
    }
}
