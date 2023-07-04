package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
import net.vadamdev.jdautils.commands.data.impl.TextCommandData;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class ClearCommand extends Command implements ISlashCommand {
    private final MessageEmbed ERROR_MESSAGE = new EmbedBuilder()
            .setTitle("Clear - Erreur")
            .setDescription("Une erreur est survenue.\nUtilisez la commande ``!clear <amount>``")
            .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
            .setColor(Color.RED).build();

    public ClearCommand() {
        super("clear");
        setPermission(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        MessageChannel textChannel = null;
        int amount = 0;

        if(commandData.getType().equals(ICommandData.Type.SLASH)) {
            SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

            textChannel = event.getChannel();
            amount = event.getOption("amount").getAsInt() + 1;

            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Clear")
                    .setDescription((amount - 1) + " messages ont été supprimer !")
                    .setColor(Color.ORANGE)
                    .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).setEphemeral(true).queue();
        }else if(commandData.getType().equals(ICommandData.Type.TEXT)) {
            TextCommandData textCommandData = (TextCommandData) commandData;

            String[] args = textCommandData.getArgs();
            if(args.length == 1) {
                Message message = textCommandData.getEvent().getMessage();

                int toRemove;

                try {
                    toRemove = Integer.parseInt(args[0]);

                    if(toRemove == 0 || toRemove > 99) {
                        message.replyEmbeds(ERROR_MESSAGE).queue();
                        return;
                    }

                    toRemove += 1;
                }catch(Exception ignored) {
                    message.replyEmbeds(ERROR_MESSAGE).queue();
                    return;
                }

                textChannel = message.getChannel();
                amount = toRemove;

                message.replyEmbeds(new EmbedBuilder()
                        .setTitle("Clear")
                        .setDescription((toRemove - 1) + " messages ont été supprimer ! (Auto Delete: <t:" + (System.currentTimeMillis() / 1000 + 5) + ":R>)")
                        .setColor(Color.ORANGE)
                        .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build())
                        .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            }
        }

        textChannel.purgeMessages(textChannel.getHistory().retrievePast(amount).complete());
    }

    @Override
    public @Nonnull SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Supprime un nombre donnée de messages dans le salon ou la commande est éxécuté")
                .addOptions(
                        new OptionData(OptionType.INTEGER, "amount", "Nombre de messages à supprimer")
                                .setMinValue(1)
                                .setMaxValue(99)
                                .setRequired(true)
                );
    }
}
