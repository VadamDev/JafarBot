package net.vadamdev.jafarbot.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.commands.api.GuildLinkedCommand;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.Utils;

import java.time.Instant;

/**
 * @author VadamDev
 * @since 03/05/2025
 */
public class DashboardCommand extends GuildLinkedCommand {
    public DashboardCommand() {
        super("dashboard", "Envois le tableau de commande du bot. Permettant de modifier certains paramètres.");
        setRequiredPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(Member sender, SlashCommandInteractionEvent event) {
        final JDA jda = event.getJDA();

        event.deferReply().flatMap(hook -> jda.getRestPing().flatMap(restPing -> {
            return hook.editOriginalEmbeds(createDashboardEmbed(jda.getGatewayPing(), restPing));
        })).queue();
    }

    private MessageEmbed createDashboardEmbed(long gatewayPing, long restPing) {
        final Runtime runtime = Runtime.getRuntime();

        return EmbedUtils.defaultEmbed()
                .setTitle("JafarBot | Dashboard")

                .addField("⌛ Uptime", "> ``" + Utils.formatMsToDHMS(JafarBot.get().getUptimeMs()) + "``", true)
                .addField("\uD83D\uDCE6 Memory Usage", "> ``" + formatBytes(runtime.totalMemory() - runtime.freeMemory()) + " / " + formatBytes(runtime.maxMemory()) + "``", true)
                .addField("\uD83C\uDF10 Ping", "> ``Gateway Ping: " + gatewayPing + " ms``\n> ``REST Ping: " + restPing + " ms``", true)

                .addField("\uD83D\uDCBD Saved Profiles", "> ``" + JafarBot.get().getProfileManager().getProfiles().size() + "``", true)
                .addBlankField(true)
                .addField("\uD83E\uDE9D Webhook Logger", "> ``" + (JafarBot.getWebhookLogger().isReady() ? "✅" : "❌") + "``", true)

                .setTimestamp(Instant.now())
                .build();
    }

    private static String formatBytes(long bytes) {
        if(bytes < 1024)
            return bytes + " bytes";

        if(bytes < 1048576)
            return String.format("%.2f", bytes / 1024d) + " kB";

        if(bytes < 1073741824)
            return String.format("%.2f", bytes / 1048576d) + " MB";

        return String.format("%.2f", bytes / 1073741824d) + " GB";
    }
}
