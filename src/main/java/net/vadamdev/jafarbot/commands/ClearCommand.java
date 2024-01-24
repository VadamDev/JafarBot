package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import net.vadamdev.jdautils.commands.data.SlashCmdData;

import javax.annotation.Nonnull;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class ClearCommand extends Command implements ISlashCommand {
    public ClearCommand() {
        super("clear");
        setPermission(Permission.ADMINISTRATOR);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        final SlashCommandInteractionEvent event = commandData.castOrNull(SlashCmdData.class).getEvent();

        final int amount = event.getOption("amount", OptionMapping::getAsInt);

        final MessageChannel textChannel = event.getChannel();
        textChannel.purgeMessages(textChannel.getHistory().retrievePast(amount).complete());

        event.replyEmbeds(new JafarEmbed()
                .setTitle("JafarBot - Clear")
                .setDescription(amount + " messages ont été supprimer !")
                .setColor(JafarEmbed.SUCCESS_COLOR).build()).setEphemeral(true).queue();
    }

    @Nonnull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Supprime un nombre de messages dans le salon ou la commande est éxecuté")
                .addOptions(
                        new OptionData(OptionType.INTEGER, "amount", "Nombre de messages à supprimer")
                                .setMinValue(1)
                                .setMaxValue(100)
                                .setRequired(true)
                );
    }
}
