package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.activity.ActivityTracker;
import net.vadamdev.jafarbot.profile.Profile;
import net.vadamdev.jafarbot.utils.Utils;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import net.vadamdev.jdautils.commands.data.impl.TextCommandData;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.util.Comparator;

public class InfoCommand extends Command implements ISlashCommand {
    private final MessageEmbed ERROR_MESSAGE = new EmbedBuilder()
            .setTitle("Informations - Erreur")
            .setDescription("Une erreur est survenue.\nUtilisez la commande ``!info <member>``")
            .setFooter("JafarBot", Main.jafarBot.getAvatarURL())
            .setColor(Color.RED).build();

    public InfoCommand() {
        super("info");
        setPermission(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        if(commandData.getType().equals(ICommandData.Type.SLASH)) {
            SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

            final String subCommand = event.getSubcommandName();
            if(subCommand == null)
                return;

            if(subCommand.equals("top")) {
                event.replyEmbeds(createUserTop(event.getGuild(), event.getOption("everyone", false, OptionMapping::getAsBoolean))).queue();
            }else if(subCommand.equals("get")) {
                Member target = event.getOption("target").getAsMember();
                event.replyEmbeds(getUserInfos(target)).queue();
            }
        }else if(commandData.getType().equals(ICommandData.Type.TEXT)) {
            TextCommandData textCommandData = ((TextCommandData) commandData);

            String[] args = textCommandData.getArgs();
            MessageReceivedEvent event = textCommandData.getEvent();

            Message message = event.getMessage();

            if(args.length != 1) {
                message.replyEmbeds(ERROR_MESSAGE).queue();
                return;
            }

            if(args[0].equalsIgnoreCase("top"))
                message.replyEmbeds(createUserTop(event.getGuild(), false)).queue();
            else {
                Member target = Utils.formatTarget(args[0], message.getMentions(), event.getGuild());
                if(target == null) {
                    message.replyEmbeds(ERROR_MESSAGE).queue();
                    return;
                }

                message.replyEmbeds(getUserInfos(target)).queue();
            }
        }
    }

    private MessageEmbed getUserInfos(Member member) {
        StringBuilder description = new StringBuilder("Activité Récente:\n");
        boolean flag = false;

        Profile profile = Main.jafarBot.getProfileManager().getProfile(member.getId());
        if(profile.isInVocal()) {
            description.append("- Le <t:" + profile.getConnectionTime() / 1000 + ":f> *actuellement en vocal*\n \n");
            flag = true;
        }else if(ActivityTracker.hasInactiveRole(member, Main.jafarBot.mainConfig.STUCKED_ROLE)) {
            description.append("**Actuellement inactif**\n \n");
            flag = true;
        }

        long[][] activityData = profile.getActivityData();
        for (long[] activityDatum : activityData) {
            final long connectionTime = activityDatum[0];
            final long deconnectionTime = activityDatum[1];

            if(connectionTime == 0 || deconnectionTime == 0)
                continue;

            description.append("- Le <t:" + connectionTime / 1000 + ":f> pendant " + formatLastActivity(deconnectionTime - connectionTime) + "\n");

            if(!flag)
                flag = true;
        }

        if(!flag)
            description.append("Aucune");

        return new EmbedBuilder()
                .setTitle("Informations de " + member.getEffectiveName())
                .setDescription(description.toString())
                .setColor(Color.ORANGE)
                .setTimestamp(Instant.now())
                .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build();
    }

    private MessageEmbed createUserTop(Guild guild, boolean everyone) {
        StringBuilder description = new StringBuilder();

        Main.jafarBot.getProfileManager().getProfiles().stream()
                .filter(profile -> guild.getMemberById(profile.getUserId()) != null)
                .filter(profile -> {
                    final long[] activityData = profile.getActivityData()[0];
                    return activityData[0] != 0 && activityData[1] != 0;
                })
                .filter(profile -> everyone || guild.getMemberById(profile.getUserId()).getRoles().stream().noneMatch(role -> role.getId().equals(Main.jafarBot.mainConfig.FRIEND_ROLE)))
                .sorted(Comparator.comparingLong(Profile::getLastActivity))
                .limit(15)
                .forEach(profile -> {
                    Member member = guild.getMemberById(profile.getUserId());
                    description.append("- " + member.getAsMention() + (ActivityTracker.hasInactiveRole(member, Main.jafarBot.mainConfig.STUCKED_ROLE) ? " (\uD83D\uDCA4)" : "") + " (<t:" + profile.getLastActivity() / 1000 + ":R>)\n");
                });

        return new EmbedBuilder()
                .setTitle("TOP des Inactifs")
                .setDescription(description.toString())
                .setColor(Color.ORANGE)
                .setTimestamp(Instant.now())
                .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build();
    }

    @Override
    public @Nonnull SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Commande destiné à récupéré des informations sur les membres du discord")
                .addSubcommands(
                        new SubcommandData("get", "Permet de connaitre l'activité d'un membre du discord")
                                .addOptions(
                                        new OptionData(OptionType.USER, "target", "Cible")
                                                .setRequired(true)
                                ),
                        new SubcommandData("top", "Renvois un leaderboard des personnes les plus inactives du discord")
                                .addOptions(
                                        new OptionData(OptionType.BOOLEAN, "everyone", "Mettez la valeur sur TRUE si vous souhaitez que même les ADJ soit compté dans le leaderboard")
                                )
                );
    }

    private String formatLastActivity(long lastActivityTime) {
        return String.format("%dh %dm %ds", (lastActivityTime / 3600000) % 24, (lastActivityTime / 60000) % 60, (lastActivityTime / 1000) % 60);
    }
}
