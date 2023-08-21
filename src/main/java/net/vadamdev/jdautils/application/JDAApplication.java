package net.vadamdev.jdautils.application;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.managers.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 08/06/2023
 */
public class JDAApplication<T extends JDABot> {
    private final T jdaBot;
    private final Logger logger;

    private final Map<String, Consumer<T>> commands;

    public JDAApplication(@Nonnull T jdaBot, @Nullable Logger logger) {
        this.jdaBot = jdaBot;

        if(logger == null)
            this.logger = LoggerFactory.getLogger(JDAApplication.class);
        else
            this.logger = logger;

        this.commands = new HashMap<>();
        addDefaultCommands();

        logger.info("-----------------------------------------------------");
        logger.info("JDA Utils by VadamDev (https://github.com/VadamDev)");
        logger.info("-----------------------------------------------------");
    }

    public void start() throws InterruptedException {
        logger.info("Starting " + jdaBot. getClass().getSimpleName() + "...");
        jdaBot.setup();

        Scanner scanner = new Scanner(System.in);
        do {
            try {
                String str = scanner.next();

                commands.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(str))
                        .findFirst().ifPresent(entry -> entry.getValue().accept(jdaBot));
            }catch(Exception ignored) {}
        }while(scanner.hasNext());
    }

    public void stop() {
        jdaBot.onDisable();
        System.exit(0);
    }

    public void registerCommand(String name, Consumer<T> command) {
        commands.put(name, command);
    }

    private void addDefaultCommands() {
        commands.put("help", jdaBot -> {
            logger.info("Current available commands:");

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
