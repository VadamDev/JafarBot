package net.vadamdev.jafarbot.commands;

import club.minnced.discord.webhook.WebhookClientBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.IWebhookContainerUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.vadamdev.dbk.framework.commands.GuildSlashCommand;
import net.vadamdev.dbk.framework.commands.annotations.AnnotationProcessor;
import net.vadamdev.dbk.framework.commands.annotations.CommandProcessor;
import net.vadamdev.jafarbot.ApplicationConfig;
import net.vadamdev.jafarbot.logger.WebhookLoggingHandler;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author VadamDev
 * @since 05/05/2025
 */
public class WebhookLoggerCommand extends GuildSlashCommand {
    private final ApplicationConfig appConfig;
    private final WebhookLoggingHandler webhookLogger;

    public WebhookLoggerCommand(ApplicationConfig appConfig, WebhookLoggingHandler webhookLogger) {
        super("webhook-logger", "Gère le webhook logger");
        setRequiredPermissions(Permission.ADMINISTRATOR);

        this.appConfig = appConfig;
        this.webhookLogger = webhookLogger;
    }

    @CommandProcessor(subCommand = "enable")
    private void enable(SlashCommandInteractionEvent event) {
        if(appConfig.WEBHOOK_ENABLED || webhookLogger.isReady()) {
            event.replyEmbeds(EmbedUtils.defaultError("Le webhook logger est déjà activé !").build()).queue();
            return;
        }

        try {
            appConfig.setValue("WEBHOOK_ENABLED", true);
            appConfig.save();

            webhookLogger.init(appConfig.WEBHOOK_URL);

            event.replyEmbeds(EmbedUtils.defaultSuccess("Le webhook logger est maintenant activé !").build()).queue();
        }catch (IOException e) {
            e.printStackTrace();
            event.replyEmbeds(EmbedUtils.defaultError("Une erreur est survenue!").build()).queue();
        }
    }

    @CommandProcessor(subCommand = "disable")
    private void disable(SlashCommandInteractionEvent event) {
        if(!appConfig.WEBHOOK_ENABLED || !webhookLogger.isReady()) {
            event.replyEmbeds(EmbedUtils.defaultError("Le webhook logger est déjà désactivé !").build()).queue();
            return;
        }

        try {
            appConfig.setValue("WEBHOOK_ENABLED", false);
            appConfig.save();

            webhookLogger.shutdown();

            event.replyEmbeds(EmbedUtils.defaultSuccess("Le webhook logger est maintenant désactivé !").build()).queue();
        }catch (IOException e) {
            e.printStackTrace();
            event.replyEmbeds(EmbedUtils.defaultError("Une erreur est survenue!").build()).queue();
        }
    }

    @CommandProcessor(subCommand = "set")
    private void set(SlashCommandInteractionEvent event) {
        final GuildChannelUnion union = event.getOption("channel", OptionMapping::getAsChannel);
        if(union == null || !union.getType().equals(ChannelType.TEXT)) {
            event.replyEmbeds(EmbedUtils.defaultError("Le salon doit être un salon textuel !").build()).queue();
            return;
        }

        final TextChannel channel = union.asTextChannel();

        //Was painful, you cant chain REST requests inside RestAction<?> flatMap...
        event.deferReply().flatMap(hook -> appConfig.retrieveLogWebhook().flatMap(optWebhook -> {
            final Webhook webhook = optWebhook.orElse(null);

            if(webhook != null && webhook.getChannel().getId().equals(channel.getId()))
                return hook.editOriginalEmbeds(EmbedUtils.defaultError("Le logger est déjà définis sur ce salon !").build());
            else {
                if(webhook != null)
                    webhook.delete().queue();

                return channel.createWebhook("JafarBot Webhook Logger").setAvatar(Utils.retrieveBotAvatarAsIcon()).flatMap(newWebhook -> {
                    try {
                        appConfig.updateWebhookLogger(newWebhook);
                        webhookLogger.init(WebhookClientBuilder.fromJDA(newWebhook));

                        return hook.editOriginalEmbeds(EmbedUtils.defaultSuccess("Les logs seront maintenant envoyé dans le salon " + channel.getAsMention()).build());
                    }catch (IOException e) {
                        e.printStackTrace();
                        return hook.editOriginalEmbeds(EmbedUtils.defaultError("Une erreur est survenue lors de la mise à jour de la webhook !").build());
                    }
                });
            }
        })).queue();
    }

    @CommandProcessor(subCommand = "status")
    private void status(SlashCommandInteractionEvent event) {
        event.deferReply().flatMap(hook -> appConfig.retrieveLogWebhook().flatMap(optWebhook -> {
            final IWebhookContainerUnion channel = optWebhook.map(Webhook::getChannel).orElse(null);

            return hook.editOriginalEmbeds(EmbedUtils.defaultEmbed()
                    .addField("Status", "> " + Utils.formatBoolean(appConfig.WEBHOOK_ENABLED && webhookLogger.isReady()), true)
                    .addBlankField(true)
                    .addField("Salon", "> " + (channel != null ? channel.getAsMention() : "``Aucun salon défini``"), true)
                    .build());
        })).queue();
    }

    @NotNull
    @Override
    public SlashCommandData createCommandData() {
        return super.createCommandData().addSubcommands(
                new SubcommandData("enable", "Active le webhook logger"),
                new SubcommandData("disable", "Désactive le webhook logger"),
                new SubcommandData("set", "Définit le salon dans lequel les logs seront envoyés et active le webhook logger").addOptions(
                        new OptionData(OptionType.CHANNEL, "channel", "Salon dans lequel les logs seront envoyé", true)
                ),
                new SubcommandData("status", "Affiche le statut du webhook logger")
        );
    }

    @Override
    public void execute(Member sender, SlashCommandInteractionEvent event) {
        AnnotationProcessor.processAnnotations(event, this);
    }
}
