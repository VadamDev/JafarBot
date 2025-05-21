package net.vadamdev.jafarbot.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.utils.EmbedUtils;

/**
 * @author VadamDev
 * @since 03/05/2025
 */
public class WebhookAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent event) {
        final String loggerName = event.getLoggerName();

        final EmbedBuilder embed = EmbedUtils.webhookLogEmbed(EmbedUtils.SYSTEM_COLOR, event.getInstant())
                .setTitle("Console")
                .addField("⚙️ Thread", "```" + event.getThreadName() + "```", true)
                .addField("\uD83D\uDCCB Logger", "```" + loggerName.substring(loggerName.lastIndexOf('.') + 1) + "```", true)
                .addField("⚠️ Level", "```" + event.getLevel().toString() + "```", true)

                ;

        final IThrowableProxy throwableProxy = event.getThrowableProxy();
        if(throwableProxy != null) {
            embed.addField("\uD83D\uDCAC Message", "```" + event.getFormattedMessage() + ":\n" + throwableProxy.getMessage() + "```", false);

            final IThrowableProxy causeProxy = throwableProxy.getCause();
            if(causeProxy != null) {
                final String cause = ThrowableProxyUtil.asString(causeProxy);
                final String formattedCause = cause.length() > 1014 ? cause.substring(0, 1014) + "..." : cause;
                embed.addField("⚡ Cause", "```" + formattedCause + "```", false);
            }
        }else
            embed.addField("\uD83D\uDCAC Message", "```" + event.getFormattedMessage() + "```", false);

        JafarBot.getWebhookLogger().appendLog(WebhookEmbedBuilder.fromJDA(embed.build()).build());
    }
}
