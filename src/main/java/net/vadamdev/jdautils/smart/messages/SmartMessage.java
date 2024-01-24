package net.vadamdev.jdautils.smart.messages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.vadamdev.jdautils.smart.SmartInteractionsManager;
import net.vadamdev.jdautils.smart.entities.ISmartComponent;

/**
 * @author VadamDev
 * @since 08/01/2024
 */
public class SmartMessage {
    public static SmartMessage fromProvider(SmartMessageProvider provider) {
        return new SmartMessage(provider, null);
    }

    private final SmartMessageProvider provider;
    private final MessageContent content;

    private SmartMessage(SmartMessageProvider provider, MessageContent content) {
        this.provider = provider;
        this.content = content;
    }

    public Message open(IReplyCallback callback) {
        final MessageContent content = computeContent(callback.getGuild(), callback.getChannelId());

        final Message message = callback.replyEmbeds(content.getEmbeds())
                .setComponents(content.getComponents()).complete()
                .retrieveOriginal().complete();

        SmartInteractionsManager.registerSmartMessage(message, content.getClickeableComponents());

        return message;
    }

    public Message open(TextChannel channel) {
        final MessageContent content = computeContent(channel.getGuild(), channel.getId());

        final Message message = channel.sendMessageEmbeds(content.getEmbeds())
                .setComponents(content.getComponents())
                .complete();

        SmartInteractionsManager.registerSmartMessage(message, content.getClickeableComponents());

        return message;
    }

    public void open(Message message) {
        final MessageContent content = computeContent(message.getGuild(), message.getChannelId());

        message.editMessageEmbeds(content.getEmbeds())
                .setComponents(content.getComponents())
                .complete();

        SmartInteractionsManager.unregisterSmartMessage(message.getGuildId(), message.getId());
        SmartInteractionsManager.registerSmartMessage(message, content.getClickeableComponents());
    }

    private MessageContent computeContent(Guild guild, String channelId) {
        if(provider != null) {
            final MessageContent content = new MessageContent();
            provider.init(guild, channelId, content);

            return content;
        }else
            return content;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final MessageContent content;

        private Builder() {
            this.content = new MessageContent();
        }

        public Builder setEmbed(MessageEmbed... embeds) {
            content.setEmbed(embeds);
            return this;
        }

        public Builder addComponents(ISmartComponent... components) {
            content.addComponents(components);
            return this;
        }

        public SmartMessage build() {
            return new SmartMessage(null, content);
        }
    }
}
