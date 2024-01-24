package net.vadamdev.jdautils.smart.messages;

import net.dv8tion.jda.api.entities.Guild;

/**
 * @author VadamDev
 * @since 08/01/2024
 */
public interface SmartMessageProvider {
    void init(Guild guild, String channelId, MessageContent contents);
}
