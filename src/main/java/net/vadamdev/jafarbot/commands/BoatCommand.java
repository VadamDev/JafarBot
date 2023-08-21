package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.captaincy.BoatType;
import net.vadamdev.jafarbot.captaincy.CaptainedBoat;
import net.vadamdev.jafarbot.profile.Profile;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;

import javax.annotation.Nonnull;
import java.awt.*;

public class BoatCommand extends Command implements ISlashCommand {
    public BoatCommand() {
        super("boat");
        setPermission(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        final SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

        final User target = event.getOption("target").getAsUser();

        switch(event.getSubcommandName()) {
            case "create":
                final BoatType boatType = BoatType.valueOf(event.getOption("type").getAsString());
                final String formattedName = event.getOption("emoji", "❔", OptionMapping::getAsString) + "┃" + event.getOption("name").getAsString();

                CaptainedBoat captainedBoat = Main.jafarBot.getCaptainedBoatManager().getCaptainedBoatByUser(target.getId());
                captainedBoat.setName(boatType, formattedName);

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Bateaux Capitainé")
                        .setDescription("Le " + boatType.getDisplayName() + " de " + target.getAsMention() + " a été mis à jour ! (``" + formattedName + "``)")
                        .setColor(Color.ORANGE)
                        .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();

                break;
            case "delete":
                final BoatType formattedBoatType = event.getOption("type", null, optionMapping -> BoatType.valueOf(optionMapping.getAsString()));

                final Profile profile = Main.jafarBot.getProfileManager().getProfile(target.getId());
                if(profile.getCaptainedBoat() != null) {
                    if(formattedBoatType != null) {
                        profile.getCaptainedBoat().setName(formattedBoatType, null);

                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("Bateaux Capitainé")
                                .setDescription("Le nom du " + formattedBoatType.name() + " de " + target.getAsMention() + " a été supprimé !")
                                .setColor(Color.ORANGE)
                                .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();
                    }else {
                        profile.setCaptainedBoat(null);

                        event.replyEmbeds(new EmbedBuilder()
                                .setTitle("Bateaux Capitainé")
                                .setDescription("Le bateau de " + target.getAsMention() + " a été supprimé !")
                                .setColor(Color.ORANGE)
                                .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();
                    }
                }else
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Bateaux Capitainé")
                            .setDescription("Le bateau de " + target.getAsMention() + " n'existe pas !")
                            .setColor(Color.RED)
                            .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();

                break;
            default:
                break;
        }
    }

    @Override
    public @Nonnull SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Commande dédié à la gestion des bateaux capitainé")
                .addSubcommands(
                        new SubcommandData("create", "Créé un nouveau bateau")
                                .addOptions(
                                        new OptionData(OptionType.USER, "target", "Cible")
                                                .setRequired(true),
                                        new OptionData(OptionType.STRING, "type", "Type du bateau")
                                                .addChoice(BoatType.SLOOP.getDisplayName(), BoatType.SLOOP.name())
                                                .addChoice(BoatType.BRIGANTINE.getDisplayName(), BoatType.BRIGANTINE.name())
                                                .addChoice(BoatType.GALLEON.getDisplayName(), BoatType.GALLEON.name())
                                                .setRequired(true),
                                        new OptionData(OptionType.STRING, "name", "Nom du bateau")
                                                .setRequired(true),
                                        new OptionData(OptionType.STRING, "emoji", "Symbole du bateau")
                                                .setMinLength(1)
                                                .setMaxLength(16)
                                ),
                        new SubcommandData("delete", "Supprime le bateau de la cible, ou supprime le nom du type du bateau de la cible")
                                .addOptions(
                                        new OptionData(OptionType.USER, "target", "Cible")
                                                .setRequired(true),
                                        new OptionData(OptionType.STRING, "type", "Type du bateau")
                                                .addChoice("Sloop", BoatType.SLOOP.name())
                                                .addChoice("Brigantin", BoatType.BRIGANTINE.name())
                                                .addChoice("Gallion", BoatType.GALLEON.name())
                                )
                );
    }
}
