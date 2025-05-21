package net.vadamdev.jafarbot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.vadamdev.jafarbot.JafarBot;

import java.awt.*;
import java.time.temporal.TemporalAccessor;

/**
 * @author VadamDev
 * @since 29/03/2025
 */
public final class EmbedUtils {
    private EmbedUtils() {}

    public static final int DEFAULT_COLOR = new Color(255, 200, 0).getRGB();
    public static final int SYSTEM_COLOR = new Color(96, 96, 96).getRGB();

    public static final int SUCCESS_COLOR = new Color(0, 255, 0).getRGB();
    public static final int ERROR_COLOR = new Color(255, 0, 0).getRGB();

    public static final int LOG_NEUTRAL_COLOR = new Color(45, 155, 218).getRGB();
    public static final int LOG_POSITIVE_COLOR = new Color(89, 238, 90).getRGB();
    public static final int LOG_NEGATIVE_COLOR = new Color(236, 70, 34, 255).getRGB();

    public static EmbedBuilder defaultEmbed() {
        return new EmbedBuilder()
                .setColor(DEFAULT_COLOR)
                .setFooter(JafarBot.get().getAppName(), JafarBot.get().getAvatarURL());
    }

    public static EmbedBuilder defaultEmbed(Color color) {
        return new EmbedBuilder()
                .setColor(color)
                .setFooter(JafarBot.get().getAppName(), JafarBot.get().getAvatarURL());
    }

    public static EmbedBuilder defaultEmbed(int color) {
        return new EmbedBuilder()
                .setColor(color)
                .setFooter(JafarBot.get().getAppName(), JafarBot.get().getAvatarURL());
    }

    public static EmbedBuilder defaultSuccess(String message) {
        return defaultEmbed(SUCCESS_COLOR)
                .setDescription(message);
    }

    public static EmbedBuilder defaultError(String message) {
        return defaultEmbed(ERROR_COLOR)
                .setDescription(message);
    }

    public static EmbedBuilder webhookLogEmbed(TemporalAccessor timestamp) {
        return new EmbedBuilder()
                .setColor(LOG_NEUTRAL_COLOR)
                .setTimestamp(timestamp);
    }

    public static EmbedBuilder webhookLogEmbed(int color, TemporalAccessor timestamp) {
        return new EmbedBuilder()
                .setColor(color)
                .setTimestamp(timestamp);
    }
}
