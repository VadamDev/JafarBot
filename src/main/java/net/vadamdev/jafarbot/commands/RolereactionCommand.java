package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class RolereactionCommand extends Command implements ISlashCommand {
    public RolereactionCommand() {
        super("rolereaction");
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        final SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

        String id = event.getOption("id").getAsString();

        Main.jafarBot.getRoleReactionManager().findRoleReactionByName(id)
                .ifPresent(roleReaction -> roleReaction.sendReactionMessage(event.getChannel().asGuildMessageChannel()));

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("Rolereaction - Succès")
                .setDescription("Le rolereaction " + id + " a été créé !")
                .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
                .setColor(Color.ORANGE).build()).setEphemeral(true).queue();
    }

    @Override
    public @Nonnull SlashCommandData createSlashCommand() {
        final Set<Choice> choices =  Main.jafarBot.getRoleReactionManager().getRoleReactions().stream()
                .map(roleReaction -> new Choice(roleReaction, roleReaction))
                .collect(Collectors.toSet());

        return Commands.slash(name, "Créé un role reaction dans le salon ou la commande est envoyé")
                .addOptions(
                        new OptionData(OptionType.STRING, "id", "ID du Rolereaction")
                                .addChoices(choices)
                                .setRequired(true)
                );
    }
}
