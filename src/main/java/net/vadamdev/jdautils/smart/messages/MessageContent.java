package net.vadamdev.jdautils.smart.messages;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.vadamdev.jdautils.smart.entities.ISmartComponent;

import java.util.*;

/**
 * @author VadamDev
 * @since 08/01/2024
 */
public class MessageContent {
    private Collection<MessageEmbed> embeds;
    private final List<ISmartComponent[]> interactables;

    public MessageContent() {
        this.interactables = new ArrayList<>();
    }

    /*
       Setters
     */

    public void setEmbed(MessageEmbed... embeds) {
        this.embeds = Arrays.asList(embeds);
    }

    public void addComponents(ISmartComponent... component) {
        interactables.add(component);
    }

    /*
       Getters
     */

    protected Collection<MessageEmbed> getEmbeds() {
        return embeds;
    }

    protected List<ISmartComponent> getClickeableComponents() {
        final List<ISmartComponent> components = new ArrayList<>();

        for(ISmartComponent[] interactable : interactables)
            Collections.addAll(components, interactable);

        return components;
    }

    protected Collection<ActionRow> getComponents() {
        final List<ActionRow> rows = new ArrayList<>();

        for (ISmartComponent[] interactable : interactables) {
            final List<ItemComponent> components = new ArrayList<>();

            for(ISmartComponent component : interactable)
                components.add(component.getComponent());

            rows.add(ActionRow.of(components));
        }

        return rows;
    }
}
