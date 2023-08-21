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
import net.vadamdev.jafarbot.music.GuildMusicManager;
import net.vadamdev.jafarbot.music.TrackScheduler;
import net.vadamdev.jdautils.commands.Command;
import net.vadamdev.jdautils.commands.ISlashCommand;
import net.vadamdev.jdautils.commands.data.ICommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
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
    public void execute(@NotNull Member sender, @NotNull ICommandData commandData) {
        final SlashCommandInteractionEvent event = ((net.vadamdev.jdautils.commands.data.impl.SlashCommandData) commandData).getEvent();

        final GuildVoiceState senderVoiceState = sender.getVoiceState();
        if(!senderVoiceState.inAudioChannel()) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Jafarbot - Musique")
                    .setDescription("Vous devez être dans un salon vocal pour utiliser cette commande !")
                    .setColor(Color.RED)
                    .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();

            return;
        }

        final AudioManager guildAudioManager = event.getGuild().getAudioManager();
        if(guildAudioManager.isConnected() && !guildAudioManager.getConnectedChannel().getId().equals(senderVoiceState.getChannel().getId())) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Jafarbot - Musique")
                    .setDescription("Vous devez être dans le même salon que Jafar pour utiliser cette commande !")
                    .setColor(Color.RED)
                    .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();

            return;
        }

        final GuildMusicManager musicManager = Main.jafarBot.getPlayerManager().getMusicManager();
        final TrackScheduler trackScheduler = musicManager.getTrackScheduler();

        switch(event.getSubcommandName()) {
            case "play":
                if(!guildAudioManager.isConnected())
                    guildAudioManager.openAudioConnection(senderVoiceState.getChannel());

                final String source = formatSource(event.getOption("source", "https://youtu.be/dQw4w9WgXcQ", OptionMapping::getAsString));
                event.deferReply().queue(hook -> Main.jafarBot.getPlayerManager().loadAndPlay(hook, source));

                break;
            case "skip":
                if(requireConnected(guildAudioManager, event))
                    return;

                musicManager.getTrackScheduler().nextTrack(event.getOption("index", 1, OptionMapping::getAsInt));

                event.replyEmbeds(new EmbedBuilder()
                                .setTitle("Jafarbot - Musique")
                                .setDescription("La lecture est passé à la musique suivante.")
                                .setColor(Color.YELLOW)
                                .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build()).queue();

                break;
            case "pause":
                if(requireConnected(guildAudioManager, event))
                    return;

                final AudioPlayer audioPlayer = musicManager.getAudioPlayer();
                audioPlayer.setPaused(!audioPlayer.isPaused());

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Jafarbot - Musique")
                        .setDescription(audioPlayer.isPaused() ? "La musique a été mise en pause" : "La musique n'est plus en pause")
                        .setColor(Color.YELLOW)
                        .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build()).queue();

                break;
            case "purge":
                if(requireConnected(guildAudioManager, event))
                    return;

                musicManager.getTrackScheduler().clear();

                event.replyEmbeds(new EmbedBuilder()
                                .setTitle("Jafarbot - Musique")
                                .setDescription("La file d'attente a été supprimer !")
                                .setColor(Color.YELLOW)
                                .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build()).queue();

                break;
            case "repeat":
                if(requireConnected(guildAudioManager, event))
                    return;

                trackScheduler.repeating = !trackScheduler.repeating;

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Jafarbot - Musique")
                        .setDescription(trackScheduler.repeating ? "Cette musique sera joué en boucle." : "Cette musique ne sera plus joué en boucle.")
                        .setColor(Color.YELLOW)
                        .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build()).queue();

                break;
            case "shuffle":
                if(requireConnected(guildAudioManager, event))
                    return;

                final List<AudioTrack> tracksCopy = new ArrayList<>(trackScheduler.queue);
                Collections.shuffle(tracksCopy);
                trackScheduler.queue = new LinkedBlockingQueue<>(tracksCopy);

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Jafarbot - Musique")
                        .setDescription("Toutes les musiques présente dans la file d'attente on été mélanger.")
                        .setColor(Color.YELLOW)
                        .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build()).queue();

                break;
            case "queue":
                if(requireConnected(guildAudioManager, event))
                    return;

                final List<AudioTrack> tracks = new ArrayList<>(trackScheduler.queue);
                if(tracks.isEmpty()) {
                    event.replyEmbeds(new EmbedBuilder()
                                    .setTitle("Jafarbot - Musique")
                                    .setDescription("Il n'y a aucune musique dans la file d'attente.")
                                    .setColor(Color.RED)
                                    .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build()).queue();
                }else {
                    final StringBuilder description = new StringBuilder("Les musiques actuellement en attente sont:\n");

                    for(int i = 0; i < tracks.size(); i++) {
                        if(i >= 10) {
                            description.append("- ... (+" + (tracks.size() - 10) + ")\n");
                            break;
                        }

                        description.append("- " + tracks.get(i).getInfo().title + "\n");
                    }

                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Jafarbot - Musique")
                            .setDescription(description.toString())
                            .setColor(Color.YELLOW)
                            .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build()).queue();
                }

                break;
            case "np":
                if(requireConnected(guildAudioManager, event))
                    return;

                final AudioTrackInfo trackInfo = musicManager.getAudioPlayer().getPlayingTrack().getInfo();

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Jafarbot - Musique")
                        .setDescription("**" + trackInfo.title + "** by **" + trackInfo.author + "**")
                        .setColor(Color.YELLOW)
                        .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build()).queue();

                break;
            case "volume":
                if(requireConnected(guildAudioManager, event))
                    return;

                final int volume = event.getOption("volume", Main.jafarBot.mainConfig.MUSIC_DEFAULT_VOLUME, OptionMapping::getAsInt);

                if(volume > Main.jafarBot.mainConfig.MUSIC_MAX_VOLUME) {
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Jafarbot - Musique")
                            .setDescription("Le volume ne peux pas dépasser **" + Main.jafarBot.mainConfig.MUSIC_MAX_VOLUME + "%**.")
                            .setColor(Color.RED)
                            .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();
                }else {
                    musicManager.getAudioPlayer().setVolume(volume);

                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Jafarbot - Musique")
                            .setDescription("Le volume est maintenant à **" + volume + "%**.")
                            .setColor(Color.YELLOW)
                            .setFooter("JafarBot", Main.jafarBot.getAvatarURL()).build()).queue();
                }

                break;
            case "leave":
                if(requireConnected(guildAudioManager, event))
                    return;

                musicManager.getTrackScheduler().clear();
                musicManager.getAudioPlayer().stopTrack();
                musicManager.getTrackScheduler().nextTrack();

                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Jafarbot - Musique")
                        .setDescription("Le bot a été déconnecté")
                        .setColor(Color.YELLOW)
                        .setFooter("", Main.jafarBot.getAvatarURL()).build()).queue();

                break;
            default:
                break;
        }
    }

    @NotNull
    @Override
    public SlashCommandData createSlashCommand() {
        return Commands.slash(name, "PLACEHOLDER")
                .addSubcommands(
                        new SubcommandData("play", "Joue la musique donnée dans votre salon")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "source", "Lien de la musique (Youtube, Soundcloud, etc...)")
                                                .setRequired(true)
                                ),
                        new SubcommandData("skip", "Passe la lecture a la musique suivante")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "index", "Nombre de musiques a passé")
                                ),
                        new SubcommandData("pause", "Met en pause la musique en train d'être joué"),
                        new SubcommandData("purge", "Enlève toutes les musiques présentent dans la file d'attente"),
                        new SubcommandData("repeat", "Répète la musique qui est actuellement en train d'être joué"),
                        new SubcommandData("shuffle", "Mélange la file d'attente"),
                        new SubcommandData("queue", "Renvoi la liste de toutes les musiques présentent dans la file d'attente"),
                        new SubcommandData("np", "Renvoi le nom de la musique qui est actuellement en train d'être joué"),
                        new SubcommandData("volume", "Change le volume du bot")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "volume", "Volume en pourcentage")
                                                .setMinValue(1)
                                                .setMaxValue(Main.jafarBot.mainConfig.MUSIC_MAX_VOLUME)
                                                .setRequired(true)
                                ),
                        new SubcommandData("leave", "Supprime la file d'attente et déconnecte le bot du salon")
                );
    }

    private boolean requireConnected(AudioManager guildAudioManager, IReplyCallback replyCallback) {
        if(!guildAudioManager.isConnected()) {
            replyCallback.replyEmbeds(new EmbedBuilder()
                    .setTitle("Jafarbot - Musique")
                    .setDescription("Le bot n'est pas connecté !")
                    .setColor(Color.RED)
                    .setFooter("Jafarbot", Main.jafarBot.getAvatarURL()).build()).queue();

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
