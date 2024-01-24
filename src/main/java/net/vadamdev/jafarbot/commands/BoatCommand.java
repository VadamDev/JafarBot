package net.vadamdev.jafarbot.commands;

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
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import net.vadamdev.jdautils.commands.data.SlashCmdData;

import javax.annotation.Nonnull;

public class BoatCommand extends Command implements ISlashCommand {
    public BoatCommand() {
        super("boat");
        setPermission(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        final SlashCommandInteractionEvent event = commandData.castOrNull(SlashCmdData.class).getEvent();

        final User target = event.getOption("target", OptionMapping::getAsUser);

        switch(event.getSubcommandName()) {
            case "create":
                final BoatType boatType = BoatType.valueOf(event.getOption("type", OptionMapping::getAsString));
                final String formattedName = event.getOption("emoji", "❔", OptionMapping::getAsString) + "┃" + event.getOption("name", OptionMapping::getAsString);

                final CaptainedBoat boat = Main.jafarBot.getCaptainedBoatManager().getCaptainedBoatByUser(target.getId());
                boat.setName(boatType, formattedName);

                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Bateaux Capitainé")
                        .setDescription("Le " + boatType.getDisplayName() + " de " + target.getAsMention() + " a été mis à jour ! (``" + formattedName + "``)")
                        .setColor(JafarEmbed.SUCCESS_COLOR).build()).queue();

                break;
            case "delete":
                final BoatType formattedBoatType = BoatType.valueOf(event.getOption("type", null, OptionMapping::getAsString));

                final Profile profile = Main.jafarBot.getProfileManager().getProfile(target.getId());
                if(profile.getCaptainedBoat() != null) {
                    if(formattedBoatType != null) {
                        profile.getCaptainedBoat().setName(formattedBoatType, null);

                        event.replyEmbeds(new JafarEmbed()
                                .setTitle("JafarBot - Bateaux Capitainé")
                                .setDescription("Le nom du " + formattedBoatType.name() + " de " + target.getAsMention() + " a été supprimé !")
                                .setColor(JafarEmbed.SUCCESS_COLOR).build()).queue();
                    }else {
                        profile.setCaptainedBoat(null);

                        event.replyEmbeds(new JafarEmbed()
                                .setTitle("JafarBot - Bateaux Capitainé")
                                .setDescription("Le bateau de " + target.getAsMention() + " a été supprimé !")
                                .setColor(JafarEmbed.SUCCESS_COLOR).build()).queue();
                    }
                }else
                    event.replyEmbeds(new JafarEmbed()
                            .setTitle("JafarBot - Bateaux Capitainé")
                            .setDescription("Le bateau de " + target.getAsMention() + " n'existe pas !")
                            .setColor(JafarEmbed.ERROR_COLOR).build()).queue();

                break;
            default:
                break;
        }
    }

    @Nonnull
    @Override
    public SlashCommandData createSlashCommand() {
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
                                                .addChoice(BoatType.SLOOP.getDisplayName(), BoatType.SLOOP.name())
                                                .addChoice(BoatType.BRIGANTINE.getDisplayName(), BoatType.BRIGANTINE.name())
                                                .addChoice(BoatType.GALLEON.getDisplayName(), BoatType.GALLEON.name())
                                )
                );
    }
}
