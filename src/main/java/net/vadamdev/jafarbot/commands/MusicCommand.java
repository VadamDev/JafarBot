package net.vadamdev.jafarbot.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.music.MusicManager;
import net.vadamdev.jafarbot.music.TrackScheduler;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jafarbot.utils.Utils;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import net.vadamdev.jdautils.commands.data.SlashCmdData;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author VadamDev
 * @since 14/07/2023
 */
public class MusicCommand extends Command implements ISlashCommand {
    public MusicCommand() {
        super("music");
    }

    @Override
    public void execute(@Nonnull Member sender, @Nonnull ICommandData commandData) {
        final SlashCommandInteractionEvent event = commandData.castOrNull(SlashCmdData.class).getEvent();

        final GuildVoiceState senderVoiceState = sender.getVoiceState();
        if(!senderVoiceState.inAudioChannel()) {
            event.replyEmbeds(new JafarEmbed()
                    .setTitle("JafarBot - Musique")
                    .setDescription("Vous devez être dans un salon vocal pour utiliser cette commande !")
                    .setColor(JafarEmbed.ERROR_COLOR).build()).queue();

            return;
        }

        final AudioManager audioManager = event.getGuild().getAudioManager();
        if(audioManager.isConnected() && !audioManager.getConnectedChannel().getId().equals(senderVoiceState.getChannel().getId())) {
            event.replyEmbeds(new JafarEmbed()
                    .setTitle("JafarBot - Musique")
                    .setDescription("Vous devez être dans le même salon que " + event.getJDA().getSelfUser().getAsMention() + " pour utiliser cette commande !")
                    .setColor(JafarEmbed.ERROR_COLOR).build()).queue();

            return;
        }

        final MusicManager musicManager = Main.jafarBot.getPlayerManager().getMusicManager();
        final TrackScheduler trackScheduler = musicManager.getTrackScheduler();

        switch(event.getSubcommandName()) {
            case "play":
                if(!audioManager.isConnected())
                    audioManager.openAudioConnection(senderVoiceState.getChannel());

                final String source = formatSource(event.getOption("source", "https://youtu.be/dQw4w9WgXcQ", OptionMapping::getAsString));
                event.deferReply().queue(hook -> Main.jafarBot.getPlayerManager().loadAndPlay(hook, source));

                break;
            case "skip":
                if(requireConnected(audioManager, event))
                    return;

                musicManager.getTrackScheduler().nextTrack(event.getOption("index", 1, OptionMapping::getAsInt));

                event.replyEmbeds(new JafarEmbed()
                                .setTitle("JafarBot - Musique")
                                .setDescription("La lecture est passé à la musique suivante.")
                                .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();

                break;
            case "pause":
                if(requireConnected(audioManager, event))
                    return;

                final AudioPlayer audioPlayer = musicManager.getAudioPlayer();
                audioPlayer.setPaused(!audioPlayer.isPaused());

                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Musique")
                        .setDescription(audioPlayer.isPaused() ? "La musique a été mise en pause" : "La musique n'est plus en pause")
                        .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();

                break;
            case "purge":
                if(requireConnected(audioManager, event))
                    return;

                musicManager.getTrackScheduler().clear();

                event.replyEmbeds(new JafarEmbed()
                                .setTitle("JafarBot - Musique")
                                .setDescription("La file d'attente a été supprimer !")
                                .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();

                break;
            case "repeat":
                if(requireConnected(audioManager, event))
                    return;

                trackScheduler.repeating = !trackScheduler.repeating;

                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Musique")
                        .setDescription(trackScheduler.repeating ? "Cette musique sera joué en boucle" : "Cette musique ne sera plus joué en boucle")
                        .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();

                break;
            case "shuffle":
                if(requireConnected(audioManager, event))
                    return;

                final List<AudioTrack> tracksCopy = new ArrayList<>(trackScheduler.queue);
                Collections.shuffle(tracksCopy);
                trackScheduler.queue = new LinkedBlockingQueue<>(tracksCopy);

                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Musique")
                        .setDescription("Toutes les musiques présente dans la file d'attente on été mélanger.")
                        .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();

                break;
            case "queue":
                if(requireConnected(audioManager, event))
                    return;

                final List<AudioTrack> tracks = new ArrayList<>(trackScheduler.queue);
                if(tracks.isEmpty()) {
                    event.replyEmbeds(new JafarEmbed()
                                    .setTitle("JafarBot - Musique")
                                    .setDescription("Il n'y a aucune musique dans la file d'attente.")
                                    .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();
                }else {
                    final StringBuilder description = new StringBuilder("Les musiques actuellement en attente sont:\n");

                    for(int i = 0; i < tracks.size(); i++) {
                        if(i >= 10) {
                            description.append("- ... *(+" + (tracks.size() - 10) + ")*\n");
                            break;
                        }

                        description.append("- " + tracks.get(i).getInfo().title + "\n");
                    }

                    event.replyEmbeds(new JafarEmbed()
                            .setTitle("JafarBot - Musique")
                            .setDescription(description.toString())
                            .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();
                }

                break;
            case "np":
                if(requireConnected(audioManager, event))
                    return;

                final AudioTrackInfo trackInfo = musicManager.getAudioPlayer().getPlayingTrack().getInfo();

                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Musique")
                        .setDescription("> " + trackInfo.title)
                        .setThumbnail(Utils.retrieveVideoThumbnail(trackInfo.uri))
                        .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();

                break;
            case "volume":
                if(requireConnected(audioManager, event))
                    return;

                final int volume = event.getOption("volume", -1, OptionMapping::getAsInt);

                if(volume == -1) {
                    event.replyEmbeds(new JafarEmbed()
                            .setTitle("JafarBot - Musique")
                            .setDescription("Le volume est actuellement à " + musicManager.getAudioPlayer().getVolume() + "%.")
                            .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();
                }else {
                    if(volume > Main.jafarBot.mainConfig.MUSIC_MAX_VOLUME) {
                        event.replyEmbeds(new JafarEmbed()
                                .setTitle("JafarBot - Musique")
                                .setDescription("Le volume ne peux pas dépasser " + Main.jafarBot.mainConfig.MUSIC_MAX_VOLUME + "%.")
                                .setColor(JafarEmbed.ERROR_COLOR).build()).queue();
                    }else {
                        musicManager.getAudioPlayer().setVolume(volume);

                        event.replyEmbeds(new JafarEmbed()
                                .setTitle("JafarBot - Musique")
                                .setDescription("Le volume est maintenant à **" + volume + "%**.")
                                .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();
                    }
                }

                break;
            case "leave":
                if(requireConnected(audioManager, event))
                    return;

                musicManager.getTrackScheduler().clear();
                musicManager.getAudioPlayer().stopTrack();
                musicManager.getTrackScheduler().nextTrack();

                event.replyEmbeds(new JafarEmbed()
                        .setTitle("JafarBot - Musique")
                        .setDescription("Le bot a été déconnecté")
                        .setColor(JafarEmbed.NEUTRAL_COLOR).build()).queue();

                break;
            default:
                break;
        }
    }

    @Nonnull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash(name, "Commande destinée à la gestion de la musique")
                .addSubcommands(
                        new SubcommandData("play", "Joue la musique donnée dans votre salon")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "source", "Lien de la musique (Youtube, Soundcloud, etc...)")
                                                .setRequired(true)
                                ),
                        new SubcommandData("skip", "Passe la lecture à la musique suivante")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "index", "Nombre de musiques a passé")
                                ),
                        new SubcommandData("pause", "Met en pause la musique"),
                        new SubcommandData("purge", "Enlève toutes les musiques de la file d'attente"),
                        new SubcommandData("repeat", "Répète la musique qui est actuellement joué"),
                        new SubcommandData("shuffle", "Mélange la file d'attente"),
                        new SubcommandData("queue", "Renvois la liste de toutes les musiques présentent dans la file d'attente"),
                        new SubcommandData("np", "Renvois le nom de la musique qui est actuellement en train d'être joué"),
                        new SubcommandData("volume", "Change le volume du bot")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "volume", "Volume en pourcentage")
                                                .setMinValue(1)
                                                .setMaxValue(Main.jafarBot.mainConfig.MUSIC_MAX_VOLUME)
                                ),
                        new SubcommandData("leave", "Supprime la file d'attente et déconnecte le bot du salon")
                );
    }

    private boolean requireConnected(AudioManager audioManager, IReplyCallback replyCallback) {
        if(!audioManager.isConnected()) {
            replyCallback.replyEmbeds(new EmbedBuilder()
                    .setTitle("JafarBot - Musique")
                    .setDescription("Le bot n'est pas connecté !")
                    .setColor(JafarEmbed.ERROR_COLOR).build()).queue();

            return true;
        }

        return false;
    }

    private String formatSource(String str) {
        try {
            new URI(str);
            return str;
        }catch(Exception ignored) {
            return "ytsearch:" + str;
        }
    }
}
