package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.vadamdev.dbk.framework.commands.annotations.AnnotationProcessor;
import net.vadamdev.dbk.framework.commands.annotations.CommandProcessor;
import net.vadamdev.jafarbot.captaincy.BoatType;
import net.vadamdev.jafarbot.commands.api.GuildLinkedCommand;
import net.vadamdev.jafarbot.profile.CaptainedFleet;
import net.vadamdev.jafarbot.profile.Profile;
import net.vadamdev.jafarbot.profile.ProfileManager;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VadamDev
 * @since 14/04/2025
 */
public class BoatsCommand extends GuildLinkedCommand {
    private final ProfileManager profileManager;

    public BoatsCommand(ProfileManager profileManager) {
        super("boat", "Commande dédié à la gestion des bateaux capitainé");

        this.profileManager = profileManager;
    }

    @CommandProcessor(subCommand = "create")
    private void create(SlashCommandInteractionEvent event) {
        final Member target = parseTarget(event.getMember(), event.getOption("target"), event);
        if(target == null)
            return;

        final BoatType type = event.getOption("type", mapping -> {
            try {
                return BoatType.valueOf(mapping.getAsString());
            }catch (Exception ignored) {
                return BoatType.GALLEON;
            }
        });

        final String name = event.getOption("name", mapping ->  {
            final String input = mapping.getAsString();

            if(input.length() > 16)
                return input.substring(0, 16);

            return input;
        });

        final String symbol = event.getOption("symbol", "❔", mapping -> {
            final String input = mapping.getAsString();

            if(input.length() > 12)
                return input.substring(0, 12);

            if(Utils.isEmoji(input))
                return input;

            return "❔";
        });

        final String formattedName = symbol + "┃" + name;

        final Profile targetProfile = profileManager.getOrCreateProfile(target.getId());
        CaptainedFleet fleet = targetProfile.getCaptainedFleet();
        if(fleet == null) {
            fleet = new CaptainedFleet();
            targetProfile.setCaptainedFleet(fleet);
        }

        fleet.setName(type, formattedName);

        event.replyEmbeds(EmbedUtils.defaultSuccess("Le " + type.getDisplayName() + " de " + target.getAsMention() + " a été mis à jour ! (``" + formattedName + "``)")
                .setTitle("JafarBot - Bateaux Capitainé").build()).queue();
    }

    @CommandProcessor(subCommand = "delete")
    private void delete(SlashCommandInteractionEvent event) {
        final Member target = parseTarget(event.getMember(), event.getOption("target"), event);
        if(target == null)
            return;

        final BoatType type = event.getOption("type", null, mapping -> {
            try {
                return BoatType.valueOf(mapping.getAsString());
            }catch (Exception ignored) {
                return null;
            }
        });

        if(type == null)
            return; //Should not happen but if I add a fallback, it will naively delete it

        final Profile targetProfile = profileManager.getOrCreateProfile(target.getId());

        final CaptainedFleet fleet = targetProfile.getCaptainedFleet();
        if(fleet != null) {
            final String currentName = fleet.getNameByBoatType(type);

            if(currentName != null) {
                fleet.setName(type, null);

                if(fleet.isEmpty())
                    targetProfile.setCaptainedFleet(null); //Unfortunately, this will delete the preferred boat type

                event.replyEmbeds(EmbedUtils.defaultSuccess("Le " + type.getDisplayName() + " de " + target.getAsMention() + " a été supprimer !")
                        .setTitle("JafarBot - Bateaux Capitainé").build()).queue();
            }else
                event.replyEmbeds(EmbedUtils.defaultError(target.getAsMention() + " n'a pas de " + type.getDisplayName() + " !")
                        .setTitle("JafarBot - Bateaux Capitainé").build()).queue();
        }else
            event.replyEmbeds(EmbedUtils.defaultError(target.getAsMention() + " n'a aucun bateau !")
                    .setTitle("JafarBot - Bateaux Capitainé").build()).queue();
    }

    @CommandProcessor(subCommand = "query")
    private void query(SlashCommandInteractionEvent event) {
        final Member target = parseTarget(event.getMember(), event.getOption("target"), event);
        if(target == null)
            return;

        final Profile targetProfile = profileManager.getOrCreateProfile(target.getId());

        final CaptainedFleet fleet = targetProfile.getCaptainedFleet();
        if(fleet != null) {
            final StringBuilder boats = new StringBuilder();
            for(BoatType type : BoatType.values()) {
                final String name = fleet.getNameByBoatType(type);
                if(name == null)
                    continue;

                boats.append("> ``").append(name).append("``\n");
            }

            final BoatType preferredBoatType = fleet.getPreferredBoatType();

            String preferredBoatName = fleet.getNameByBoatType(preferredBoatType);
            if(preferredBoatName == null)
                preferredBoatName = preferredBoatType.getDisplayName();

            event.replyEmbeds(EmbedUtils.defaultEmbed(EmbedUtils.SUCCESS_COLOR)
                    .setTitle("JafarBot - Bateaux Capitainé")
                    .addField("Bateaux", boats.toString(), true)
                    .addField("Bateau préféré", "> ``" + preferredBoatName + "``", true)
                    .addField("Verrouiller par default", "> ``" + Utils.formatBoolean(fleet.isDefaultLocked()) + "``", true).build()).queue();
        }else
            event.replyEmbeds(EmbedUtils.defaultError(target.getAsMention() + " n'a aucun bateau !")
                    .setTitle("JafarBot - Bateaux Capitainé").build()).queue();
    }

    @Nullable
    private Member parseTarget(Member commandSender, @Nullable OptionMapping targetOption, SlashCommandInteractionEvent event) {
        if(targetOption == null)
            return commandSender;

        if(commandSender.hasPermission(Permission.MESSAGE_MANAGE))
            return targetOption.getAsMember();
        else {
            event.replyEmbeds(EmbedUtils.defaultError("Vous n'avez pas la permission requise pour modifier le bateau d'autres personnes !").build())
                    .setEphemeral(true).queue();

            return null;
        }
    }

    @NotNull
    @Override
    public SlashCommandData createCommandData() {
        return super.createCommandData().addSubcommands(
                new SubcommandData("create", "Permet de créé un nouveau bateau ou d'en modifier un déjà éxistant").addOptions(
                        new OptionData(OptionType.STRING, "type", "Type de bateau", true)
                                .addChoices(BoatType.asChoicesList()),
                        new OptionData(OptionType.STRING, "name", "Nom du bateau", true)
                                .setRequiredLength(1, 16),
                        new OptionData(OptionType.STRING, "symbol", "Symbole du bateau (Doit être un emoji unicode)")
                                .setRequiredLength(6, 12),
                        new OptionData(OptionType.USER, "target", "Cible")
                ),

                new SubcommandData("delete", "Supprime un bateau suivant le type fournis").addOptions(
                        new OptionData(OptionType.STRING, "type", "Type de bateau", true)
                                .addChoices(BoatType.asChoicesList()),
                        new OptionData(OptionType.USER, "target", "Cible")
                ),

                new SubcommandData("query", "Affiche la liste des bateau de la cible (ou vous même)").addOptions(
                        new OptionData(OptionType.USER, "target", "Cible")
                )
        );
    }

    @Override
    public void executeCommand(Member sender, SlashCommandInteractionEvent event) {
        AnnotationProcessor.processAnnotations(event, this);
    }
}
