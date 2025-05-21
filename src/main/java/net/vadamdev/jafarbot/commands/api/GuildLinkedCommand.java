package net.vadamdev.jafarbot.commands.api;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.vadamdev.dbk.framework.commands.GuildSlashCommand;
import net.vadamdev.jafarbot.JafarBot;

/**
 * @author VadamDev
 * @since 31/03/2025
 */
public abstract class GuildLinkedCommand extends GuildSlashCommand {
    public GuildLinkedCommand(String name, String description) {
        super(name, description);
    }

    public GuildLinkedCommand(String name) {
        super(name);
    }

    @Override
    public void execute(Member sender, SlashCommandInteractionEvent event) {
        JafarBot.get().getGuildLinkService().getLinkedGuildId().ifPresent(guildId -> {
            if(!event.getGuild().getId().equals(guildId)) {
                event.reply("Cette commande n'est pas disponible sur ce serveur !").setEphemeral(true).queue();
                return;
            }

            executeCommand(sender, event);
        });
    }

    public abstract void executeCommand(Member sender, SlashCommandInteractionEvent event);
}
