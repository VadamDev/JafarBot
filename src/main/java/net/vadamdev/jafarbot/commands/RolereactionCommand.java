package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
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
import java.util.HashSet;
import java.util.Set;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class RolereactionCommand extends Command implements ISlashCommand {
    public RolereactionCommand() {
        super("rolereaction");
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        GuildMessageChannel messageChannel = null;
        String id = null;

        if(commandData.getType().equals(ICommandData.Type.SLASH)) {
            SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

            messageChannel = event.getChannel().asGuildMessageChannel();
            id = event.getOption("id").getAsString();

            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Rolereaction - Succès")
                    .setDescription("Le rolereaction a été créé !")
                    .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
                    .setColor(Color.ORANGE).build()).setEphemeral(true).queue();
        }else if(commandData.getType().equals(ICommandData.Type.TEXT)) {
            TextCommandData textCommandData = (TextCommandData) commandData;

            String[] args = textCommandData.getArgs();
            Message message = textCommandData.getEvent().getMessage();

            if(args.length != 1 || !Main.jafarBot.getRoleReactionManager().getRoleReactions().contains(args[0])) {
                message.replyEmbeds(new EmbedBuilder()
                        .setTitle("Rolereaction - Erreur")
                        .setDescription("Une erreur est survenue.\nUtilisez la commande ``!rolereaction <id>``")
                        .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
                        .setColor(Color.RED).build()).queue();
                return;
            }

            messageChannel = message.getChannel().asGuildMessageChannel();
            id = args[0];

            message.delete().queue();
        }

        GuildMessageChannel finalMessageChannel = messageChannel;
        Main.jafarBot.getRoleReactionManager().findRoleReactionByName(id)
                .ifPresent(roleReaction -> roleReaction.sendReactionMessage(finalMessageChannel));
    }

    @Override
    public @Nonnull SlashCommandData createSlashCommand() {
        Set<Choice> choices = new HashSet<>();
        Main.jafarBot.getRoleReactionManager().getRoleReactions()
                .forEach(roleReaction -> choices.add(new Choice(roleReaction, roleReaction)));

        return Commands.slash(name, "Créé un role reaction dans le salon ou la commande est envoyé")
                .addOptions(
                        new OptionData(OptionType.STRING, "id", "ID du Rolereaction")
                                .addChoices(choices)
                                .setRequired(true)
                );
    }
}
