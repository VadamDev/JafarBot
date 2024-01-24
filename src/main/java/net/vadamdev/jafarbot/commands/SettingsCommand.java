package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.config.MainConfig;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import net.vadamdev.jdautils.commands.data.SlashCmdData;
import net.vadamdev.jdautils.commands.data.TextCmdData;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class SettingsCommand extends Command implements ISlashCommand {
    private static final MessageEmbed SUCCESS_MESSAGE = new JafarEmbed()
            .setTitle("JafarBot - Paramètres")
            .setDescription("Vos modifications ont été enregistrer avec succès !")
            .setColor(JafarEmbed.SUCCESS_COLOR).build();

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
            final SlashCommandInteractionEvent event = commandData.castOrNull(SlashCmdData.class).getEvent();

            fieldName = event.getOption("field", OptionMapping::getAsString);
            value = event.getOption("value", OptionMapping::getAsString);

            event.replyEmbeds(SUCCESS_MESSAGE).queue();
        }else if(commandData.getType().equals(ICommandData.Type.TEXT)) {
            final TextCmdData textData = commandData.castOrNull(TextCmdData.class);

            final String[] args = textData.getArgs();
            final Message message = textData.getEvent().getMessage();

            if(args.length < 2 || !mainConfig.hasField(args[0])) {
                message.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Paramètres")
                        .setDescription("Une erreur est survenue.\nUtilisez la commande ``!settings <fieldName> <value>``")
                        .setColor(JafarEmbed.ERROR_COLOR).build()).queue();

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
        return Commands.slash(name, "Change la valeur d'une variable dans la config du bot")
                .addOptions(
                        new OptionData(OptionType.STRING, "field", "Nom de la variable")
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
