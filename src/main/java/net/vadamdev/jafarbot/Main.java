package net.vadamdev.jafarbot;

import net.vadamdev.jdautils.application.JDAApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VadamDev
 * @since 08/06/2023
 */
public class Main {
    public static final Logger logger = LoggerFactory.getLogger(JafarBot.class);
    public static final JafarBot jafarBot = new JafarBot();

    public static void main(String[] args) {
        final JDAApplication<JafarBot> application = new JDAApplication<>(jafarBot, logger);

        application.registerCommand("serialize", bot -> bot.getProfileManager().serialize());
        application.registerCommand("unserialize", bot -> bot.getProfileManager().unserialize());

        application.start();
    }
}
