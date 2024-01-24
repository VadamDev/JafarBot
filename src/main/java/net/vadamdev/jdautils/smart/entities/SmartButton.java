package net.vadamdev.jdautils.smart.entities;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 08/01/2024
 */
public class SmartButton implements ISmartComponent {
    public static SmartButton of(Button button, Consumer<ButtonInteractionEvent> consumer) {
        return new SmartButton(button, consumer);
    }

    public static SmartButton empty(Button button) {
        if(!button.getStyle().equals(ButtonStyle.LINK))
            return of(button, event -> event.deferEdit().queue());

        return of(button, event -> {});
    }

    private final Button button;
    private final Consumer<ButtonInteractionEvent> consumer;

    private SmartButton(Button button, Consumer<ButtonInteractionEvent> consumer) {
        this.button = button;
        this.consumer = consumer;
    }

    @Override
    public void run(GenericComponentInteractionCreateEvent event) {
        consumer.accept((ButtonInteractionEvent) event);
    }

    @Override
    public ActionComponent getComponent() {
        return button;
    }
}
