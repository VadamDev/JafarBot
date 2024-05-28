package net.vadamdev.jafarbot.music;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jafarbot.utils.Utils;

import java.util.List;

public class PlayerManager {
    private static final AudioSourceManager[] sourceManagers = new AudioSourceManager[] {
            new YoutubeAudioSourceManager(), //Lavalink's YoutubeAudioSourceManager
            SoundCloudAudioSourceManager.createDefault(),
            new BandcampAudioSourceManager(),
            new VimeoAudioSourceManager(),
            new TwitchStreamAudioSourceManager(),
            new BeamAudioSourceManager(),
            new GetyarnAudioSourceManager(),
            new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY),

            new LocalAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY)
    };

    private final AudioPlayerManager audioPlayerManager;
    private final MusicManager musicManager;

    public PlayerManager(Guild guild) {
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        audioPlayerManager.registerSourceManagers(sourceManagers);

        this.musicManager = new MusicManager(guild, audioPlayerManager);
        guild.getAudioManager().setSendingHandler(musicManager.getAudioHandler());
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
