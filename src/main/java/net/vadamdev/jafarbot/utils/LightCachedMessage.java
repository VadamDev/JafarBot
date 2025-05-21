package net.vadamdev.jafarbot.utils;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VadamDev
 * @since 11/05/2025
 */
public record LightCachedMessage(String channelMention, String authorId, String authorName, String authorAvatarURL, @Nullable String rawContent, List<String> images) {
    public LightCachedMessage(Message message) {
        this(
                message.getChannel().getAsMention(),
                message.getAuthor().getId(),
                message.getAuthor().getEffectiveName(),
                message.getAuthor().getEffectiveAvatarUrl(),
                !message.getContentRaw().isEmpty() ? message.getContentRaw() : null,
                Utils.getMessageImages(message)
        );
    }

    public String authorMention() {
        return "<@" + authorId + ">";
    }

    public List<String> getImagesDifference(Message message) {
        final List<String> messageImages = Utils.getMessageImages(message);
        final List<String> result = new ArrayList<>();

        for(String imageURL : images) {
            if(!messageImages.contains(imageURL))
                result.add(imageURL);
        }

        return result;
    }
}
