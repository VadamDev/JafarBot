package net.vadamdev.jafarbot.activity;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.vadamdev.dbk.framework.DBKFramework;
import net.vadamdev.jafarbot.GuildLinkService;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.configs.MainConfig;
import net.vadamdev.jafarbot.profile.Profile;
import net.vadamdev.jafarbot.profile.ProfileManager;
import net.vadamdev.jafarbot.utils.Utils;

import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author VadamDev
 * @since 22/06/2023
 */
public final class ActivityTracker {
    private final MainConfig mainConfig;
    private final ProfileManager profileManager;
    private final GuildLinkService guildLinkService;

    public ActivityTracker() {
        final JafarBot jafarBot = JafarBot.get();

        this.mainConfig = jafarBot.getConfig();
        this.profileManager = jafarBot.getProfileManager();
        this.guildLinkService = jafarBot.getGuildLinkService();

        DBKFramework.getScheduledExecutorMonoThread()
                .scheduleAtFixedRate(this::checkServerActivity, 0, 6, TimeUnit.HOURS);
    }

    public void checkServerActivity() {
        guildLinkService.ifLinked(guild -> {
            final Role inactiveRole = guild.getRoleById(mainConfig.INACTIVE_ROLE);
            if(inactiveRole == null)
                return;

            final Date currentDate = new Date();

            profileManager.getProfiles().stream()
                    .filter(profile -> !profile.isInVC())
                    .filter(profile -> guild.getMemberById(profile.getUserId()) != null)
                    .forEach(profile -> checkProfileActivity(profile, guild, inactiveRole, currentDate));
        });
    }

    private void checkProfileActivity(Profile profile, Guild guild, Role inactiveRole, Date currentDate) {
        final Member member = guild.getMemberById(profile.getUserId());

        final boolean hasInactiveRole = Utils.hasRole(member, inactiveRole.getId());
        final boolean shouldBeInactive = shouldBeInactive(profile, member, currentDate);

        if(!hasInactiveRole && shouldBeInactive) {
            guild.addRoleToMember(member, inactiveRole).queue();
            JafarBot.getLogger().info(member.getEffectiveName() + " was set as an inactive account");
        }else if(hasInactiveRole && !shouldBeInactive){
            guild.removeRoleFromMember(member, inactiveRole).queue();
            JafarBot.getLogger().info(member.getEffectiveName() + " was set as an active account");
        }
    }

    private boolean shouldBeInactive(Profile profile, Member member, Date currentDate) {
        if(!mainConfig.ACTIVITY_TRACKER)
            return false;

        if(Utils.hasRole(member, mainConfig.FRIEND_ROLE, mainConfig.RETIRED_ROLE))
            return false;

        final long lastActivity = profile.getLastActivity();
        if(lastActivity == 0)
            return false;

        //Wtf?
        final Date date = Date.from(new Date(lastActivity).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .plusDays(14).atZone(ZoneId.systemDefault()).toInstant());

        return currentDate.after(date);
    }
}
