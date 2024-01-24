package net.vadamdev.jdautils.smart.entities;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.vadamdev.jdautils.smart.SmartInteractionsManager;

import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 12/01/2024
 */
public class SmartModal {
    public static SmartModal of(Modal modal, Consumer<ModalInteractionEvent> consumer) {
        return new SmartModal(modal, consumer);
    }

    private final Modal modal;
    private final Consumer<ModalInteractionEvent> consumer;

    public SmartModal(Modal modal, Consumer<ModalInteractionEvent> consumer) {
        this.modal = modal;
        this.consumer = consumer;

        SmartInteractionsManager.registerSmartModal(this);
    }

    public void open(GenericComponentInteractionCreateEvent callback) {
        callback.replyModal(modal).queue();
    }

    public void unregister() {
        SmartInteractionsManager.unregisterSmartModal(this);
    }

    public Consumer<ModalInteractionEvent> getConsumer() {
        return consumer;
    }

    public String getModalId() {
        return modal.getId();
    }
}
