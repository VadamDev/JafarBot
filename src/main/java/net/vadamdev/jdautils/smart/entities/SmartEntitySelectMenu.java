package net.vadamdev.jdautils.smart.entities;

import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;

import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 08/01/2024
 */
public class SmartEntitySelectMenu implements ISmartComponent {
    public static SmartEntitySelectMenu of(EntitySelectMenu selectMenu, Consumer<EntitySelectInteractionEvent> consumer) {
        return new SmartEntitySelectMenu(selectMenu, consumer);
    }

    private final EntitySelectMenu selectMenu;
    private final Consumer<EntitySelectInteractionEvent> consumer;

    private SmartEntitySelectMenu(EntitySelectMenu selectMenu, Consumer<EntitySelectInteractionEvent> consumer) {
        this.selectMenu = selectMenu;
        this.consumer = consumer;
    }

    @Override
    public void run(GenericComponentInteractionCreateEvent event) {
        consumer.accept((EntitySelectInteractionEvent) event);
    }

    @Override
    public ActionComponent getComponent() {
        return selectMenu;
    }
}
