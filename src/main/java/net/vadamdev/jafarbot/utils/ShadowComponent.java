package net.vadamdev.jafarbot.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.vadamdev.dbk.framework.interactive.InteractiveComponents;
import net.vadamdev.dbk.framework.interactive.api.components.InteractiveComponent;

import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 11/05/2025
 */
public class ShadowComponent<T extends GenericComponentInteractionCreateEvent> implements InteractiveComponent<T> {
    public static <T extends GenericComponentInteractionCreateEvent> ShadowComponent<T> of(String componentId, Consumer<T> action, Class<T> clazz) {
        return new ShadowComponent<>(componentId, action, clazz);
    }

    private final String componentId;
    private final Consumer<T> action;
    private final Class<T> clazz;

    protected ShadowComponent(String componentId, Consumer<T> action, Class<T> clazz) {
        this.componentId = componentId;
        this.action = action;
        this.clazz = clazz;
    }

    @Override
    public void execute(T event) {
        action.accept(event);
    }

    @Override
    public boolean isValidFor(T event) {
        return event.getComponentId().equals(componentId);
    }

    @Override
    public Class<T> getClassType() {
        return clazz;
    }

    @Override
    public boolean shouldInvalidateOnCatch() {
        return false;
    }

    public ShadowComponent<T> register(JDA jda) {
        InteractiveComponents.registerComponent(jda, this);
        return this;
    }
}
