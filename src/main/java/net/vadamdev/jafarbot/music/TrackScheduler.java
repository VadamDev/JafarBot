package net.vadamdev.jafarbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import net.vadamdev.jafarbot.Main;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final Guild guild;
    private final AudioPlayer player;
    public BlockingQueue<AudioTrack> queue;

    public boolean repeating = false;

    public TrackScheduler(Guild guild, AudioPlayer player) {
        this.guild = guild;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(!endReason.mayStartNext)
            return;

        if(repeating)
            this.player.startTrack(track.makeClone(), false);
        else
            nextTrack();
    }

    public void queue(AudioTrack track) {
        if(!player.startTrack(track, true))
            queue.offer(track);
    }

    public void nextTrack() {
        nextTrack(1);
    }

    public void nextTrack(int index) {
        if(queue.isEmpty()) {
            GuildMusicManager musicManager = Main.jafarBot.getPlayerManager().getMusicManager();

            if(musicManager.getInterfaceMessage() != null) {
                musicManager.getInterfaceMessage().delete().queue();
                musicManager.setInterfaceMessage(null);
            }

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

            player.startTrack(queue.poll(), false);
        }
    }

    public void clear() {
        queue.clear();
    }

    public AudioTrack getNextTrack() {
        try {
            return new ArrayList<>(queue).get(0);
        }catch(Exception ignored) {
            return null;
        }
    }
}
