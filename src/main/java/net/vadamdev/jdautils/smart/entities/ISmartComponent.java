package net.vadamdev.jdautils.smart.entities;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;

/**
 * @author VadamDev
 * @since 08/01/2024
 */
public interface ISmartComponent {
    void run(GenericComponentInteractionCreateEvent event);

    ActionComponent getComponent();
}
