package net.vadamdev.jafarbot.activity;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.vadamdev.jafarbot.Main;
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

    public ActivityTracker(JDA jda) {
        this.jda = jda;

        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.scheduledExecutorService.scheduleAtFixedRate(this::checkServerActivity, 0, 6, TimeUnit.HOURS);
    }

    public void checkServerActivity() {
        final Guild guild = jda.getGuildById(Main.jafarBot.mainConfig.GUILD_ID);
        final Role inactiveRole = guild.getRoleById(Main.jafarBot.mainConfig.STUCKED_ROLE);
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
        if(member.getRoles().stream().anyMatch(r -> r.getId().equals(Main.jafarBot.mainConfig.FRIEND_ROLE)))
            return false;

        Date date = Date.from(new Date(profile.getLastActivity()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .plusDays(14).atZone(ZoneId.systemDefault()).toInstant());

        return currentDate.after(date);
    }

    public void shutdown() {
        scheduledExecutorService.shutdownNow();
    }

    public static boolean hasInactiveRole(Member member, String inactiveRoleId) {
        return member.getRoles().stream().anyMatch(r -> r.getId().equals(inactiveRoleId));
    }
}
