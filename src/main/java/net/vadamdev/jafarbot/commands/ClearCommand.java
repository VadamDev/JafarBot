package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.vadamdev.jafarbot.commands.api.GuildLinkedCommand;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class ClearCommand extends GuildLinkedCommand {
    public ClearCommand() {
        super("clear", "Supprime un nombre de messages dans le salon ou la commande est exécuté");
        setRequiredPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(Member sender, SlashCommandInteractionEvent event) {
        final int amount = Math.clamp(event.getOption("amount", 1, OptionMapping::getAsInt), 1, 100);
        final MessageChannel textChannel = event.getChannel();

        event.deferReply(true).flatMap(hook -> textChannel.getHistory().retrievePast(amount).flatMap(messages -> {
            textChannel.purgeMessages(messages);
            return hook.editOriginalEmbeds(EmbedUtils.defaultSuccess(amount + " messages ont été supprimer !").setTitle("JafarBot - Clear").build());
        })).queue();
    }

    @NotNull
    @Override
    public SlashCommandData createCommandData() {
        return super.createCommandData().addOptions(
                new OptionData(OptionType.INTEGER, "amount", "Nombre de messages à supprimer", true)
                        .setRequiredRange(1, 100)
        );
    }
}
