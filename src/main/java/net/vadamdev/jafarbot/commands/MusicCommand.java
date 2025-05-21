package net.vadamdev.jafarbot.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.vadamdev.dbk.framework.commands.annotations.AnnotationProcessor;
import net.vadamdev.dbk.framework.commands.annotations.CommandProcessor;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.commands.api.GuildLinkedCommand;
import net.vadamdev.jafarbot.configs.MainConfig;
import net.vadamdev.jafarbot.music.GuildMusicBridge;
import net.vadamdev.jafarbot.music.GuildMusicManager;
import net.vadamdev.jafarbot.music.audio.TrackScheduler;
import net.vadamdev.jafarbot.music.audio.sources.AudioSourcesLoader;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author VadamDev
 * @since 13/05/2025
 */
public class MusicCommand extends GuildLinkedCommand {
    private final MainConfig mainConfig;
    private final GuildMusicManager musicManager;

    public MusicCommand(MainConfig mainConfig, GuildMusicManager musicManager) {
        super("music", "PLACEHOLDER");

        this.mainConfig = mainConfig;
        this.musicManager = musicManager;
    }

    @CommandProcessor(subCommand = "play")
    private void play(SlashCommandInteractionEvent event) {
        final String source = event.getOption("source", mapping -> {
            final String input = mapping.getAsString();
            return Utils.isURL(input) ? input : YoutubeAudioSourceManager.SEARCH_PREFIX + input;
        });

        playOrQueue(event ,source);
    }

    @CommandProcessor(subCommand = "search")
    private void search(SlashCommandInteractionEvent event) {
        final String source = event.getOption("platform", OptionMapping::getAsString) + event.getOption("source", OptionMapping::getAsString);
        playOrQueue(event, source);
    }

