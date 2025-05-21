package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.vadamdev.dbk.framework.commands.annotations.AnnotationProcessor;
import net.vadamdev.dbk.framework.commands.annotations.CommandProcessor;
import net.vadamdev.jafarbot.ApplicationConfig;
import net.vadamdev.jafarbot.commands.api.GuildLinkedCommand;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class ActivityCommand extends GuildLinkedCommand {
    private final ApplicationConfig appConfig;

    public ActivityCommand(ApplicationConfig appConfig) {
        super("activity", "Permet de changer l'activité du bot");
        setRequiredPermissions(Permission.MESSAGE_MANAGE);

        this.appConfig = appConfig;
    }

    @CommandProcessor(subCommand = "set")
    private void set(SlashCommandInteractionEvent event) {
        final Activity.ActivityType type = event.getOption("type", mapping -> {
            final String typeString = mapping.getAsString();

            try {
                return Activity.ActivityType.valueOf(typeString);
            }catch (Exception ignored) {
                return Activity.ActivityType.PLAYING;
            }
        });

        updateStatus(event, type, event.getOption("activity", "Sea of Thieves", mapping -> {
            final String input = mapping.getAsString();

            if(input.length() > 128)
                return input.substring(0, 128);

            return input;
        }).substring(0, 128));
    }

    @CommandProcessor(subCommand = "reset")
    private void reset(SlashCommandInteractionEvent event) {
        updateStatus(event, null, null);
    }

    private void updateStatus(SlashCommandInteractionEvent event, @Nullable Activity.ActivityType type, @Nullable String activity) {
        try {
            if(type == null || activity == null)
                appConfig.updateActivity(event.getJDA(), null, null);
            else
                appConfig.updateActivity(event.getJDA(), type, activity);

            event.replyEmbeds(EmbedUtils.defaultSuccess("L'activité du bot a été mis à jour.")
                    .setTitle("JafarBot - Activité").build()).queue();
        }catch (IOException e) {
            e.printStackTrace();

            event.replyEmbeds(EmbedUtils.defaultError("Une erreur est survenue.")
                    .setTitle("JafarBot - Activité").build()).queue();
        }
    }

    @NotNull
    @Override
    public SlashCommandData createCommandData() {
        return super.createCommandData().addSubcommands(
                new SubcommandData("set", "Permet de définir l'activité du bot").addOptions(
                        new OptionData(OptionType.STRING, "type", "Type d'activité", true)
                                .addChoice("Joue à", "PLAYING")
                                .addChoice("Écoute", "LISTENING")
                                .addChoice("Regarde", "WATCHING")
                                .addChoice("Participant à", "COMPETING")
                                .addChoice("Custom", "CUSTOM_STATUS"),

                        new OptionData(OptionType.STRING, "activity", "Activité", true)
                                .setRequiredLength(1, 128)
                ),

                new SubcommandData("reset", "Supprime l'activité du bot")
        );
    }

    @Override
    public void executeCommand(Member sender, SlashCommandInteractionEvent event) {
        AnnotationProcessor.processAnnotations(event, this);
    }
}
