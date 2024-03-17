package net.vadamdev.jdautils;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.vadamdev.jdautils.commands.CommandHandler;
import net.vadamdev.jdautils.smart.SmartInteractionsManager;

import javax.annotation.Nonnull;

/**
 * @author VadamDev
 * @since 08/01/2024
 */
public class JDAUListener extends ListenerAdapter {
    private final CommandHandler commandHandler;

    public JDAUListener(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        commandHandler.handleMessageReceive(event);
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        commandHandler.handleSlashCommandInteraction(event);
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        SmartInteractionsManager.handleInteraction(event);
    }

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        SmartInteractionsManager.handleInteraction(event);
    }

    @Override
    public void onEntitySelectInteraction(@Nonnull EntitySelectInteractionEvent event) {
        SmartInteractionsManager.handleInteraction(event);
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        SmartInteractionsManager.handleModalInteraction(event);
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        SmartInteractionsManager.unregisterSmartMessage(event.getGuild().getId(), event.getMessageId());
    }

    @Override
    public void onMessageBulkDelete(@Nonnull MessageBulkDeleteEvent event) {
        event.getMessageIds().forEach(messageId -> SmartInteractionsManager.unregisterSmartMessage(event.getGuild().getId(), messageId));
    }
}
