package net.vadamdev.jafarbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.vadamdev.dbk.framework.DBKFramework;
import net.vadamdev.dbk.framework.application.JDABot;
import net.vadamdev.dbk.framework.application.annotations.AppConfig;
import net.vadamdev.dbk.framework.application.annotations.Bot;
import net.vadamdev.dbk.framework.config.ConfigurationLoader;
import net.vadamdev.jafarbot.activity.ActivityTracker;
import net.vadamdev.jafarbot.captaincy.PortChannelCreator;
import net.vadamdev.jafarbot.channelcreator.LockeableCreatedChannel;
import net.vadamdev.jafarbot.channelcreator.SimpleChannelCreator;
import net.vadamdev.jafarbot.channelcreator.system.ChannelCreatorManager;
import net.vadamdev.jafarbot.commands.*;
import net.vadamdev.jafarbot.configs.MainConfig;
import net.vadamdev.jafarbot.listeners.ActionLogListener;
import net.vadamdev.jafarbot.listeners.EventListener;
import net.vadamdev.jafarbot.logger.WebhookLoggingHandler;
import net.vadamdev.jafarbot.music.GuildMusicManager;
import net.vadamdev.jafarbot.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VadamDev
 * @since 08/06/2023
 */
public class JafarBot extends JDABot {
    private final Logger logger;
    private final WebhookLoggingHandler webhookLogger;

    private final GuildLinkService guildLinkService;

    private final MainConfig mainConfig;

    private ProfileManager profileManager;
    private ActivityTracker activityTracker;
    private ChannelCreatorManager channelCreatorManager;
    private GuildMusicManager musicManager;

    private long startTime;

    JafarBot() {
        super(() -> JDABuilder.createDefault(APP_CONFIG.TOKEN)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setActivity(APP_CONFIG.formatActivity()));

        this.logger = LoggerFactory.getLogger(JafarBot.class);
        this.webhookLogger = new WebhookLoggingHandler();

        this.guildLinkService = new GuildLinkService(APP_CONFIG);

        this.mainConfig = new MainConfig();
    }

    @Override
    protected void onStart() throws Exception {
        if(APP_CONFIG.WEBHOOK_ENABLED)
            webhookLogger.init(APP_CONFIG.WEBHOOK_URL);

        final Guild guild = guildLinkService.init(jda).waitCompleteLink();
        logger.info("Linked to guild: " + guild.getName() + " (" + guild.getId() + ") !");

        //Load the main configuration
        ConfigurationLoader.loadConfiguration(mainConfig);

        //Load features
        profileManager = new ProfileManager("./profiles.json");
        activityTracker = new ActivityTracker();
        channelCreatorManager = new ChannelCreatorManager(jda); registerChannelCreators();
        musicManager = new GuildMusicManager(jda);

        registerListeners(
                new EventListener(mainConfig, profileManager),
                new ActionLogListener(APP_CONFIG, webhookLogger)
        );

        registerCommands(
                new DashboardCommand(),
                new WebhookLoggerCommand(APP_CONFIG, webhookLogger),
                new ClearCommand(),
                new ActivityTrackerCommand(mainConfig),
                new ActivityCommand(APP_CONFIG),
                new InfoCommand(mainConfig, profileManager),
                new BoatsCommand(profileManager),
                new MusicCommand(mainConfig, musicManager)
        );

        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onStop() {
        profileManager.serialize();
        webhookLogger.shutdown();
    }

    private void registerChannelCreators() {
        channelCreatorManager.registerChannelCreator(new PortChannelCreator(
                mainConfig.BOAT_CREATOR,
                (channel, owner) -> new LockeableCreatedChannel(channel.getId(), owner.getId()),
                owner -> "⛵┃Bateau #" + SimpleChannelCreator.CHANNEL_INDEX_PLACEHOLDER,
                owner -> owner.getGuild().getCategoryById(mainConfig.BOAT_CREATOR_CATEGORY)
        ));

        channelCreatorManager.registerChannelCreator(new SimpleChannelCreator(
                mainConfig.GAMES_CREATOR,
                (channel, owner) -> new LockeableCreatedChannel(channel.getId(), owner.getId()),
                owner -> "\uD83C\uDFAE┃Vocal #" + SimpleChannelCreator.CHANNEL_INDEX_PLACEHOLDER,
                owner -> owner.getGuild().getCategoryById(mainConfig.GAMES_CREATOR_CATEGORY)
        ));
    }

    public GuildLinkService getGuildLinkService() {
        return guildLinkService;
    }

    public MainConfig getConfig() {
        return mainConfig;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public ActivityTracker getActivityTracker() {
        return activityTracker;
    }

    public ChannelCreatorManager getChannelCreatorManager() {
        return channelCreatorManager;
    }

    public GuildMusicManager getMusicManager() {
        return musicManager;
    }

    public long getUptimeMs() {
        return System.currentTimeMillis() - startTime;
    }

    /*
       Main
     */

    @AppConfig
    private static final ApplicationConfig APP_CONFIG = new ApplicationConfig();

    @Bot
    private static final JafarBot INSTANCE = new JafarBot();

    public static JafarBot get() { return INSTANCE; }
    public static Logger getLogger() { return INSTANCE.logger; }
    public static WebhookLoggingHandler getWebhookLogger() { return INSTANCE.webhookLogger; }

    public static void main(String[] args) {
        DBKFramework.launch(INSTANCE.getClass(), INSTANCE.logger);
    }
}
