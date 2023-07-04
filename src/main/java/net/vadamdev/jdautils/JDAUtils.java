package net.vadamdev.jdautils;

import net.dv8tion.jda.api.JDA;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.CommandHandler;

/**
 * @author VadamDev
 * @since 17/10/2022
 */
public final class JDAUtils {
    private final JDA jda;
    private final String commandPrefix;

    private CommandHandler commandHandler;

    public JDAUtils(JDA jda, String commandPrefix) {
        this.jda = jda;
        this.commandPrefix = commandPrefix;
    }

    public void initCommandHandler() {
        commandHandler = new CommandHandler(jda, commandPrefix);
        jda.addEventListener(commandHandler);
    }

    public void registerCommand(Command command) {
        if(commandHandler == null)
            return;

        commandHandler.registerCommand(command);
    }

    public void finishCommandRegistry() {
        if(commandHandler == null)
            return;

        commandHandler.registerCommands();
    }
}
