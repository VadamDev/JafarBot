package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.captaincy.BoatType;
import net.vadamdev.jafarbot.captaincy.CaptainedBoat;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class BoatOptionsCommand extends Command implements ISlashCommand {
    private final MessageEmbed SUCCESS_MESSAGE = new EmbedBuilder()
            .setTitle("Bateaux Capitainé")
            .setDescription("Ce paramètre a été mis a jour !")
            .setColor(Color.ORANGE)
            .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build();

    public BoatOptionsCommand() {
        super("boat-options");
    }

    @Override
    public void execute(@NotNull Member sender, @NotNull ICommandData commandData) {
        final SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

        final CaptainedBoat captainedBoat = Main.jafarBot.getProfileManager().getProfile(event.getMember().getId()).getCaptainedBoat();
        if(captainedBoat == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Bateaux Capitainé")
                    .setDescription("Vous ne possédez par de bateau capitainé !")
                    .setColor(Color.RED)
                    .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();

            return;
        }

        switch(event.getSubcommandName()) {
            case "defaultlocked":
                captainedBoat.setDefaultLocked(event.getOption("boolean").getAsBoolean());
                event.replyEmbeds(SUCCESS_MESSAGE).queue();
                break;
            case "preferredboattype":
                captainedBoat.setPreferredBoatType(BoatType.valueOf(event.getOption("type").getAsString()));
                event.replyEmbeds(SUCCESS_MESSAGE).queue();
                break;
            default:
                break;
        }
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Permet de modifier certains paramètres de votre bateau capitainée")
                .addSubcommands(
                        new SubcommandData("defaultlocked", "Permet de définir si le salon de votre bateau sera limité par défaut")
                                .addOptions(
                                        new OptionData(OptionType.BOOLEAN, "boolean", "value")
                                                .setRequired(true)
                                ),
                        new SubcommandData("preferredboattype", "Permet de définir le type de bateau préféré lors de la création de son salon")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "type", "Type du bateau")
                                                .addChoice(BoatType.SLOOP.getDisplayName(), BoatType.SLOOP.name())
                                                .addChoice(BoatType.BRIGANTINE.getDisplayName(), BoatType.BRIGANTINE.name())
                                                .addChoice(BoatType.GALLEON.getDisplayName(), BoatType.GALLEON.name())
                                                .setRequired(true)
                                )
                );
    }
}
