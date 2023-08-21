package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
import java.io.IOException;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class ActivityCommand extends Command implements ISlashCommand {
    public ActivityCommand() {
        super("activity");
        setPermission(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        final SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

        try {
            Main.jafarBot.mainConfig.updateActivity(event.getJDA(), Activity.ActivityType.valueOf(event.getOption("activitytype").getAsString().toUpperCase()), event.getOption("activity").getAsString());

            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Activité")
                    .setDescription("Le status d'activité a été mis à jour.")
                    .setColor(Color.ORANGE)
                    .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();
        } catch (IOException e) {
            e.printStackTrace();

            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Activité")
                    .setDescription("Une érreur est survenue.")
                    .setColor(Color.RED)
                    .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();
        }
    }

    @Override
    public @Nonnull SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Commande permettant de changer l'activité du bot")
                .addOptions(
                        new OptionData(OptionType.STRING, "activitytype", "Type d'activité")
                                .addChoice("Joue à", "PLAYING")
                                .addChoice("Ecoute", "LISTENING")
                                .addChoice("Regarde", "WATCHING")
                                .addChoice("Participant à", "COMPETING")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "activity", "L'activité")
                                .setRequired(true)
                );
    }
}
