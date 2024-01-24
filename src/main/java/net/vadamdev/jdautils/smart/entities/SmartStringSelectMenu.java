package net.vadamdev.jdautils.smart.entities;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 08/01/2024
 */
public class SmartStringSelectMenu implements ISmartComponent {
    public static SmartStringSelectMenu of(StringSelectMenu selectMenu, Consumer<StringSelectInteractionEvent> consumer) {
        return new SmartStringSelectMenu(selectMenu, consumer);
    }

    private final StringSelectMenu selectMenu;
    private final Consumer<StringSelectInteractionEvent> consumer;

    private SmartStringSelectMenu(StringSelectMenu selectMenu, Consumer<StringSelectInteractionEvent> consumer) {
        this.selectMenu = selectMenu;
        this.consumer = consumer;
    }

    @Override
    public void run(GenericComponentInteractionCreateEvent event) {
        consumer.accept((StringSelectInteractionEvent) event);
    }

    @Override
    public ActionComponent getComponent() {
        return selectMenu;
    }
}
