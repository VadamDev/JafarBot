package net.vadamdev.jdautils.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.vadamdev.jdautils.commands.data.ICommandData;
import net.vadamdev.jdautils.commands.data.SlashCmdData;
import net.vadamdev.jdautils.commands.data.TextCmdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author VadamDev
 * @since 17/10/2022
 */
public final class CommandHandler {
    public static Consumer<Message> PERMISSION_ACTION = message -> message.reply("You don't have enough permission.").queue();
    public static boolean LOGGING = true;

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final List<Command> commands;
    private final String commandPrefix;

    public CommandHandler(String commandPrefix, JDA jda) {
        this.commands = new ArrayList<>();
        this.commandPrefix = commandPrefix;

        if(commandPrefix != null && !jda.getGatewayIntents().contains(GatewayIntent.MESSAGE_CONTENT))
            logger.warn("MESSAGE_CONTENT intent is currently not enabled, legacy commands are disabled!");
    }

    /*
       Handle Events
     */

    public void handleMessageReceive(@Nonnull MessageReceivedEvent event) {
        if(commandPrefix == null)
            return;

        final String messageContent = event.getMessage().getContentRaw();

        if(messageContent.startsWith(commandPrefix)) {
            final String[] args = messageContent.split(" ");
            final String commandName = args[0].replace(commandPrefix, "");

            commands.stream()
                    .filter(command -> command.check(commandName))
                    .findFirst().ifPresent(command -> {
                        if(command instanceof ISlashCommand && ((ISlashCommand) command).isSlashOnly())
                            return;

                        final Member member = event.getMember();
                        if(command.getPermission() != null && !member.hasPermission(command.getPermission())) {
                            PERMISSION_ACTION.accept(event.getMessage());
                            return;
                        }

                        final TextCmdData commandData = new TextCmdData(event, args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length));

                        logCommandExecution(member, commandData, commandName);
                        command.execute(member, commandData);
                    });
        }
    }

    public void handleSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        commands.stream()
                .filter(ISlashCommand.class::isInstance)
                .filter(command -> command.check(event.getName()))
                .findFirst().ifPresent(command -> {
                    if(event.getGuild() == null) {
                        event.reply("❌ Slash commands only work in guilds!").setEphemeral(true).queue();
                        return;
                    }

                    final SlashCmdData commandData = new SlashCmdData(event);
                    final Member member = event.getMember();

                    logCommandExecution(member, commandData, event.getFullCommandName());
                    command.execute(member, commandData);
                });
    }

    /*
       Register
     */

    public void registerCommand(Command command) {
        commands.add(command);
    }

    public void registerSlashCommands(JDA jda) {
        final List<CommandData> commands = this.commands.stream()
                .filter(ISlashCommand.class::isInstance)
                .map(command -> {
                    final CommandData commandData = ((ISlashCommand) command).createSlashCommand();

                    if(command.getPermission() != null)
                        commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(command.getPermission()));
                    else
                        commandData.setDefaultPermissions(DefaultMemberPermissions.ENABLED);

                    return commandData;
                }).collect(Collectors.toList());

        jda.updateCommands().addCommands(commands).queue();
    }

    /*
       Utils
     */

    private void logCommandExecution(Member sender, ICommandData commandData, String commandName) {
        if(!LOGGING)
            return;

        final StringBuilder formattedCommand = new StringBuilder(commandName);

        if(commandData.getType().equals(ICommandData.Type.TEXT)) {
            for(String arg : commandData.castOrNull(TextCmdData.class).getArgs()) {
                formattedCommand.append(" " + arg);
            }
        }else if(commandData.getType().equals(ICommandData.Type.SLASH)) {
            final SlashCommandInteractionEvent event = commandData.castOrNull(SlashCmdData.class).getEvent();
            event.getOptions().forEach(optionMapping -> formattedCommand.append(" (" + optionMapping.getName() + ": " + formatOptionMapping(optionMapping) + ")"));
        }

        logger.info(sender.getUser().getName() + " in " + sender.getGuild().getId() + " issued command: " + formattedCommand);
    }

    private String formatOptionMapping(OptionMapping optionMapping) {
        switch(optionMapping.getType()) {
            case STRING:
                return optionMapping.getAsString() + " (string)";
            case INTEGER:
                return optionMapping.getAsInt() + " (integer)";
            case BOOLEAN:
                return optionMapping.getAsBoolean() + " (boolean)";
            case USER:
                return optionMapping.getAsUser().getEffectiveName() + " (user)";
            case CHANNEL:
                return optionMapping.getAsChannel().getName() + " (channel)";
            case ROLE:
                return optionMapping.getAsRole().getName() + " (role)";
            case MENTIONABLE:
                return optionMapping.getAsMentionable().getAsMention() + " (mention)";
            case ATTACHMENT:
                final Message.Attachment attachment = optionMapping.getAsAttachment();
                return attachment.getFileName() + "." + attachment.getFileExtension() + " (attachment)";
            default:
                return "UNKNOWN_OPTION";
        }
    }
}
