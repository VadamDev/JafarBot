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

    private final CommandHandler commandHandler;

    public JDAUtils(JDA jda, String commandPrefix) {
        this.jda = jda;

        this.commandHandler = new CommandHandler(commandPrefix, jda);

        jda.addEventListener(new JDAUListener(commandHandler));
    }

    public void registerCommand(Command command) {
        commandHandler.registerCommand(command);
    }

    public void finishCommandRegistry() {
        commandHandler.registerSlashCommands(jda);
    }
}
