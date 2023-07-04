package net.vadamdev.jdautils.commands.data.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.vadamdev.jdautils.commands.data.ICommandData;

import javax.annotation.Nonnull;

/**
 * @author VadamDev
 * @since 08/06/2023
 */
public class SlashCommandData implements ICommandData {
    private final SlashCommandInteractionEvent event;

    public SlashCommandData(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    public SlashCommandInteractionEvent getEvent() {
        return event;
    }

    @Nonnull
    @Override
    public Type getType() {
        return Type.SLASH;
    }
}
