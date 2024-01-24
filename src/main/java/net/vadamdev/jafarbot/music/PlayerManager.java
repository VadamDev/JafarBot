package net.vadamdev.jafarbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jafarbot.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerManager {
    private final AudioPlayerManager audioPlayerManager;
    private final MusicManager musicManager;

    public PlayerManager(Guild guild) {
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        this.musicManager = new MusicManager(guild, audioPlayerManager);
        guild.getAudioManager().setSendingHandler(musicManager.getAudioHandler());

        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public void loadAndPlay(InteractionHook interactionHook, String source) {
        audioPlayerManager.loadItemOrdered(musicManager, source, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.getTrackScheduler().queue(track);

                final AudioTrackInfo trackInfo = track.getInfo();
                interactionHook.sendMessageEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Musique")
                        .setDescription(
                                "> **Ajout de la musique:**\n" +
                                "> " + trackInfo.title
                        )
                        .setThumbnail(Utils.retrieveVideoThumbnail(trackInfo.uri))
                        .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();

                final StringBuilder description = new StringBuilder(
                        "Ajout de la playlist: __" + playlist.getName() + "__\n" +
                        "\n"
                );

                for(int i = 0; i < tracks.size(); i++) {
                    if(i >= 10) {
                        description.append("- ... *(+" + (tracks.size() - 10) + ")*\n");
                        break;
                    }

                    description.append("- " + playlist.getTracks().get(i).getInfo().title + "\n");
                }

                for(AudioTrack track : tracks)
                    musicManager.getTrackScheduler().queue(track);

                interactionHook.sendMessageEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Musique")
                        .setDescription(description.toString())
                        .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();
            }

            @Override
            public void noMatches() {
                interactionHook.sendMessageEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Musique")
                        .setDescription(source + " n'a pas pus être trouvé !")
                        .setColor(JafarEmbed.ERROR_COLOR).build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                interactionHook.sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("JafarBot - Musique")
                        .setDescription("Erreur lors de la lecture ! (" + exception.getMessage() + ")")
                        .setColor(JafarEmbed.ERROR_COLOR).build()).queue();
            }
        });
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}
