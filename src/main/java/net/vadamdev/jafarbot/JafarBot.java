package net.vadamdev.jafarbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.vadamdev.jafarbot.activity.ActivityTracker;
import net.vadamdev.jafarbot.captaincy.CaptainedBoatManager;
import net.vadamdev.jafarbot.channelcreator.ChannelCreator;
import net.vadamdev.jafarbot.channelcreator.ChannelCreatorManager;
import net.vadamdev.jafarbot.commands.*;
import net.vadamdev.jafarbot.config.MainConfig;
import net.vadamdev.jafarbot.listeners.EventListener;
import net.vadamdev.jafarbot.profile.ProfileManager;
import net.vadamdev.jafarbot.rolereaction.RoleOption;
import net.vadamdev.jafarbot.rolereaction.RoleReaction;
import net.vadamdev.jafarbot.rolereaction.RoleReactionManager;
import net.vadamdev.jafarbot.utils.Utils;
import net.vadamdev.jdautils.application.IReloadable;
import net.vadamdev.jdautils.application.JDABot;
import net.vadamdev.jdautils.configuration.ConfigurationLoader;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * @author VadamDev
 * @since 08/06/2023
 */
public class JafarBot extends JDABot implements IReloadable {
    public final MainConfig mainConfig;

    private File profilesFile;

    private ProfileManager profileManager;
    private RoleReactionManager roleReactionManager;
    private ChannelCreatorManager channelCreatorManager;
    private CaptainedBoatManager captainedBoatManager;

    private ActivityTracker activityTracker;

    public JafarBot() {
        super(BotToken.RELEASE.getToken(), "!");

        this.mainConfig = new MainConfig();
    }

    @Override
    public void onEnable() {
        loadConfigs();

        jda.getPresence().setActivity(mainConfig.formatActivity());

        profileManager = new ProfileManager(profilesFile);

        roleReactionManager = new RoleReactionManager();
        registerRolereactions();

        channelCreatorManager = new ChannelCreatorManager();
        registerChannelCreators();

        captainedBoatManager = new CaptainedBoatManager();

        registerListeners(
                new EventListener()
        );

        registerCommands(
                new SettingsCommand(),
                new RolereactionCommand(),
                new ClearCommand(),
                new InfoCommand(),
                new ActivityCommand(),

                new BoatCommand(),
                new BoatOptionsCommand()
        );

        activityTracker = new ActivityTracker(jda);
    }

    @Override
    public void onReload() {
        try {
            ConfigurationLoader.loadConfiguration(mainConfig);
            jda.getPresence().setActivity(mainConfig.formatActivity());

            Main.logger.info("Configuration was reloaded successfully !");
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        activityTracker.onDisable();
        profileManager.onDisable();
    }

    private void loadConfigs() {
        try {
            ConfigurationLoader.loadConfiguration(mainConfig);
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            profilesFile = Utils.initFile("JafarBot/profiles.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerRolereactions() {
        roleReactionManager.addRoleReaction(new RoleReaction(
                "job",
                "Postes",
                RoleOption.of(mainConfig.HELM_ROLE, Emoji.fromUnicode("⛵")),
                RoleOption.of(mainConfig.MAIN_ROLE, Emoji.fromUnicode("\uD83D\uDCA3")),
                RoleOption.of(mainConfig.FLEX_ROLE, Emoji.fromUnicode("⚔️")),
                RoleOption.of(mainConfig.CARPENTER_ROLE, Emoji.fromUnicode("\uD83D\uDD28"))
        ));

        roleReactionManager.addRoleReaction(new RoleReaction(
                "gameplay",
                "Gameplay",
                RoleOption.of(mainConfig.PVP_ROLE, Emoji.fromUnicode("☠️")),
                RoleOption.of(mainConfig.PVE_ROLE, Emoji.fromUnicode("\uD83D\uDCB5")),
                RoleOption.of(mainConfig.TDM_ROLE, Emoji.fromUnicode("\uD83D\uDD2B"))
        ));

        roleReactionManager.addRoleReaction(new RoleReaction(
                "notifications",
                "Notifications",
                RoleOption.of(mainConfig.MATE_SEARCH_ROLE, Emoji.fromUnicode("\uD83D\uDCE3"))
        ));
    }

    private void registerChannelCreators() {
        channelCreatorManager.registerChannelCreator(new ChannelCreator(
                mainConfig.BOAT_CREATOR,
                mainConfig.BOAT_CREATOR_CATEGORY,
                "\uD83D\uDD0A┃Bateau #%index%"
        ));

        channelCreatorManager.registerChannelCreator(new ChannelCreator(
                mainConfig.GAMES_CREATOR,
                mainConfig.GAMES_CREATOR_CATEGORY,
                "\uD83D\uDD0A┃Vocal #%index%"
        ));
    }

    @Nonnull
    @Override
    protected JDABuilder initBuilder(JDABuilder jdaBuilder) {
        return jdaBuilder
                .setAutoReconnect(true)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public RoleReactionManager getRoleReactionManager() {
        return roleReactionManager;
    }

    public ChannelCreatorManager getChannelCreatorManager() {
        return channelCreatorManager;
    }

    public CaptainedBoatManager getCaptainedBoatManager() {
        return captainedBoatManager;
    }
}
