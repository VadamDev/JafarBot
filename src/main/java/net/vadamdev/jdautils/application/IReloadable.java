package net.vadamdev.jdautils.application;

/**
 * A {@link JDABot} implementing this interface can be reloaded with the /reload command.
 *
 * @author VadamDev
 * @since 02/04/2023
 */
public interface IReloadable {
    void onReload();
}
