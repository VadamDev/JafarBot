package net.vadamdev.jafarbot.music.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

/**
 * @author VadamDev
 * @since 15/06/2024
 */
public interface AudioLoadResultAdapter extends AudioLoadResultHandler {
    @Override
    default void playlistLoaded(AudioPlaylist playlist) {
        if(!playlist.isSearchResult())
            onPlaylistLoaded(playlist);
        else
            onSearchResultFound(playlist);
    }

    void onPlaylistLoaded(AudioPlaylist playlist);
    void onSearchResultFound(AudioPlaylist playlist);
}
