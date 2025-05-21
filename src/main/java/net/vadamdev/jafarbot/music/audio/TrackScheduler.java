package net.vadamdev.jafarbot.music.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import net.vadamdev.jafarbot.music.InactivityChecker;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author VadamDev
 * @since 06/06/2024
 */
public class TrackScheduler extends AudioEventAdapter {
    private final String guildId;
    private final InactivityChecker inactivityChecker;

    private final AudioPlayer audioPlayer;
    private Queue<AudioTrack> queue;

    public boolean repeatCurrentTrack;

    public TrackScheduler(String guildId, InactivityChecker inactivityChecker, AudioPlayer audioPlayer) {
        this.guildId = guildId;
        this.inactivityChecker = inactivityChecker;

        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();

        this.audioPlayer.addListener(this);
    }

    public void startNextTrack() {
        if(queue.isEmpty()) {
            inactivityChecker.add(guildId);
            return;
        }

        audioPlayer.startTrack(queue.poll(), false);
        inactivityChecker.remove(guildId);
    }

    public void skipTracks(int amount) {
        if(amount > 1) {
            for(int i = 0; i < amount - 1; i++)
                queue.poll();
        }

        startNextTrack();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(!endReason.mayStartNext)
            return;

        if(repeatCurrentTrack)
            audioPlayer.startTrack(track.makeClone(), false);
        else
            startNextTrack();
    }

    public void add(Collection<AudioTrack> tracks) {
        final boolean wasEmpty = queue.isEmpty();

        queue.addAll(tracks);

        if(wasEmpty && !isPlaying())
            startNextTrack();
    }

    public void add(AudioTrack track) {
        final boolean wasEmpty = queue.isEmpty();

        queue.add(track);

        if(wasEmpty && !isPlaying())
            startNextTrack();
    }

    public void shuffleQueue() {
        final List<AudioTrack> tracksCopy = new ArrayList<>(queue);
        Collections.shuffle(tracksCopy);
        queue = new ConcurrentLinkedQueue<>(tracksCopy);
    }

    public void clearQueue() {
        queue.clear();
    }

    public void clear() {
        queue.clear();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public boolean isPlaying() {
        final AudioTrack track = audioPlayer.getPlayingTrack();
        return track != null && track.getState().equals(AudioTrackState.PLAYING);
    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    public List<AudioTrack> getUnmodifiableQueue() {
        return List.copyOf(queue);
    }
}
