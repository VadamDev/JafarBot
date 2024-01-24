package net.vadamdev.jdautils.application;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.vadamdev.jdautils.JDAUtils;
import net.vadamdev.jdautils.commands.Command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a JDA bot
 *
 * @author VadamDev
 * @since 30/03/2023
 */
public class JDABot {
    private final JDABuilder jdaBuilder;
    private final String commandPrefix;

    protected JDA jda;
    private JDAUtils jdaUtils;

    private String AVATAR_URL;

    public JDABot(String token, @Nullable String commandPrefix) {
        this.jdaBuilder = computeBuilder(JDABuilder.createDefault(token));
        this.commandPrefix = commandPrefix;
    }

    void setup() throws InterruptedException {
        jda = jdaBuilder.build();
        jda.awaitReady();

        jdaUtils = new JDAUtils(jda, commandPrefix);

        AVATAR_URL = jda.getSelfUser().getAvatarUrl();

        onEnable();

        jdaUtils.finishCommandRegistry();
    }

    public void onEnable() {}
    public void onDisable() {}

    /*
       JDA Listeners
     */
    protected void registerListeners(Object... listeners) {
        jda.addEventListener(listeners);
    }

    /*
       JDAUtils Commands
     */
    protected void registerCommands(Command... commands) {
        for(Command command : commands)
            jdaUtils.registerCommand(command);
    }

    /*
       JDA Builder Bridge
     */

    @Nonnull
    protected JDABuilder computeBuilder(JDABuilder jdaBuilder) {
        return jdaBuilder;
    }

    /*
       Getters
     */

    public JDA getJda() {
        return jda;
    }

    public String getAvatarURL() {
        return AVATAR_URL;
    }
}
