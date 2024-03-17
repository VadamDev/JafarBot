package net.vadamdev.jafarbot.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.config.MainConfig;
import net.vadamdev.jafarbot.profile.Profile;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * @author VadamDev
 * @since 02/03/2023
 */
public class EventListener extends ListenerAdapter {
    private final JafarBot jafarBot;
    private final MainConfig mainConfig;

    public EventListener() {
        this.jafarBot = Main.jafarBot;
        this.mainConfig = jafarBot.mainConfig;
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        final User user = event.getUser();
        if(user.isBot())
            return;

        final Guild guild = event.getGuild();

        //Add default roles
        guild.addRoleToMember(user, guild.getRoleById(mainConfig.MEMBER_ROLE)).queue();
        guild.addRoleToMember(user, guild.getRoleById(mainConfig.SEPARATOR_1_ROLE)).queue();
        guild.addRoleToMember(user, guild.getRoleById(mainConfig.SEPARATOR_2_ROLE)).queue();
        guild.addRoleToMember(user, guild.getRoleById(mainConfig.SEPARATOR_3_ROLE)).queue();

        //Send a welcome message
        guild.getTextChannelById(mainConfig.WELCOME_CHANNEL).sendMessageEmbeds(new JafarEmbed()
                .setTitle("Bienvenue " + user.getEffectiveName() + " !")
                .setDescription("Bienvenue **" + user.getAsMention() + "** sur le discord de **" + guild.getName() + "** !")
                .setThumbnail(user.getAvatarUrl())
                .setTimestamp(Instant.now())
                .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();

        //Create the profile
        jafarBot.getProfileManager().getProfile(user.getId());
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        jafarBot.getChannelCreatorManager().handleButtonInteractionEvent(event);
        jafarBot.getCaptainedBoatManager().handleButtonInteractionEvent(event);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        jafarBot.getChannelCreatorManager().handleSelectInteractionEvent(event);
        jafarBot.getCaptainedBoatManager().handleSelectInteractionEvent(event);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        jafarBot.getChannelCreatorManager().handleModalInteractionEvent(event);
    }

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final String memberId = member.getId();

        final AudioChannelUnion channelJoined = event.getChannelJoined();
        final AudioChannelUnion channelLeft = event.getChannelLeft();

        if(member.getUser().isBot()) {
            if(event.getJDA().getSelfUser().getId().equals(memberId) && channelJoined != null && channelLeft != null && channelJoined.getUserLimit() == 1) {
                guild.moveVoiceMember(member, channelLeft).queue();
            }

            return;
        }

        jafarBot.getChannelCreatorManager().handleVoiceUpdateEvent(event);
        jafarBot.getCaptainedBoatManager().handleVoiceUpdateEvent(event);

        final VoiceChannel afkChannel = guild.getAfkChannel();

        final Profile profile = jafarBot.getProfileManager().getProfile(memberId);

        if(channelJoined != null && !isAfkChannel(channelJoined, afkChannel) && channelLeft == null)
            profile.updateConnectionTime();
        else if(channelLeft != null && !isAfkChannel(channelLeft, afkChannel) && channelJoined == null)
            profile.updateAndComputeDeconnectionTime();
        else if(channelJoined != null && channelLeft != null) {
            if(isAfkChannel(channelJoined, afkChannel))
                profile.updateAndComputeDeconnectionTime();
            else if(isAfkChannel(channelLeft, afkChannel))
                profile.updateConnectionTime();
        }
    }

    @Override
    public void onChannelDelete(@Nonnull ChannelDeleteEvent event) {
        if(!event.getChannelType().equals(ChannelType.VOICE))
            return;

        final VoiceChannel voiceChannel = event.getChannel().asVoiceChannel();

        jafarBot.getChannelCreatorManager().handleChannelDelete(voiceChannel);
        jafarBot.getCaptainedBoatManager().handleChannelDelete(event.getGuild(), voiceChannel);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        jafarBot.getProfileManager().deleteProfile(event.getUser().getId());
    }

    private boolean isAfkChannel(AudioChannelUnion voiceChannel, VoiceChannel afkChannel) {
        return afkChannel != null && afkChannel.getId().equals(voiceChannel.getId());
    }
}
