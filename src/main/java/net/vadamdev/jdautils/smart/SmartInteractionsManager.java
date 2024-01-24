package net.vadamdev.jdautils.smart;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.vadamdev.jdautils.smart.entities.ISmartComponent;
import net.vadamdev.jdautils.smart.entities.SmartModal;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 08/01/2024
 */
public final class SmartInteractionsManager {
    private static final Map<String, Map<String, List<ISmartComponent>>> smartMessages = new HashMap<>();
    private static final Map<String, Consumer<ModalInteractionEvent>> smartModals = new HashMap<>();

    private SmartInteractionsManager() {}

    public static void registerSmartMessage(Message message, List<ISmartComponent> components) {
        smartMessages.computeIfAbsent(message.getGuildId(), k -> new HashMap<>())
                .put(message.getId(), components);
    }

    public static void unregisterSmartMessage(String guildId, String messageId) {
        smartMessages.computeIfPresent(guildId, (k, v) -> {
            v.remove(messageId);

            if(v.isEmpty())
                return null;

            return v;
        });
    }

    public static void registerSmartModal(SmartModal smartModal) {
        smartModals.put(smartModal.getModalId(), smartModal.getConsumer());
    }

    public static void unregisterSmartModal(SmartModal smartModal) {
        smartModals.remove(smartModal.getModalId());
    }

    public static void handleInteraction(@Nonnull GenericComponentInteractionCreateEvent event) {
        findComponent(event.getGuild().getId(), event.getMessageId(), event.getComponentId())
                .ifPresent(component -> component.run(event));
    }

    public static void handleModalInteraction(@Nonnull ModalInteractionEvent event) {
        final String modalId = event.getModalId();

        if(!smartModals.containsKey(modalId))
            return;

        smartModals.get(modalId).accept(event);
    }

    private static Optional<ISmartComponent> findComponent(String guildId, String messageId, String componentId) {
        return Optional.ofNullable(smartMessages.get(guildId))
                .map(subMap -> subMap.get(messageId))
                .flatMap(components -> components.stream().filter(component -> component.getComponent().getId().equals(componentId)).findFirst());
    }
}
