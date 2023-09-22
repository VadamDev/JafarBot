package net.vadamdev.jafarbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.vadamdev.jafarbot.activity.ActivityTracker;
import net.vadamdev.jafarbot.captaincy.CaptainedBoatManager;
import net.vadamdev.jafarbot.channelcreator.ChannelCreatorManager;
import net.vadamdev.jafarbot.channelcreator.impl.BoatChannelCreator;
import net.vadamdev.jafarbot.channelcreator.impl.GamesChannelCreator;
import net.vadamdev.jafarbot.commands.*;
import net.vadamdev.jafarbot.config.MainConfig;
import net.vadamdev.jafarbot.listeners.EventListener;
import net.vadamdev.jafarbot.music.PlayerManager;
import net.vadamdev.jafarbot.profile.ProfileManager;
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
    private ChannelCreatorManager channelCreatorManager;
    private CaptainedBoatManager captainedBoatManager;
    private PlayerManager playerManager;

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

        channelCreatorManager = new ChannelCreatorManager();
        registerChannelCreators();

        captainedBoatManager = new CaptainedBoatManager();

        final Guild guild = jda.getGuildById(mainConfig.GUILD_ID);
        if(guild != null)
            playerManager = new PlayerManager(guild);

        registerListeners(
                new EventListener()
        );

        registerCommands(
                new SettingsCommand(),
                new ClearCommand(),
                new InfoCommand(),
                new ActivityCommand(),

                new BoatCommand(),
                new BoatOptionsCommand(),

                new MusicCommand()
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
        activityTracker.shutdown();
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

    private void registerChannelCreators() {
        channelCreatorManager.registerChannelCreator(new BoatChannelCreator());
        channelCreatorManager.registerChannelCreator(new GamesChannelCreator());
    }

    @Nonnull
    @Override
    protected JDABuilder computeBuilder(JDABuilder jdaBuilder) {
        return jdaBuilder
                .setAutoReconnect(true)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES)
                .enableCache(CacheFlag.ACTIVITY);
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public ChannelCreatorManager getChannelCreatorManager() {
        return channelCreatorManager;
    }

    public CaptainedBoatManager getCaptainedBoatManager() {
        return captainedBoatManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ActivityTracker getActivityTracker() {
        return activityTracker;
    }
}
