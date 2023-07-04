package net.vadamdev.jdautils.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.vadamdev.jdautils.commands.data.impl.SlashCommandData;
import net.vadamdev.jdautils.commands.data.impl.TextCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 17/10/2022
 */
public final class CommandHandler extends ListenerAdapter {
    public static Consumer<Message> PERMISSION_ACTION = (message -> message.reply("You don't have enough permission.").queue());

    private final JDA jda;

    private final List<Command> commands;
    private final String commandPrefix;

    public CommandHandler(JDA jda, String commandPrefix) {
        this.jda = jda;
        this.commands = new ArrayList<>();
        this.commandPrefix = commandPrefix;
    }

    /*
       Legacy Commands
     */

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final String messageContent = event.getMessage().getContentRaw();

        if(messageContent.startsWith(commandPrefix)) {
            final String[] args = messageContent.split(" ");

            commands.stream()
                    .filter(command -> command.check(args[0].replace(commandPrefix, "")))
                    .findFirst().ifPresent(command -> {
                        if(command instanceof ISlashCommand && ((ISlashCommand) command).isSlashOnly())
                            return;

                        final Member member = event.getMember();

                        if(command.getPermission() != null && !member.hasPermission(command.getPermission())) {
                            PERMISSION_ACTION.accept(event.getMessage());
                            return;
                        }

                        command.execute(member, new TextCommandData(event, Arrays.copyOfRange(args, 1, args.length)));
                    });
        }
    }

    /*
       Slash Commands
     */

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        commands.stream()
                .filter(ISlashCommand.class::isInstance)
                .filter(command -> command.check(event.getName()))
                .findFirst().ifPresent(command ->
                        command.execute(event.getMember(), new SlashCommandData(event))
                );
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        commands.stream()
                .filter(ISlashCommand.class::isInstance)
                .filter(command -> command.check(event.getName()))
                .findFirst().ifPresent(command -> ((ISlashCommand) command).onAutoCompleteEvent(event));
    }

    public void registerCommand(Command command) {
        commands.add(command);
    }

    public void registerCommands() {
        final List<CommandData> commands = new ArrayList<>();

        for (Command command : this.commands) {
            if(!(command instanceof ISlashCommand))
                continue;

            final CommandData commandData = ((ISlashCommand) command).createSlashCommand();

            if(command.getPermission() != null)
                commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.getPermission()));
            else
                commandData.setDefaultPermissions(DefaultMemberPermissions.ENABLED);

            commands.add(commandData);
        }

        jda.updateCommands().addCommands(commands).queue();
    }
}
