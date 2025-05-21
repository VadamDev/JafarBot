package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.commands.api.GuildLinkedCommand;
import net.vadamdev.jafarbot.configs.MainConfig;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author VadamDev
 * @since 24/01/2024
 */
public class ActivityTrackerCommand extends GuildLinkedCommand {
    private final MainConfig mainConfig;

    public ActivityTrackerCommand(MainConfig mainConfig) {
        super("activity-tracker", "Commande dédié à la gestion du détecteur d'activité");

        this.mainConfig = mainConfig;
    }

    @Override
    public void executeCommand(Member sender, SlashCommandInteractionEvent event) {
        final OptionMapping enabledOption = event.getOption("enabled");
        final boolean refresh = event.getOption("refresh", false, OptionMapping::getAsBoolean);

        final boolean currentlyEnabled = mainConfig.ACTIVITY_TRACKER;

        if(enabledOption == null) {
            if(refresh) {
                JafarBot.get().getActivityTracker().checkServerActivity();

                event.replyEmbeds(EmbedUtils.defaultSuccess("La vérification de l'activité de chaque membre a été effectuée !")
                        .setTitle("JafarBot - Activity Tracker").build()).queue();
            }else {
                event.replyEmbeds(EmbedUtils.defaultSuccess("La détection de l'activité est actuellement __" + (currentlyEnabled ? "activé" : "désactivé") + "__ !")
                        .setTitle("JafarBot - Activity Tracker").build()).queue();
            }
        }else {
            final boolean enabled = enabledOption.getAsBoolean();

            if(enabled != currentlyEnabled) {
                try {
                    mainConfig.setValue("ACTIVITY_TRACKER", enabled);
                    mainConfig.save();
                } catch (IOException e) {
                    e.printStackTrace();
                    event.replyEmbeds(EmbedUtils.defaultError("Une erreur est survenue !").build()).queue();

                    return;
                }

                if(refresh)
                    JafarBot.get().getActivityTracker().checkServerActivity();

                event.replyEmbeds(EmbedUtils.defaultSuccess("La détection de l'activité est maintenant __" + (enabled ? "activé" : "désactivé") + "__ !")
                        .setTitle("JafarBot - Activity Tracker").build()).queue();
            }else {
                event.replyEmbeds(EmbedUtils.defaultError("La détection de l'activité déjà " + (currentlyEnabled ? "activé" : "désactivé") + " !")
                        .setTitle("JafarBot - Activity Tracker").build()).queue();
            }
        }
    }

    @NotNull
    @Override
    public SlashCommandData createCommandData() {
        return super.createCommandData().addOptions(
                new OptionData(OptionType.BOOLEAN, "enabled", "Définie si le détecteur d'activité doit être activé"),
                new OptionData(OptionType.BOOLEAN, "refresh", "Définie si les inactifs doivent être mis à jour lors de l'exécution de la commande")
        );
    }
}
