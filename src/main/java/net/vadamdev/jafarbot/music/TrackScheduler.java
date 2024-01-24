package net.vadamdev.jafarbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final Guild guild;
    private final AudioPlayer audioPlayer;
    public BlockingQueue<AudioTrack> queue;

    public boolean repeating = false;

    public TrackScheduler(Guild guild, AudioPlayer audioPlayer) {
        this.guild = guild;
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(!endReason.mayStartNext)
            return;

        if(repeating)
            this.audioPlayer.startTrack(track.makeClone(), false);
        else
            nextTrack();
    }

    public void queue(AudioTrack track) {
        if(!audioPlayer.startTrack(track, true))
            queue.offer(track);
    }

    public void nextTrack() {
        nextTrack(1);
    }

    public void nextTrack(int index) {
        if(queue.isEmpty()) {
            final AudioManager audioManager = guild.getAudioManager();

            if(audioManager.getConnectedChannel() != null)
                audioManager.closeAudioConnection();
        }else {
            if(index > queue.size() - 1)
                return;

            if(index > 1) {
                for(int i = 0; i < index - 1; i++) {
                    if(queue.poll() == null)
                        break;
                }
            }

            audioPlayer.startTrack(queue.poll(), false);
        }
    }

    public void clear() {
        queue.clear();
    }
}