    private void playOrQueue(SlashCommandInteractionEvent event, String source) {
        final Guild guild = event.getGuild();
        final GuildMusicBridge bridge = musicManager.getOrCreate(guild);

        event.deferReply().queue(hook -> {
            bridge.playOrQueue(musicManager, source, hook).whenComplete((shouldConnect, throwable) -> {
                final AudioManager audioManager = guild.getAudioManager();
                if(!shouldConnect || audioManager.isConnected())
                    return;

                audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());
            });
        });
    }

    @CommandProcessor(subCommand = "skip")
    private void skip(SlashCommandInteractionEvent event) {
        final TrackScheduler trackScheduler = musicManager.getOrCreate(event.getGuild()).trackScheduler();
        if(!trackScheduler.isPlaying()) {
            event.replyEmbeds(EmbedUtils.defaultError("Il n'y a pas de musique à passer !").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
            return;
        }

        final OptionMapping amount = event.getOption("amount");
        int toSkip = amount != null ? amount.getAsInt() : 1;

        if(toSkip > 1) {
            final int queueSize = trackScheduler.getQueueSize();
            if(queueSize > toSkip)
                toSkip = Math.clamp(toSkip, 1, queueSize);

            trackScheduler.skipTracks(toSkip);
            event.replyEmbeds(EmbedUtils.defaultSuccess("``" + toSkip + "`` musiques ont été passées !").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
        }else {
            trackScheduler.startNextTrack();
            event.replyEmbeds(EmbedUtils.defaultSuccess("La musique actuelle a été passée !").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
        }
    }

    @CommandProcessor(subCommand = "loop")
    private void loop(SlashCommandInteractionEvent event) {
        final TrackScheduler trackScheduler = musicManager.getOrCreate(event.getGuild()).trackScheduler();
        trackScheduler.repeatCurrentTrack = !trackScheduler.repeatCurrentTrack;

        final EmbedBuilder embed = EmbedUtils.defaultEmbed(EmbedUtils.SUCCESS_COLOR).setTitle("\uD83C\uDFA7┃Musique");
        if(trackScheduler.repeatCurrentTrack)
            embed.setDescription("La musique actuelle sera jouée en boucle !");
        else
            embed.setDescription("La musique actuelle ne sera plus jouée en boucle !");

        event.replyEmbeds(embed.build()).queue();
    }

    @CommandProcessor(subCommand = "nowplaying")
    private void nowplaying(SlashCommandInteractionEvent event) {
        final AudioPlayer audioPlayer = musicManager.getOrCreate(event.getGuild()).audioPlayer();
        final AudioTrack track = audioPlayer.getPlayingTrack();

        if(track == null || !track.getState().equals(AudioTrackState.PLAYING)) {
            event.replyEmbeds(EmbedUtils.defaultError("Il n'y a pas de musique en cours de lecture !").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
            return;
        }

        final AudioTrackInfo trackInfo = track.getInfo();

        String musicTitle = trackInfo.title;
        if(trackInfo.uri != null)
            musicTitle  = "[" + musicTitle + "](" + trackInfo.uri + ")";

        final EmbedBuilder embed = EmbedUtils.defaultSuccess(String.format(
                """
                La musique actuelle est :
                **%s**
                """,
                musicTitle)
        ).setTitle("\uD83C\uDFA7┃Musique");

        if(trackInfo.artworkUrl != null && !trackInfo.artworkUrl.isBlank())
            embed.setThumbnail(trackInfo.artworkUrl);
        else {
            final String thumbnailURL = Utils.retrieveVideoThumbnail(trackInfo.uri);
            if(thumbnailURL != null)
                embed.setThumbnail(thumbnailURL);
        }

        event.replyEmbeds(embed.build()).queue();
    }

    @CommandProcessor(subCommand = "queue")
    private void queue(SlashCommandInteractionEvent event) {
        final TrackScheduler trackScheduler = musicManager.getOrCreate(event.getGuild()).trackScheduler();

        if(trackScheduler.isQueueEmpty()) {
            event.replyEmbeds(EmbedUtils.defaultError("La file d'attente est vide").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
            return;
        }

        final StringBuilder description = new StringBuilder("Les musiques actuellement en attente sont:\n");

        final List<AudioTrack> queue = trackScheduler.getUnmodifiableQueue();
        final int queueSize = queue.size();

        for(int i = 0; i < Math.min(10, queueSize); i++) {
            final AudioTrack track = queue.get(i);
            final AudioTrackInfo info = track.getInfo();

            description.append("- " + i + ". ``[" + Utils.formatMsToHMS(track.getDuration()) + "]`` - [" + info.title + "](" + info.uri + ")\n");
            description.append("   de __" + info.author + "__\n");
        }

        if(queueSize > 10)
            description.append("- ... (+" + (queueSize - 10) + ")");

        event.replyEmbeds(EmbedUtils.defaultSuccess(description.toString()).setTitle("\uD83C\uDFA7┃Musique").build()).queue();
    }

    @CommandProcessor(subCommand = "shuffle")
    private void shuffle(SlashCommandInteractionEvent event) {
        final TrackScheduler trackScheduler = musicManager.getOrCreate(event.getGuild()).trackScheduler();

        if(trackScheduler.isQueueEmpty()) {
            event.replyEmbeds(EmbedUtils.defaultError("La file d'attente est vide").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
            return;
        }

        trackScheduler.shuffleQueue();
        event.replyEmbeds(EmbedUtils.defaultSuccess("La file d'attente a été mélanger").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
    }

    @CommandProcessor(subCommand = "volume")
    private void volume(SlashCommandInteractionEvent event) {
        final AudioPlayer audioPlayer = musicManager.getOrCreate(event.getGuild()).audioPlayer();

        final OptionMapping volumeOption = event.getOption("volume");
        if(volumeOption == null) {
            event.replyEmbeds(EmbedUtils.defaultSuccess("Le volume est actuellement à ``" + audioPlayer.getVolume() + "%``.").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
            return;
        }

        final int newVolume = Math.clamp(volumeOption.getAsInt(), 1, mainConfig.MAX_VOLUME);
        audioPlayer.setVolume(newVolume);

        event.replyEmbeds(EmbedUtils.defaultSuccess("Le volume est maintenant à ``" + newVolume + "%``.").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
    }

    @CommandProcessor(subCommand = "pause")
    private void pause(SlashCommandInteractionEvent event) {
        final AudioPlayer audioPlayer = musicManager.getOrCreate(event.getGuild()).audioPlayer();

        final boolean paused = !audioPlayer.isPaused();
        audioPlayer.setPaused(paused);

        if(!paused)
            event.replyEmbeds(EmbedUtils.defaultSuccess("La musique a été reprise !").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
        else
            event.replyEmbeds(EmbedUtils.defaultSuccess("La musique a été mise sur pause !").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
    }

    @CommandProcessor(subCommand = "purge")
    private void purge(SlashCommandInteractionEvent event) {
        final TrackScheduler trackScheduler = musicManager.getOrCreate(event.getGuild()).trackScheduler();
        if(trackScheduler.isQueueEmpty()) {
            event.replyEmbeds(EmbedUtils.defaultError("La file d'attente est déjà vide").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
            return;
        }

        trackScheduler.clearQueue();
        event.replyEmbeds(EmbedUtils.defaultSuccess("La file d'attente a été supprimée !").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
    }

    @CommandProcessor(subCommand = "leave")
    private void leave(SlashCommandInteractionEvent event) {
        final Guild guild = event.getGuild();
        final GuildMusicBridge bridge = musicManager.getOrCreate(guild);

        if(bridge.tryDisconnect(guild))
            event.replyEmbeds(
                    EmbedUtils.defaultSuccess("Le bot a été déconnecter !").setTitle("\uD83C\uDFA7┃Musique").build()
            ).queue();
        else
            event.replyEmbeds(
                    EmbedUtils.defaultError("Le bot n'est actuellement pas connecter !").setTitle("\uD83C\uDFA7┃Musique").build()
            ).queue();
    }

    @NotNull
    @Override
    public SlashCommandData createCommandData() {
        return super.createCommandData().addSubcommands(
                new SubcommandData("play", "Ajoute une musique dans la file d'attente. Si ce n'est pas un lien, effectue une recherche YouTube").addOptions(
                        new OptionData(OptionType.STRING, "source", "Source de la musique (lien youtube, soundcloud, etc...)", true)
                ),
                new SubcommandData("search", "Effectue une recherche sur la plateforme de streaming indiqué").addOptions(
                        new OptionData(OptionType.STRING, "source", "Mot clé de la recherche", true),
                        new OptionData(OptionType.STRING, "platform", "Plateforme ou la recherche sera éffectué", true)
                                .addChoices(AudioSourcesLoader.getSearchChoices())
                ),
                new SubcommandData("skip", "Passe à la musique suivante").addOptions(
                        new OptionData(OptionType.INTEGER, "amount", "Nombre de musiques à passer")
                                .setMinValue(1)
                ),
                new SubcommandData("loop", "Joue la musique actuel en boucle"),
                new SubcommandData("nowplaying", "Affiche le nom de la musique actuellement joué"),
                new SubcommandData("queue", "Affiche la liste des musiques dans la file d'attente"),
                new SubcommandData("shuffle", "Mélange la file d'attente"),
                new SubcommandData("volume", "Change le volume du bot").addOptions(
                        new OptionData(OptionType.INTEGER, "volume", "Volume de la musique")
                                .setRequiredRange(1, mainConfig.MAX_VOLUME)
                ),
                new SubcommandData("pause", "Met en pause ou reprend la musique actuelle"),
                new SubcommandData("purge", "Supprime la file d'attente"),
                new SubcommandData("leave", "Supprime la file d'attente et déconnecte le bot")
        );
    }

    @Override
    public void executeCommand(Member sender, SlashCommandInteractionEvent event) {
        final GuildVoiceState senderVoiceState = sender.getVoiceState();
        final AudioManager audioManager = event.getGuild().getAudioManager();
        if(!senderVoiceState.inAudioChannel() || (audioManager.isConnected() && !audioManager.getConnectedChannel().getId().equals(senderVoiceState.getChannel().getId()))) {
            event.replyEmbeds(EmbedUtils.defaultError("Vous devez être dans le même salon que " + JafarBot.get().getJDA().getSelfUser().getAsMention() + " pour utiliser cette commande !")
                    .setTitle("➕┃Musique").build()).queue();

            return;
        }

        AnnotationProcessor.processAnnotations(event, this);
    }
}
