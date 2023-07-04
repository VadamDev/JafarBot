package net.vadamdev.jdautils.commands.data;

import javax.annotation.Nonnull;

/**
 * @author VadamDev
 * @since 08/06/2023
 */
public interface ICommandData {
    @Nonnull
    Type getType();

    enum Type {
        TEXT, SLASH
    }
}
