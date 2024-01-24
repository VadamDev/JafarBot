package net.vadamdev.jdautils.commands.data;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nonnull;

/**
 * @author VadamDev
 * @since 08/06/2023
 */
public class SlashCmdData implements ICommandData {
    private final SlashCommandInteractionEvent event;

    public SlashCmdData(SlashCommandInteractionEvent event) {
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
