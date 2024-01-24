package net.vadamdev.jafarbot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.vadamdev.jafarbot.Main;

import java.awt.*;

/**
 * @author VadamDev
 * @since 22/01/2024
 */
public class JafarEmbed extends EmbedBuilder {
    public static final Color NEUTRAL_COLOR = Color.ORANGE;
    public static final Color SUCCESS_COLOR = Color.GREEN;
    public static final Color ERROR_COLOR = Color.RED;

    public JafarEmbed() {
        setFooter("JafarBot", Main.jafarBot.getAvatarURL());
    }
}
