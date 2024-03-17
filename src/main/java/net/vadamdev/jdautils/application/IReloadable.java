package net.vadamdev.jdautils.application;

/**
 * A {@link JDABot} implementing this interface will have the possibility to be reloaded with the reload console command.
 *
 * @author VadamDev
 * @since 02/04/2023
 */
public interface IReloadable {
    /**
     * Called when the reload console commande is typed
     */
    void onReload();
}
