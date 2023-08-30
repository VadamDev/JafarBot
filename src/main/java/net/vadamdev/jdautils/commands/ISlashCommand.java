package net.vadamdev.jdautils.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import javax.annotation.Nonnull;

/**
 * @author VadamDev
 * @since 13/11/2022
 */
public interface ISlashCommand {
    @Nonnull
    SlashCommandData createSlashCommand();

    @Deprecated
    default void onAutoCompleteEvent(@Nonnull CommandAutoCompleteInteractionEvent event) {}

    default boolean isSlashOnly() {
        return true;
    }
}
