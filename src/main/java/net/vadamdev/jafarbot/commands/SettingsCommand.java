package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
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
import net.vadamdev.jdautils.configuration.ConfigValue;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

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

    public SettingsCommand() {
        super("settings");
        setPermission(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        MainConfig mainConfig = Main.jafarBot.mainConfig;

        String fieldName = null, value = null;

        if(commandData.getType().equals(ICommandData.Type.SLASH)) {
            SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

            fieldName = event.getOption("fieldname").getAsString();
            value = event.getOption("value").getAsString();

            event.replyEmbeds(SUCCESS_MESSAGE).queue();
        }else if(commandData.getType().equals(ICommandData.Type.TEXT)) {
            TextCommandData textCommandData = (TextCommandData) commandData;

            String[] args = textCommandData.getArgs();
            Message message = textCommandData.getEvent().getMessage();

            if(!mainConfig.hasField(args[0]) || args.length < 2) {
                message.replyEmbeds(new EmbedBuilder()
                        .setTitle("Paramètres - Erreur")
                        .setDescription("Une erreur est survenue.\nUtilisez la commande ``!settings <fieldName> <value>``")
                        .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
                        .setColor(Color.RED).build()).queue();
                return;
            }

            StringBuilder builder = new StringBuilder();
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

    @Override
    public @Nonnull SlashCommandData createSlashCommand() {
        Set<Choice> choices = new HashSet<>();
        for (Field field : Main.jafarBot.mainConfig.getClass().getFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                if(annotation instanceof ConfigValue) {
                    choices.add(new Choice(field.getName(), field.getName()));
                    break;
                }
            }
        }

        return Commands.slash(name, "Change la valeur de n'importe quel variable dans la config")
                .addOptions(
                        new OptionData(OptionType.STRING, "fieldname", "Nom de la variable")
                                .addChoices(choices)
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "value", "Nouvelle valeur")
                                .setRequired(true)
                );
    }
}
