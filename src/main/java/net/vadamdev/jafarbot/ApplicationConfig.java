package net.vadamdev.jafarbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import net.vadamdev.dbk.framework.config.Configuration;
import net.vadamdev.dbk.framework.config.annotations.ConfigRange;
import net.vadamdev.dbk.framework.config.annotations.ConfigValue;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;

/**
 * @author VadamDev
 * @since 29/03/2025
 */
public final class ApplicationConfig extends Configuration {
    @ConfigValue(path = "client.token", comment = "The bot token, provided by the Discord Developer Portal")
    String TOKEN = "Secret Token Here";

    @ConfigValue(path = "client.guildId", comment = "The guild ID associated with the bot")
    public String GUILD_ID = "";

    /*
        Logging
     */

    @ConfigValue(path = "logging.actions.enabled", comment = "Should action such as joining/leaving a voice channel be logged in the webhook?")
    public boolean LOG_ACTIONS = true;

    @ConfigRange(min = 16, setToNearestLimit = false)
    @ConfigValue(path = "logging.actions.messageCacheSize", comment = "The size of the cache used to know the content of a deleted/edited message")
    public int MESSAGE_CACHE_SIZE = 256;

    @ConfigValue(path = "logging.webhook.enabled", comment = "Should the webhook be enabled?")
    public boolean WEBHOOK_ENABLED = false;

    @ConfigValue(path = "logging.webhook.url")
    public String WEBHOOK_URL = "";

    /*
       Activity
     */

    @ConfigValue(path = "activity.activityType")
    public String ACTIVITY_TYPE = Activity.ActivityType.PLAYING.name();

    @ConfigValue(path = "activity.activity")
    public String ACTIVITY = "Sea of Thieves";

    ApplicationConfig() {
        super("./application.yml");
    }

    public void updateWebhookLogger(@Nullable  Webhook webhook) throws IOException {
        if(webhook != null) {
            setValue("WEBHOOK_ENABLED", true);
            setValue("WEBHOOK_URL", webhook.getUrl());
        }else {
            setValue("WEBHOOK_ENABLED", false);
            setValue("WEBHOOK_URL", "");
        }

        save();
    }

    public RestAction<Optional<Webhook>> retrieveLogWebhook() {
        final JafarBot jafarBot = JafarBot.get();
        final JDA jda = jafarBot.getJDA();

        final Guild guild = jafarBot.getGuildLinkService().getLinkedGuild().orElse(null);
        if(guild == null)
            return new CompletedRestAction<>(jda, null, new NullPointerException("Guild is null"));

        if(WEBHOOK_URL == null || WEBHOOK_URL.isBlank())
            return new CompletedRestAction<>(jda, Optional.empty(), null);

        return guild.retrieveWebhooks().map(webHooks -> webHooks.stream().filter(w -> w.getUrl().equals(WEBHOOK_URL)).findFirst());
    }

    @Nullable
    public Activity formatActivity() {
        if(ACTIVITY_TYPE == null || ACTIVITY == null)
            return null;

        return Activity.of(Activity.ActivityType.valueOf(ACTIVITY_TYPE), ACTIVITY);
    }

    public void updateActivity(JDA jda, @Nullable Activity.ActivityType activityType, @Nullable String activity) throws IOException {
        setValue("ACTIVITY_TYPE", activityType != null ? activityType.name() : null);
        setValue("ACTIVITY", activity);
        save();

        jda.getPresence().setActivity(formatActivity());
    }
}
