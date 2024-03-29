package net.vadamdev.jdautils.application;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.managers.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Represents a JDA application. This class will handle the basics running of a {@link JDABot}
 *
 * @author VadamDev
 * @since 08/06/2023
 */
public class JDAApplication<T extends JDABot> {
    private final T jdaBot;
    private final Logger logger;

    private final Map<String, Consumer<T>> commands;

    public JDAApplication(T jdaBot, @Nullable Logger logger) {
        this.jdaBot = jdaBot;

        if(logger == null)
            this.logger = LoggerFactory.getLogger(JDAApplication.class);
        else
            this.logger = logger;

        this.commands = new HashMap<>();
        addDefaultCommands();

        this.logger.info(
                System.lineSeparator() + System.lineSeparator() +
                "-------------------------------------------------------" + System.lineSeparator() +
                "  JDA Utils by VadamDev (https://github.com/VadamDev)" + System.lineSeparator() +
                System.lineSeparator() +
                "      Bot Class: " + jdaBot.getClass().getName() + "     " + System.lineSeparator() +
                "-------------------------------------------------------" +
                System.lineSeparator()
        );
    }

    public void start() {
        logger.info("Starting " + jdaBot.getClass().getSimpleName() + "...");

        try {
            jdaBot.setup();
        }catch(Exception e) {
            logger.error("An error occurred while starting the bot:");
            e.printStackTrace();

            System.exit(-1);
        }

        final Scanner scanner = new Scanner(System.in);
        do {
            try {
                final String str = scanner.next();

                Map.Entry<String, Consumer<T>> command = commands.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(str))
                        .findFirst().orElse(null);

                if(command != null)
                    command.getValue().accept(jdaBot);
                else
                    logger.info("Unknown command ! Type help to see a list of all available commands.");
            } catch(NoSuchElementException | IllegalStateException e) {
                e.printStackTrace();
            }
        }while(scanner.hasNext());
    }

    public void stop() {
        logger.info("Disabling JDA Application...");

        jdaBot.onDisable();
        System.exit(0);
    }

    public void registerCommand(String name, Consumer<T> command) {
        commands.put(name, command);
    }

    private void addDefaultCommands() {
        commands.put("help", jdaBot -> {
            logger.info("Available commands:");

            commands.keySet().stream()
                    .filter(command -> !command.equals("help"))
                    .forEach(logger::info);
        });

        commands.put("stop", jdaBot -> stop());

        if(jdaBot instanceof IReloadable) {
            commands.put("reload", jdaBot -> {
                final Presence presence = jdaBot.getJda().getPresence();

                presence.setPresence(OnlineStatus.IDLE, true);

                logger.info("Trying to reload the app...");
                ((IReloadable) jdaBot).onReload();
                logger.info("Reloading was successful !");

                presence.setPresence(OnlineStatus.ONLINE, false);
            });
        }
    }
}
