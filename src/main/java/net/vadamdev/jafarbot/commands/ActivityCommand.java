package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import net.vadamdev.jdautils.commands.data.impl.TextCommandData;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class ActivityCommand extends Command implements ISlashCommand {
    private final MessageEmbed SUCCESS_MESSAGE = new EmbedBuilder()
            .setTitle("Activité")
            .setDescription("Le status d'activité a été mis à jour.")
            .setColor(Color.ORANGE)
            .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
            .build();

    public ActivityCommand() {
        super("activity");
        setPermission(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        JDA jda = null;
        Activity.ActivityType activityType = null;
        String activity = null;

        if(commandData.getType().equals(ICommandData.Type.SLASH)) {
            SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

            jda = event.getJDA();
            activityType = Activity.ActivityType.valueOf(event.getOption("activitytype").getAsString().toUpperCase());
            activity = event.getOption("activity").getAsString();

            event.replyEmbeds(SUCCESS_MESSAGE).queue();
        }else if(commandData.getType().equals(ICommandData.Type.TEXT)) {
            TextCommandData textCommandData = (TextCommandData) commandData;

            String[] args = textCommandData.getArgs();
            Message message = textCommandData.getEvent().getMessage();

            if(args.length < 2) {
                message.replyEmbeds(new EmbedBuilder()
                        .setTitle("Activité - Erreur")
                        .setDescription("Une erreur est survenue.\nUtilisez la commande ``!activity (PLAYING/LISTENING/WATCHING/COMPETING) <text>``")
                        .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
                        .setColor(Color.RED)
                        .build()).queue();

                return;
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; i++)
                builder.append(args[i] + (i < args.length - 1 ? " " : ""));

            jda = textCommandData.getEvent().getJDA();
            activityType = Activity.ActivityType.valueOf(args[0].toUpperCase());
            activity = builder.toString();

            message.replyEmbeds(SUCCESS_MESSAGE).queue();
        }

        try {
            Main.jafarBot.mainConfig.updateActivity(jda, activityType, activity);
        } catch (IOException e) {
            e.printStackTrace();
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
