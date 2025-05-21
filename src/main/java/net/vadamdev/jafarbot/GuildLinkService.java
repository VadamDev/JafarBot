package net.vadamdev.jafarbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.vadamdev.dbk.framework.DBKFramework;
import net.vadamdev.dbk.framework.interactive.entities.buttons.InteractiveButton;
import net.vadamdev.dbk.framework.interactive.entities.buttons.LightweightButton;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 29/03/2025
 */
public class GuildLinkService {
    private static final Logger logger = LoggerFactory.getLogger(GuildLinkService.class);

    private static final Permission LINK_PERMISSION = Permission.ADMINISTRATOR;
    private static final Consumer<ButtonInteractionEvent> MISSING_PERMISSION_ACTION = event -> event
            .reply("You are missing the required permission (``" + LINK_PERMISSION.name() + "``) to use this button.").setEphemeral(true).queue();

    private final ApplicationConfig appConfig;
    private final List<Consumer<Guild>> thingsToExecuteOnceLinked;

    private Optional<String> linkedGuild;

    private JDA jda;

    private LightweightButton linkButton, cancelButton;

    public GuildLinkService(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
        this.thingsToExecuteOnceLinked = new ArrayList<>();

        this.linkedGuild = Optional.empty();
    }

    public GuildLinkService init(JDA jda) {
        this.jda = jda;
        jda.addEventListener(new Listener());

        final Guild storedGuild = (appConfig.GUILD_ID != null && !appConfig.GUILD_ID.isBlank()) ? jda.getGuildById(appConfig.GUILD_ID) : null;

        if(storedGuild != null) {
            for(Guild guild : jda.getGuilds()) {
                if(guild.getId().equals(storedGuild.getId()))
                    continue;

                guild.leave().queue();
                logger.info("Guild link request already completed, leaving guild: " + guild.getName() + " (" + guild.getId() + ")");
            }

            link(storedGuild);
        }else {
            try {
                appConfig.setValue("GUILD_ID", "");
                appConfig.save();
            }catch (IOException e) {
                e.printStackTrace();
            }

            logger.info("No linked guild found, sending link request messages...");

            //Create buttons
            linkButton = InteractiveButton.of(ButtonStyle.SUCCESS)
                    .addRequiredPermission(MISSING_PERMISSION_ACTION, LINK_PERMISSION)
                    .label("Link")
                    .emoji(Emoji.fromUnicode("\uD83D\uDD0C"))
                    .action((event, invalidatable) -> {
                        if(linkedGuild.isPresent()) {
                            event.reply("The bot is already linked to a server.").setEphemeral(true).complete();
                            event.getMessage().delete().complete();
                            event.getGuild().leave().queue();
                            return;
                        }

                        final Guild guild = event.getGuild();

                        event.reply("Successfully linked to " + guild.getName() + " (" + guild.getId() + ") !").setEphemeral(true).complete();
                        event.getMessage().delete().complete();
                        link(guild);
                    }).lightweight(jda);

            cancelButton = InteractiveButton.of(ButtonStyle.DANGER)
                    .addRequiredPermission(MISSING_PERMISSION_ACTION, LINK_PERMISSION)
                    .label("Cancel")
                    .emoji(Emoji.fromUnicode("\uD83D\uDCA5"))
                    .action((event, invalidatable) -> {
                        event.reply("Link request cancelled. Leaving the server...").setEphemeral(true).complete();
                        event.getMessage().delete().complete();
                        event.getGuild().leave().queue();
                    }).lightweight(jda);

            //Send link request messages to all guilds
            for(Guild guild : jda.getGuilds()) {
                final TextChannel systemChannel = guild.getSystemChannel();
                if(systemChannel == null || !systemChannel.canTalk()) {
                    guild.leave().queue();
                    logger.warn("Failed to find system channel for guild: " + guild.getName() + " (" + guild.getId() + ")");
                    continue;
                }

                sendLinkMessage(systemChannel);
            }
        }

        return this;
    }

    private void link(Guild guild) {
        linkedGuild = Optional.of(guild.getId());

        try {
            appConfig.setValue("GUILD_ID", guild.getId());
            appConfig.save();
        }catch (IOException e) {
            e.printStackTrace();
        }

        for(Consumer<Guild> action : thingsToExecuteOnceLinked)
            action.accept(guild);

        thingsToExecuteOnceLinked.clear();
    }

    public void onLinked(Consumer<Guild> action) {
        if(linkedGuild.isPresent())
            action.accept(jda.getGuildById(linkedGuild.get()));
        else
            thingsToExecuteOnceLinked.add(action);
    }

    public void ifLinked(Consumer<Guild> action) {
        linkedGuild.ifPresent(guildId -> action.accept(jda.getGuildById(guildId)));
    }

    public Guild waitCompleteLink() throws InterruptedException {
        if(linkedGuild.isPresent())
            return jda.getGuildById(linkedGuild.get());

        //TODO: actually add a timeout
        while(linkedGuild.isEmpty())
            Thread.sleep(10);

        return jda.getGuildById(linkedGuild.get());
    }

    public Optional<String> getLinkedGuildId() {
        return linkedGuild;
    }

    public Optional<Guild> getLinkedGuild() {
        return linkedGuild.map(guildId -> jda.getGuildById(guildId));
    }

    private void sendLinkMessage(TextChannel channel) {
        channel.sendMessageEmbeds(EmbedUtils.defaultEmbed()
                .setColor(0xFFFFFF)
                .setTitle("``\uD83D\uDD0C`` | Guild Link Request")
                .setDescription(
                        """
                        This bot is meant to be only usable on one server.
                        Please link this bot to your server by using the button below.
                        
                        ⚠️ Note that this action cannot be undone !
                        """
                ).setFooter("This is an automated message.").build()).setActionRow(linkButton, cancelButton).queue();
    }

    /*
        Event Listener
     */

    private final class Listener extends ListenerAdapter {
        @Override
        public void onGuildJoin(GuildJoinEvent event) {
            final Guild guild = event.getGuild();

            if(linkedGuild.isPresent() && !linkedGuild.get().equals(guild.getId())) {
                logger.info("Guild link request already completed, leaving guild: " + guild.getName() + " (" + guild.getId() + ")");
                guild.leave().queue();

                return;
            }

            final TextChannel systemChannel = guild.getSystemChannel();
            if(systemChannel == null || !systemChannel.canTalk()) {
                logger.warn("Failed to find system channel for guild: " + guild.getName() + " (" + guild.getId() + ")");
                guild.leave().queue();

                return;
            }

            sendLinkMessage(systemChannel);
        }

        @Override
        public void onGuildLeave(GuildLeaveEvent event) {
            final Guild guild = event.getGuild();
            if(linkedGuild.isEmpty() || !linkedGuild.get().equals(guild.getId()))
                return;

            logger.info("Linked guild left: " + guild.getName() + " (" + guild.getId() + ")");
            logger.info("Stopping the bot automatically...");

            linkedGuild = Optional.empty();

            try {
                appConfig.setValue("GUILD_ID", "");
                appConfig.setValue("WEBHOOK_LOGGER", false);
                appConfig.setValue("WEBHOOK_URL", "");

                appConfig.save();
            }catch (IOException e) {
                e.printStackTrace();
            }

            DBKFramework.stop();
        }
    }
}
