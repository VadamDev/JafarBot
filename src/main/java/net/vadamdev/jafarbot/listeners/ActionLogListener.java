package net.vadamdev.jafarbot.listeners;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.vadamdev.jafarbot.ApplicationConfig;
import net.vadamdev.jafarbot.logger.WebhookLoggingHandler;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.LightCachedMessage;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author VadamDev
 * @since 07/05/2025
 */
public class ActionLogListener extends ListenerAdapter {
    private final ApplicationConfig appConfig;
    private final WebhookLoggingHandler webhookLogger;

    private final Map<String, LightCachedMessage> cachedMessages;

    public ActionLogListener(ApplicationConfig appConfig, WebhookLoggingHandler webhookLogger) {
        this.appConfig = appConfig;
        this.webhookLogger = webhookLogger;

        this.cachedMessages = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, LightCachedMessage> eldest) {
                return size() > appConfig.MESSAGE_CACHE_SIZE;
            }
        };
    }

    /*
       Events
     */

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!appConfig.LOG_ACTIONS || !event.isFromGuild())
            return;

        final User user = event.getAuthor();
        if(user.isSystem() || user.isBot())
            return;

        final Message message = event.getMessage();
        cachedMessages.put(message.getId(), new LightCachedMessage(message));
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if(!appConfig.LOG_ACTIONS || !event.isFromGuild())
            return;

        final String messageId = event.getMessageId();

        final LightCachedMessage cachedMessage = cachedMessages.get(messageId);
        if(cachedMessage == null)
            return;

        if(cachedMessage.rawContent() != null) {
            webhookLogger.appendLog(createUserLog(
                    EmbedUtils.LOG_NEGATIVE_COLOR,
                    cachedMessage,
                    e -> e
                            .setDescription("Message sent by " + cachedMessage.authorMention() + " deleted in " + cachedMessage.channelMention())
                            .addField("Content", cachedMessage.rawContent(), false)
                            .setFooter("Author: " + cachedMessage.authorId() + " | Message ID: " + messageId)
            ));
        }

        final List<String> images = cachedMessage.images();
        if(!images.isEmpty()) {
            for(String imageURL : images) {
                webhookLogger.appendLog(createUserLog(
                        EmbedUtils.LOG_NEGATIVE_COLOR,
                        cachedMessage,
                        e -> e
                                .setDescription("Image sent by " + cachedMessage.authorMention() + " deleted in " + cachedMessage.channelMention())
                                .setImage(imageURL)
                                .setFooter("Author: " + cachedMessage.authorId() + " | Message ID: " + messageId)
                ));
            }
        }

        cachedMessages.remove(messageId);
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if(!appConfig.LOG_ACTIONS || !event.isFromGuild())
            return;

        final User user = event.getAuthor();
        if(user.isSystem() || user.isBot())
            return;

        final String messageId = event.getMessageId();
        if(!cachedMessages.containsKey(messageId))
            return;

        final LightCachedMessage oldMessage = cachedMessages.get(messageId);
        final Message newMessage = event.getMessage();

        if(oldMessage.rawContent() != null && !oldMessage.rawContent().equals(newMessage.getContentRaw())) {
            webhookLogger.appendLog(createUserLog(
                    EmbedUtils.LOG_NEUTRAL_COLOR,
                    user,
                    e -> e
                            .setDescription("Message edited in " + newMessage.getChannel().getAsMention() + " [Jump to message](" + newMessage.getJumpUrl() + ")")
                            .addField("Before", oldMessage.rawContent(), false)
                            .addField("After", newMessage.getContentRaw(), false)
            ));
        }

        final List<String> removedImages = oldMessage.getImagesDifference(newMessage);
        if(!removedImages.isEmpty()) {
            for(String imageURL : removedImages) {
                webhookLogger.appendLog(createUserLog(
                        EmbedUtils.LOG_NEGATIVE_COLOR,
                        user,
                        e -> e
                                .setDescription("Image sent by " + newMessage.getAuthor().getAsMention() + " deleted in " + newMessage.getChannel().getAsMention())
                                .setImage(imageURL)
                                .setFooter("Author: " + user.getId() + " | Message ID: " + messageId)
                ));
            }
        }

        cachedMessages.replace(messageId, new LightCachedMessage(newMessage));
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if(!appConfig.LOG_ACTIONS)
            return;

        final User user = event.getUser();

        webhookLogger.appendLog(createUserLog(
                EmbedUtils.LOG_POSITIVE_COLOR,
                user,
                e -> e.setDescription(user.getAsMention() + " joined the guild !")
        ));
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if(!appConfig.LOG_ACTIONS)
            return;

        final User user = event.getUser();

        webhookLogger.appendLog(createUserLog(
                EmbedUtils.LOG_NEGATIVE_COLOR,
                user,
                e -> e.setDescription(user.getAsMention() + " left the guild !")
        ));
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        if(!appConfig.LOG_ACTIONS)
            return;

        final User user = event.getUser();

        webhookLogger.appendLog(createUserLog(
                EmbedUtils.LOG_NEUTRAL_COLOR,
                user,
                e -> e.setDescription(user.getAsMention() + " changed nickname ``" + event.getOldNickname() + "`` -> ``" + event.getNewNickname() + "``")
        ));
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if(!appConfig.LOG_ACTIONS)
            return;

        final User user = event.getMember().getUser();

        final AudioChannelUnion joined = event.getChannelJoined();
        final AudioChannelUnion left = event.getChannelLeft();

        if(joined != null && left != null) {
            webhookLogger.appendLog(createUserLog(
                    EmbedUtils.LOG_POSITIVE_COLOR,
                    user,
                    e -> e.setDescription(user.getAsMention() + " switched voice channels " + left.getAsMention() + " -> " + joined.getAsMention())
            ));
        }else if(joined != null) {
            webhookLogger.appendLog(createUserLog(
                    EmbedUtils.LOG_POSITIVE_COLOR,
                    user,
                    e -> e.setDescription(user.getAsMention() + " joined voice channel " + joined.getAsMention())
            ));
        }else if(left != null) {
            webhookLogger.appendLog(createUserLog(
                    EmbedUtils.LOG_NEGATIVE_COLOR,
                    user,
                    e -> e.setDescription(user.getAsMention() + " left voice channel " + left.getAsMention())
            ));
        }
    }

    /*
       Utils
     */

    private static WebhookEmbed createUserLog(int color, String userId, String userName, String userAvatarURL, UnaryOperator<EmbedBuilder> mapper) {
        final EmbedBuilder builder = EmbedUtils.webhookLogEmbed(color, Instant.now())
                .setAuthor(userName, null, userAvatarURL)
                .setFooter("User ID: " + userId);

        return WebhookEmbedBuilder.fromJDA(mapper.apply(builder).build()).build();
    }

    private static WebhookEmbed createUserLog(int color, User user, UnaryOperator<EmbedBuilder> mapper) {
        return createUserLog(color, user.getId(), user.getEffectiveName(), user.getEffectiveAvatarUrl(), mapper);
    }

    private static WebhookEmbed createUserLog(int color, LightCachedMessage cachedMessage, UnaryOperator<EmbedBuilder> mapper) {
        return createUserLog(color, cachedMessage.authorId(), cachedMessage.authorName(), cachedMessage.authorAvatarURL(), mapper);
    }
}
