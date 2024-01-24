package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.activity.ActivityTracker;
import net.vadamdev.jafarbot.profile.Profile;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import net.vadamdev.jdautils.commands.data.SlashCmdData;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class InfoCommand extends Command implements ISlashCommand {
    public InfoCommand() {
        super("info");
        setPermission(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        final SlashCommandInteractionEvent event = commandData.castOrNull(SlashCmdData.class).getEvent();

        switch(event.getSubcommandName()) {
            case "get":
                event.replyEmbeds(getUserInfos(event.getOption("target", OptionMapping::getAsMember))).queue();

                break;
            case "top":
                final int limit = event.getOption("limit", 15, OptionMapping::getAsInt);
                final boolean showFriends = event.getOption("showfriends", false, OptionMapping::getAsBoolean);
                final boolean showRetired = event.getOption("showretired", false, OptionMapping::getAsBoolean);

                event.replyEmbeds(createUserTop(event.getGuild(), limit, showFriends, showRetired)).queue();

                break;
            default:
                break;
        }
    }

    private MessageEmbed getUserInfos(Member member) {
        final StringBuilder description = new StringBuilder("Activité Récente:\n");
        boolean flag = false;

        final Profile profile = Main.jafarBot.getProfileManager().getProfile(member.getId());
        if(profile.isInVC()) {
            description.append("- Le <t:" + profile.getConnectionTime() / 1000 + ":f> *actuellement en vocal*\n \n");
            flag = true;
        }else if(ActivityTracker.hasInactiveRole(member, Main.jafarBot.mainConfig.INACTIVE_ROLE)) {
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

        return new JafarEmbed()
                .setTitle("Informations de " + member.getEffectiveName())
                .setDescription(description.toString())
                .setColor(JafarEmbed.NEUTRAL_COLOR)
                .setTimestamp(Instant.now()).build();
    }

    private MessageEmbed createUserTop(Guild guild, int limit, boolean showFriends, boolean showRetired) {
        final StringBuilder description = new StringBuilder();

        Main.jafarBot.getProfileManager().getProfiles().stream()
                .filter(profile -> guild.getMemberById(profile.getUserId()) != null)
                .filter(profile -> {
                    final long[] activityData = profile.getActivityData()[0];
                    return activityData[0] != 0 && activityData[1] != 0;
                })
                .filter(profile -> {
                    final List<Role> roles = guild.getMemberById(profile.getUserId()).getRoles();

                    if((showFriends && roles.stream().anyMatch(role -> role.getId().equals(Main.jafarBot.mainConfig.FRIEND_ROLE)) || (showRetired && roles.stream().anyMatch(role -> role.getId().equals(Main.jafarBot.mainConfig.RETIRED_ROLE)))))
                        return true;

                    return roles.stream().noneMatch(role -> role.getId().equals(Main.jafarBot.mainConfig.FRIEND_ROLE) || role.getId().equals(Main.jafarBot.mainConfig.RETIRED_ROLE));
                })
                .sorted(Comparator.comparingLong(Profile::getLastActivity))
                .limit(limit)
                .forEach(profile -> {
                    Member member = guild.getMemberById(profile.getUserId());
                    description.append("- " + member.getAsMention() + (ActivityTracker.hasInactiveRole(member, Main.jafarBot.mainConfig.INACTIVE_ROLE) ? " (\uD83D\uDCA4)" : "") + " (<t:" + profile.getLastActivity() / 1000 + ":R>)\n");
                });

        return new JafarEmbed()
                .setTitle("TOP des Inactifs")
                .setDescription(description.toString())
                .setColor(JafarEmbed.NEUTRAL_COLOR)
                .setTimestamp(Instant.now()).build();
    }

    @Nonnull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Commande destiné à récupéré des informations sur les membres du discord")
                .addSubcommands(
                        new SubcommandData("get", "Permet de connaitre l'activité d'un membre du discord")
                                .addOptions(
                                        new OptionData(OptionType.USER, "target", "Cible")
                                                .setRequired(true)
                                ),
                        new SubcommandData("top", "Renvois un leaderboard des personnes les plus inactives du discord")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "limit", "Définie la taille maximale du TOP")
                                                .setMinValue(5)
                                                .setMaxValue(50),
                                        new OptionData(OptionType.BOOLEAN, "showfriends", "Mettez la valeur sur TRUE si vous souhaitez que même les ADJ soit compté dans le leaderboard"),
                                        new OptionData(OptionType.BOOLEAN, "showretired", "Mettez la valeur sur TRUE si vous souhaitez que même les RDLL soit compté dans le leaderboard")
                                )
                );
    }

    private String formatLastActivity(long lastActivityTime) {
        return String.format("%dh %dm %ds", (lastActivityTime / 3600000) % 24, (lastActivityTime / 60000) % 60, (lastActivityTime / 1000) % 60);
    }
}
