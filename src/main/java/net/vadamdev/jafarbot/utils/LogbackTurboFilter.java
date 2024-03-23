package net.vadamdev.jafarbot.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

/**
 * @author VadamDev
 * @since 22/03/2024
 */
public class LogbackTurboFilter extends TurboFilter {
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if(logger.getName().equals("com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAccessTokenTracker") && format.equals("YouTube auth tokens can't be retrieved because email and password is not set in YoutubeAudioSourceManager, age restricted videos will throw exceptions."))
            return FilterReply.DENY;

        return FilterReply.NEUTRAL;
    }
}
