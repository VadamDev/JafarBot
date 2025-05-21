package net.vadamdev.jafarbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.music.audio.AudioLoadResultAdapter;
import net.vadamdev.jafarbot.music.audio.sources.AudioSourcesLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author VadamDev
 * @since 06/06/2024
 */
public final class GuildMusicManager {
    private final Map<String, GuildMusicBridge> bridges;

    private final AudioPlayerManager playerManager;
    private final InactivityChecker inactivityChecker;

    public GuildMusicManager(JDA jda) {
        this.bridges = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        this.inactivityChecker = new InactivityChecker(jda, this);

        try {
            AudioSourcesLoader.load(playerManager);

            for(AudioSourceManager sourceManager : AudioSourcesLoader.getAudioSources())
                playerManager.registerSourceManager(sourceManager);
        }catch (IOException e) {
            JafarBot.getLogger().error("An error occurred while loading audio sources:", e);
            System.exit(-1);
        }
    }

    CompletableFuture<Boolean> loadTrack(GuildMusicBridge bridge, String source, Function<CompletableFuture<Boolean>, AudioLoadResultAdapter> hookingFunc) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>(); //future that tells if the bot should connect (true = track was found)
        final AudioLoadResultAdapter hook = hookingFunc.apply(future);

        playerManager.loadItemOrdered(bridge, source, new AudioLoadResultAdapter() {
            @Override
            public void trackLoaded(AudioTrack track) {
                bridge.trackScheduler().add(track);
                hook.trackLoaded(track);

                future.complete(true);
            }

            @Override
            public void onPlaylistLoaded(AudioPlaylist playlist) {
                bridge.trackScheduler().add(playlist.getTracks());
                future.complete(true);
            }

            @Override
            public void onSearchResultFound(AudioPlaylist playlist) {
                //Delegated to the hook.
                //THE HOOK NEEDS TO COMPLETE THE FUTURE OR IT WILL CAUSE A DEADLOCK
            }

            @Override
            public void noMatches() {
                hook.noMatches();
                future.complete(false);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                hook.loadFailed(exception);
                future.complete(false);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioLoadResultAdapter.super.playlistLoaded(playlist); //Delegate the work to onPlaylistLoaded() and onSearchResultFound()
                hook.playlistLoaded(playlist);
            }
        });

        return future;
    }

    @NotNull
    public GuildMusicBridge getOrCreate(Guild guild) {
        return bridges.computeIfAbsent(guild.getId(), k -> new GuildMusicBridge(guild, playerManager.createPlayer(), inactivityChecker));
    }

    public boolean remove(Guild guild) {
        final GuildMusicBridge bridge = bridges.remove(guild.getId());
        if(bridge == null)
            return false;

        bridge.audioPlayer().destroy();
        return true;
    }
}
