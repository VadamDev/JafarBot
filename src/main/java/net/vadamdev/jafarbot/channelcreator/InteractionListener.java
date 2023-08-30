package net.vadamdev.jafarbot.channelcreator;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import javax.annotation.Nonnull;

/**
 * @author VadamDev
 * @since 30/08/2023
 */
public interface InteractionListener {
    default void handleButtonInteractionEvent(@Nonnull ButtonInteractionEvent event) {}
    default void handleSelectInteractionEvent(@Nonnull StringSelectInteractionEvent event) {}
    default void handleModalInteractionEvent(@Nonnull ModalInteractionEvent event) {}
}
