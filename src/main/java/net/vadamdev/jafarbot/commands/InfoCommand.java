package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.vadamdev.dbk.framework.commands.annotations.AnnotationProcessor;
import net.vadamdev.dbk.framework.commands.annotations.CommandProcessor;
import net.vadamdev.jafarbot.commands.api.GuildLinkedCommand;
import net.vadamdev.jafarbot.configs.MainConfig;
import net.vadamdev.jafarbot.profile.Profile;
import net.vadamdev.jafarbot.profile.ProfileManager;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Comparator;

/**
 * @author VadamDev
 */
public class InfoCommand extends GuildLinkedCommand {
    private final MainConfig mainConfig;
    private final ProfileManager profileManager;

    public InfoCommand(MainConfig mainConfig, ProfileManager profileManager) {
        super("info", "Permet de récupérer des informations sur les membres du discord");
        setRequiredPermissions(Permission.MESSAGE_MANAGE);

        this.mainConfig = mainConfig;
        this.profileManager = profileManager;
    }

    @CommandProcessor(subCommand = "get")
    private void getSubCommand(SlashCommandInteractionEvent event) {
        event.replyEmbeds(getUserInfos(event.getOption("target", OptionMapping::getAsMember))).queue();
    }

    @CommandProcessor(subCommand = "top")
    private void topSubCommand(SlashCommandInteractionEvent event) {
        final int limit = Math.clamp(event.getOption("limit", 15, OptionMapping::getAsInt), 5, 50);
        final boolean showFriends = event.getOption("showfriends", false, OptionMapping::getAsBoolean);
        final boolean showRetired = event.getOption("showretired", false, OptionMapping::getAsBoolean);

        event.replyEmbeds(createUserTop(event.getGuild(), limit, showFriends, showRetired)).queue();
    }

    private MessageEmbed getUserInfos(Member member) {
        final StringBuilder description = new StringBuilder("Activité Récente:\n");
        boolean flag = false; // Wtf is this flag for?

        final Profile profile = profileManager.getOrCreateProfile(member.getId());
        if(profile.isInVC()) {
            description.append("- Le <t:" + profile.getConnectionTime() / 1000 + ":f> *actuellement en vocal*\n \n");
            flag = true;
        }else if(Utils.hasRole(member, mainConfig.INACTIVE_ROLE)) {
            description.append("**Actuellement inactif**\n \n");
            flag = true;
        }

        for(long[] activityData : profile.getActivityData()) {
            final long loginTime = activityData[0];
            final long logoutTime = activityData[1];

            if(loginTime == 0 || logoutTime == 0)
                continue;

            description.append("- Le <t:" + loginTime / 1000 + ":f> pendant " + Utils.formatMsToHMS(logoutTime - loginTime) + "\n");

            if(!flag)
                flag = true;
        }

        if(!flag)
            description.append("Aucune");

        return EmbedUtils.defaultSuccess(description.toString())
                .setTitle("Informations de " + member.getEffectiveName())
                .setTimestamp(Instant.now()).build();
    }

    private MessageEmbed createUserTop(Guild guild, int limit, boolean showFriends, boolean showRetired) {
        final StringBuilder description = new StringBuilder();

        profileManager.getProfiles().stream()
                .filter(profile -> guild.getMemberById(profile.getUserId()) != null)
                .filter(profile -> {
                    final long[] activityData = profile.getActivityData()[0];
                    return activityData[0] != 0 && activityData[1] != 0;
                })
                .filter(profile -> {
                    //Seriously, this needs a rewrite
                    final Member member = guild.getMemberById(profile.getUserId());

                    final boolean hasFriendRole = Utils.hasRole(member, mainConfig.FRIEND_ROLE);
                    final boolean hasRetiredRole = Utils.hasRole(member, mainConfig.RETIRED_ROLE);

                    if((showFriends && hasFriendRole || (showRetired && hasRetiredRole)))
                        return true;

                    return !(hasFriendRole && hasRetiredRole);
                })
                .sorted(Comparator.comparingLong(Profile::getLastActivity))
                .limit(limit)
                .forEach(profile -> {
                    final Member member = guild.getMemberById(profile.getUserId());
                    description.append("- " + member.getAsMention() + (Utils.hasRole(member, mainConfig.INACTIVE_ROLE) ? " (\uD83D\uDCA4)" : "") + " (<t:" + profile.getLastActivity() / 1000 + ":R>)\n");
                });

        return EmbedUtils.defaultSuccess(description.toString()).setTitle("TOP des Inactifs").setTimestamp(Instant.now()).build();
    }

    @NotNull
    @Override
    public SlashCommandData createCommandData() {
        return super.createCommandData().addSubcommands(
                new SubcommandData("get", "Permet de connaitre l'activité d'un membre du discord").addOptions(
                        new OptionData(OptionType.USER, "target", "Target", true)
                ),

                new SubcommandData("top", "Renvois un leaderboard des personnes les plus inactives du discord").addOptions(
                        new OptionData(OptionType.INTEGER, "limit", "Définie la taille maximale du TOP")
                                .setRequiredRange(5, 50),
                        new OptionData(OptionType.BOOLEAN, "showfriends", "Mettez la valeur sur TRUE si vous souhaitez que même les ADJ soit compté dans le leaderboard"),
                        new OptionData(OptionType.BOOLEAN, "showretired", "Mettez la valeur sur TRUE si vous souhaitez que même les RDLL soit compté dans le leaderboard")
                )
        );
    }

    @Override
    public void executeCommand(Member member, SlashCommandInteractionEvent event) {
        AnnotationProcessor.processAnnotations(event, this);
    }
}
