package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
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

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class ClearCommand extends Command implements ISlashCommand {
    public ClearCommand() {
        super("clear");
        setPermission(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        final SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

        final MessageChannel textChannel = event.getChannel();
        final int amount = event.getOption("amount").getAsInt();

        textChannel.purgeMessages(textChannel.getHistory().retrievePast(amount).complete());

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("Clear")
                .setDescription(amount + " messages ont été supprimer !")
                .setColor(Color.ORANGE)
                .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).setEphemeral(true).queue();
    }

    @Override
    public @Nonnull SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Supprime un nombre donnée de messages dans le salon ou la commande est éxécuté")
                .addOptions(
                        new OptionData(OptionType.INTEGER, "amount", "Nombre de messages à supprimer")
                                .setMinValue(1)
                                .setMaxValue(100)
                                .setRequired(true)
                );
    }
}
