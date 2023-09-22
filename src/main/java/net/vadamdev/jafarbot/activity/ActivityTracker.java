package net.vadamdev.jafarbot.activity;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.config.MainConfig;
import net.vadamdev.jafarbot.profile.Profile;

import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author VadamDev
 * @since 22/06/2023
 */
public final class ActivityTracker {
    private final JDA jda;
    private final ScheduledExecutorService scheduledExecutorService;

    private final MainConfig mainConfig;

    public ActivityTracker(JDA jda) {
        this.jda = jda;

        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.scheduledExecutorService.scheduleAtFixedRate(this::checkServerActivity, 0, 6, TimeUnit.HOURS);

        this.mainConfig = Main.jafarBot.mainConfig;
    }

    public void checkServerActivity() {
        final Guild guild = jda.getGuildById(mainConfig.GUILD_ID);
        final Role inactiveRole = guild.getRoleById(mainConfig.INACTIVE_ROLE);
        final Date currentDate = new Date();

        Main.jafarBot.getProfileManager().getProfiles().stream()
                .filter(profile -> !profile.isInVocal())
                .filter(profile -> guild.getMemberById(profile.getUserId()) != null)
                .forEach(profile -> checkProfileActivity(profile, guild, inactiveRole, currentDate));
    }

    private void checkProfileActivity(Profile profile, Guild guild, Role inactiveRole, Date currentDate) {
        final Member member = guild.getMemberById(profile.getUserId());

        final boolean hasInactiveRole = hasInactiveRole(member, inactiveRole.getId());
        final boolean shouldBeInactive = shouldBeInactive(profile, member, currentDate);

        if(!hasInactiveRole && shouldBeInactive) {
            guild.addRoleToMember(member, inactiveRole).queue();
            Main.logger.info(member.getEffectiveName() + " was set as an inactive account");
        }else if(hasInactiveRole && !shouldBeInactive){
            guild.removeRoleFromMember(member, inactiveRole).queue();
            Main.logger.info(member.getEffectiveName() + " was set as an active account");
        }
    }

    private boolean shouldBeInactive(Profile profile, Member member, Date currentDate) {
        if(hasRole(member, mainConfig.FRIEND_ROLE, mainConfig.RETIRED_ROLE))
            return false;

        final long lastActivity = profile.getLastActivity();
        if(lastActivity == 0)
            return false;

        final Date date = Date.from(new Date(lastActivity).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .plusDays(14).atZone(ZoneId.systemDefault()).toInstant());

        return currentDate.after(date);
    }

    public void shutdown() {
        scheduledExecutorService.shutdownNow();
    }

    public static boolean hasInactiveRole(Member member, String inactiveRoleId) {
        return member.getRoles().stream().anyMatch(r -> r.getId().equals(inactiveRoleId));
    }

    private boolean hasRole(Member member, String... roleIds) {
        return member.getRoles().stream().anyMatch(role -> {
            for (String id : roleIds) {
                if(role.getId().equals(id)) {
                    return true;
                }
            }

            return false;
        });
    }
}
