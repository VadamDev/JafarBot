package net.vadamdev.jafarbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.vadamdev.jafarbot.Main;

public class GuildMusicManager {
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final LavaAudioHandler audioHandler;

    private Message interfaceMessage;

    public GuildMusicManager(Guild guild, AudioPlayerManager audioPlayerManager) {
        this.audioPlayer = audioPlayerManager.createPlayer();
        this.audioPlayer.setVolume(Main.jafarBot.mainConfig.MUSIC_DEFAULT_VOLUME);

        this.trackScheduler = new TrackScheduler(guild, audioPlayer);
        this.audioPlayer.addListener(trackScheduler);

        this.audioHandler = new LavaAudioHandler(audioPlayer);
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public LavaAudioHandler getAudioHandler() {
        return audioHandler;
    }

    public Message getInterfaceMessage() {
        return interfaceMessage;
    }

    public void setInterfaceMessage(Message interfaceMessage) {
        this.interfaceMessage = interfaceMessage;
    }
}
