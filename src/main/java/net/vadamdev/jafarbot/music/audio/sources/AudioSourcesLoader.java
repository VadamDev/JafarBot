package net.vadamdev.jafarbot.music.audio.sources;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.vadamdev.jafarbot.JafarBot;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VadamDev
 * @since 15/07/2024
 */
public final class AudioSourcesLoader {
    private AudioSourcesLoader() {}

    static final YamlFile YAML_FILE = new YamlFile("./sources.yml");

    private static final List<AudioSourceManager> AUDIO_SOURCES = new ArrayList<>();
    public static Collection<AudioSourceManager> getAudioSources() { return AUDIO_SOURCES; }

    private static final List<Command.Choice> SEARCH_CHOICES = new ArrayList<>();
    public static Collection<Command.Choice> getSearchChoices() { return SEARCH_CHOICES; }

    public static void load(AudioPlayerManager playerManager) throws IOException {
        if(!YAML_FILE.exists())
            YAML_FILE.createNewFile();
        else
            YAML_FILE.load();

        for(AudioSources audioSource : AudioSources.values()) {
            final String path = "sources." + audioSource.name().toLowerCase();

            boolean enabled;
            ConfigurationSection parameters;

            if(YAML_FILE.isSet(path)) {
                enabled = YAML_FILE.getBoolean(path + ".enabled");
                parameters = YAML_FILE.getConfigurationSection(path + ".parameters");
            }else {
                enabled = audioSource.isDefaultEnabled();
                parameters = YAML_FILE.createSection(path + ".parameters");

                YAML_FILE.addDefault(path + ".enabled", enabled);
                audioSource.compose(parameters);
            }

            if(!enabled)
                continue;

            final AudioSourceManager sourceManager = audioSource.parse(playerManager, parameters);
            if(sourceManager == null)
                continue;

            if(audioSource.getSearchPrefix() != null && SEARCH_CHOICES.size() < 10) {
                final StringBuilder name = new StringBuilder();
                for(String str : audioSource.name().toLowerCase().split("_"))
                    name.append(str.substring(0, 1).toUpperCase() + str.substring(1) + " ");

                name.delete(name.length() - 1, name.length());

                SEARCH_CHOICES.add(new Command.Choice(name.toString(), audioSource.getSearchPrefix()));
            }

            AUDIO_SOURCES.add(sourceManager);
        }

        JafarBot.getLogger().info("Loaded " + AUDIO_SOURCES.size() + " audio sources !");

        YAML_FILE.save();
    }
}
