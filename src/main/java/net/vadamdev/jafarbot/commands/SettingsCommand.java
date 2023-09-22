package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.config.MainConfig;
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
public class SettingsCommand extends Command implements ISlashCommand {
    private final MessageEmbed SUCCESS_MESSAGE = new EmbedBuilder()
            .setTitle("Paramètres")
            .setDescription("Vos modifications ont été enregistrer avec succès !")
            .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
            .setColor(Color.ORANGE).build();

    private final MainConfig mainConfig;

    public SettingsCommand() {
        super("settings");
        setPermission(Permission.ADMINISTRATOR);

        this.mainConfig = Main.jafarBot.mainConfig;
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        String fieldName = null, value = null;

        if(commandData.getType().equals(ICommandData.Type.SLASH)) {
            final SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

            fieldName = event.getOption("fieldname").getAsString();
            value = event.getOption("value").getAsString();

            event.replyEmbeds(SUCCESS_MESSAGE).queue();
        }else if(commandData.getType().equals(ICommandData.Type.TEXT)) {
            final TextCommandData textCommandData = (TextCommandData) commandData;

            final String[] args = textCommandData.getArgs();
            final Message message = textCommandData.getEvent().getMessage();

            if(args.length < 2 || !mainConfig.hasField(args[0])) {
                message.replyEmbeds(new EmbedBuilder()
                        .setTitle("Paramètres - Erreur")
                        .setDescription("Une erreur est survenue.\nUtilisez la commande ``!settings <fieldName> <value>``")
                        .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
                        .setColor(Color.RED).build()).queue();

                return;
            }

            final StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; i++)
                builder.append(args[i] + (i < args.length - 1 ? " " : ""));

            fieldName = args[0];
            value = builder.toString();

            message.replyEmbeds(SUCCESS_MESSAGE).queue();
        }

        try {
            mainConfig.setValue(fieldName, value);
            mainConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Change la valeur de n'importe quel variable dans la config")
                .addOptions(
                        new OptionData(OptionType.STRING, "fieldname", "Nom de la variable")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "value", "Nouvelle valeur")
                                .setRequired(true)
                );
    }

    @Override
    public boolean isSlashOnly() {
        return false;
    }
}
