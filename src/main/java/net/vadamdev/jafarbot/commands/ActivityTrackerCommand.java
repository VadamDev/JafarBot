package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import net.vadamdev.jdautils.commands.data.SlashCmdData;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VadamDev
 * @since 24/01/2024
 */
public class ActivityTrackerCommand extends Command implements ISlashCommand {
    public ActivityTrackerCommand() {
        super("activity-tracker");
        setPermission(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        final SlashCommandInteractionEvent event = commandData.castOrNull(SlashCmdData.class).getEvent();

        final OptionMapping enabledOption = event.getOption("enabled");
        final boolean refresh = event.getOption("refresh", false, OptionMapping::getAsBoolean);

        final boolean currentlyEnabled = Main.jafarBot.mainConfig.ACTIVITY_TRACKER;

        if(enabledOption == null) {
            if(refresh) {
                Main.jafarBot.getActivityTracker().checkServerActivity();

                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Activity Tracker")
                        .setDescription("La vérification de l'activité de chaque membre a été effectuée !")
                        .setColor(JafarEmbed.SUCCESS_COLOR)
                        .build()).queue();
            }else {
                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Activity Tracker")
                        .setDescription("La détection de l'activité est actuellement __" + (currentlyEnabled ? "activé" : "désactivé") + "__ !")
                        .setColor(JafarEmbed.SUCCESS_COLOR)
                        .build()).queue();
            }
        }else {
            final boolean enabled = enabledOption.getAsBoolean();

            if(enabled != currentlyEnabled) {
                try {
                    Main.jafarBot.mainConfig.setValue("ACTIVITY_TRACKER", enabled);
                    Main.jafarBot.mainConfig.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(refresh)
                    Main.jafarBot.getActivityTracker().checkServerActivity();

                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Activity Tracker")
                        .setDescription("La détection de l'activité est maintenant __" + (enabled ? "activé" : "désactivé") + "__ !")
                        .setColor(JafarEmbed.SUCCESS_COLOR)
                        .build()).queue();
            }else {
                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Activity Tracker")
                        .setDescription("La détection de l'activité déjà " + (currentlyEnabled ? "activé" : "désactivé") + " !")
                        .setColor(JafarEmbed.ERROR_COLOR)
                        .build()).queue();
            }
        }
    }

    @Nonnull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Commande dédié à la gestion du détecteur d'activité")
                .addOptions(
                        new OptionData(OptionType.BOOLEAN, "enabled", "Définie si le détecteur d'activité doit être activé"),
                        new OptionData(OptionType.BOOLEAN, "refresh", "Définie si les inactifs doivent être mis à jour lors de l'exécution de la commande")
                );
    }
}
