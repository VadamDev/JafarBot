package net.vadamdev.jafarbot.rolereaction;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.vadamdev.jafarbot.Main;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * @author VadamDev
 * @since 02/03/2023
 */
public final class RoleOption {
    private final String roleId;
    private final Emoji emoji;

    private RoleOption(String roleId, Emoji emoji) {
        this.roleId = roleId;
        this.emoji = emoji;
    }

    String getRoleId() {
        return roleId;
    }

    Emoji getEmoji() {
        return emoji;
    }

    boolean hasRole(Member member) {
        return member.getRoles().stream().anyMatch(role -> role.getId().equals(roleId));
    }

    void computeRole(Member member, IReplyCallback replyCallback) {
        final Guild guild = member.getGuild();

        final Role role = guild.getRoleById(roleId);
        if(role == null) {
            Main.logger.error("Specified role (" + roleId + ") does not exist !");
            return;
        }

        if(hasRole(member))
            guild.removeRoleFromMember(member, role).queue(v -> replyCallback.replyEmbeds(new EmbedBuilder()
                    .setTitle("Role Reaction")
                    .setDescription("Le role " + role.getAsMention() + " vous a été retirer !")
                    .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
                    .setColor(Color.ORANGE)
                    .build()).setEphemeral(true).queue());
        else
            guild.addRoleToMember(member, role).queue(v -> replyCallback.replyEmbeds(new EmbedBuilder()
                    .setTitle("Role Reaction")
                    .setDescription("Le role " + role.getAsMention() + " vous a été attribuer !")
                    .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
                    .setColor(Color.ORANGE)
                    .build()).setEphemeral(true).queue());
    }

    public static RoleOption of(@Nonnull String role, @Nonnull Emoji emoji) {
        return new RoleOption(role, emoji);
    }
}
