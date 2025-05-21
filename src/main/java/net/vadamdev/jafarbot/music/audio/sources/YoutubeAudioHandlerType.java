package net.vadamdev.jafarbot.music.audio.sources;

import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.Web;
import net.vadamdev.dbk.framework.DBKFramework;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 21/05/2025
 */
public enum YoutubeAudioHandlerType {
    NONE,

    OAUTH(
            (youtube, section) -> {
                String token = section.getString("refreshToken");
                if(token == null || token.isBlank() || token.equals("REFRESH_TOKEN"))
                    token = null;

                youtube.useOauth2(token, false);

                DBKFramework.getScheduledExecutorMonoThread().scheduleAtFixedRate(() -> {
                    final YamlFile yamlFile = AudioSourcesLoader.YAML_FILE;
                    if(!yamlFile.exists())
                        return;

                    final String refreshToken = youtube.getOauth2Handler().getRefreshToken();
                    if(refreshToken == null)
                        return;

                    synchronized(yamlFile) {
                        final String storedToken = yamlFile.getString("sources.youtube.parameters.handler.oauth.refreshToken");
                        if(storedToken.equals(refreshToken))
                            return;

                        try {
                            yamlFile.set("sources.youtube.parameters.handler.oauth.refreshToken", refreshToken);
                            yamlFile.save();
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 5, 5, TimeUnit.MINUTES);
            },
            section -> section.addDefault("refreshToken", "REFRESH_TOKEN")
    ),

    POT(
            (youtube, section) -> Web.setPoTokenAndVisitorData(
                    section.getString("poToken"),
                    section.getString("visitorData")
            ),
            section -> {
                section.addDefault("poToken", "PO_TOKEN");
                section.addDefault("visitorData", "VISITOR_DATA");
            }
    );

    private final BiConsumer<YoutubeAudioSourceManager, ConfigurationSection> handler;
    @Nullable private final Consumer<ConfigurationSection> composer;

    YoutubeAudioHandlerType(BiConsumer<YoutubeAudioSourceManager, ConfigurationSection> handler, @Nullable Consumer<ConfigurationSection> composer) {
        this.handler = handler;
        this.composer = composer;
    }

    YoutubeAudioHandlerType() {
        this((youtube, section) -> {}, null);
    }

    public void handle(YoutubeAudioSourceManager youtube, ConfigurationSection handlerSection) {
        final ConfigurationSection section = handlerSection.getConfigurationSection(name().toLowerCase());
        if(section == null)
            return;

        handler.accept(youtube, section);
    }

    public static void composeAll(ConfigurationSection handlerSection) {
        for(YoutubeAudioHandlerType handler : values()) {
            if(handler.composer == null)
                continue;

            handler.composer.accept(handlerSection.createSection(handler.name().toLowerCase()));
        }
    }

    public static YoutubeAudioHandlerType of(String str) {
        if(str == null || str.isBlank())
            return NONE;

        try {
            return valueOf(str.toUpperCase());
        }catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
