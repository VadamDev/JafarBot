package net.vadamdev.jdautils.commands.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VadamDev
 * @since 08/06/2023
 */
public interface ICommandData {
    enum Type {
        TEXT, SLASH
    }

    @Nonnull
    Type getType();

    @Nullable
    default <T extends ICommandData> T castOrNull(Class<T> clazz) {
        return getClass().isAssignableFrom(clazz) ? (T) this : null;
    }
}
