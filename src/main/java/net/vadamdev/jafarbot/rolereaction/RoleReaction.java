package net.vadamdev.jafarbot.rolereaction;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.vadamdev.jafarbot.Main;

import java.awt.*;
import java.util.Optional;

/**
 * @author VadamDev
 * @since 02/03/2023
 */
public class RoleReaction {
    private final String name, title;
    private final RoleOption[] roleOptions;

    public RoleReaction(String name, String title, RoleOption... roleOptions) {
        this.name = name;
        this.title = title;
        this.roleOptions = roleOptions;
    }

    public void sendReactionMessage(GuildMessageChannel messageChannel) {
        final ItemComponent[] components = new ItemComponent[roleOptions.length];
        for (int i = 0; i < roleOptions.length; i++)
            components[i] = Button.secondary("roleReaction-" + name + "-" + roleOptions[i].getRoleId(), roleOptions[i].getEmoji());

        messageChannel.sendMessageEmbeds(createEmbed(messageChannel.getGuild())).setActionRow(components).queue();
    }

    protected MessageEmbed createEmbed(Guild guild) {
        final StringBuilder description = new StringBuilder("__Veuillez choisir votre/vos rôle(s) :__\n\n");
        for (RoleOption roleOption : roleOptions)
            description.append(roleOption.getEmoji().getName() + " • " + guild.getRoleById(roleOption.getRoleId()).getName() + "\n");

        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description.toString())
                .setColor(Color.YELLOW)
                .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
                .build();
    }

    Optional<RoleOption> findRoleOptionByRoleId(String roleId) {
        for(RoleOption option : roleOptions) {
            if(!option.getRoleId().equals(roleId))
                continue;

            return Optional.of(option);
        }

        return Optional.empty();
    }

    String getName() {
        return name;
    }
}
