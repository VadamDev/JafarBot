package net.vadamdev.jafarbot.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.captaincy.PortChannelCreator;
import net.vadamdev.jafarbot.configs.MainConfig;
import net.vadamdev.jafarbot.profile.Profile;
import net.vadamdev.jafarbot.profile.ProfileManager;
import net.vadamdev.jafarbot.utils.EmbedUtils;

import java.time.Instant;

/**
 * @author VadamDev
 * @since 02/03/2023
 */
public class EventListener extends ListenerAdapter {
    private final MainConfig mainConfig;
    private final ProfileManager profileManager;

    public EventListener(MainConfig mainConfig, ProfileManager profileManager) {
        this.mainConfig = mainConfig;
        this.profileManager = profileManager;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        final User user = event.getUser();
        if(user.isBot())
            return;

        final Guild guild = event.getGuild();

        //Create the profile
        profileManager.getOrCreateProfile(user.getId());

        //Add default roles
        for(String roleId : mainConfig.ONBOARDING_ROLES) {
            final Role role = guild.getRoleById(roleId);
            if(role == null) {
                JafarBot.getLogger().warn("Failed to give onboarding role " + roleId + " to user " + user.getId());
                continue;
            }

            guild.addRoleToMember(user, role).queue();
        }

        //Send a welcome message
        guild.getTextChannelById(mainConfig.WELCOME_CHANNEL).sendMessageEmbeds(EmbedUtils.defaultEmbed()
                .setTitle("Bienvenue " + user.getEffectiveName() + " !")
                .setDescription("Bienvenue **" + user.getAsMention() + "** sur le discord de **" + guild.getName() + "** !")
                .setThumbnail(user.getAvatarUrl())
                .setTimestamp(Instant.now()).build()).queue();
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final String memberId = member.getId();

        final AudioChannelUnion joined = event.getChannelJoined();
        final AudioChannelUnion left = event.getChannelLeft();

        //Cancel bot move if the channel has a user limit of 1
        if(member.getUser().isBot()) {
            if(event.getJDA().getSelfUser().getId().equals(memberId) && joined != null && left != null && joined.getUserLimit() == 1)
                guild.moveVoiceMember(member, left).queue();

            return;
        }

        //Handle BoatCreatedChannel force lock
        PortChannelCreator.handleGuildVoiceUpdate(event);

        //Profile Activity
        final VoiceChannel afkChannel = guild.getAfkChannel();
        final Profile profile = profileManager.getOrCreateProfile(memberId);

        if(joined != null && !isAFKChannel(joined, afkChannel) && left == null)
            profile.updateConnectionTime();
        else if(left != null && !isAFKChannel(left, afkChannel) && joined == null)
            profile.updateAndComputeDeconnectionTime();
        else if(joined != null && left != null) {
            if(isAFKChannel(joined, afkChannel))
                profile.updateAndComputeDeconnectionTime();
            else if(isAFKChannel(left, afkChannel))
                profile.updateConnectionTime();
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        profileManager.deleteProfile(event.getUser().getId());
    }

    private boolean isAFKChannel(AudioChannelUnion voiceChannel, VoiceChannel afKChannel) {
        return afKChannel != null && afKChannel.getId().equals(voiceChannel.getId());
    }
}
