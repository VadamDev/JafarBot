package net.vadamdev.jdautils.commands;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import javax.annotation.Nonnull;

/**
 * A {@link Command} implementing this interface will be slash command compatible
 *
 * @see net.vadamdev.jdautils.commands.data.SlashCmdData SlashCmdData
 *
 * @author VadamDev
 * @since 13/11/2022
 */
public interface ISlashCommand {
    @Nonnull
    SlashCommandData createSlashCommand();

    /**
     * Determines if the command is compatible with the legacy command system
     *
     * @return True if the command can only be used as a slash command
     */
    default boolean isSlashOnly() {
        return true;
    }
}
