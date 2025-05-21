package net.vadamdev.jafarbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.managers.AudioManager;
import net.vadamdev.dbk.framework.interactive.api.components.InteractiveComponent;
import net.vadamdev.dbk.framework.interactive.api.registry.MessageRegistry;
import net.vadamdev.dbk.framework.interactive.entities.buttons.InteractiveButton;
import net.vadamdev.dbk.framework.interactive.entities.dropdowns.InteractiveStringSelectMenu;
import net.vadamdev.dbk.framework.menu.InteractiveComponentMenu;
import net.vadamdev.dbk.framework.menu.InvalidateActions;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.music.audio.AudioLoadResultAdapter;
import net.vadamdev.jafarbot.music.audio.LavaSendHandler;
import net.vadamdev.jafarbot.music.audio.TrackScheduler;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.LazyAccessor;
import net.vadamdev.jafarbot.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author VadamDev
 * @since 06/06/2024
 */
@SuppressWarnings("unchecked")
public record GuildMusicBridge(String guildId, AudioPlayer audioPlayer, TrackScheduler trackScheduler, LavaSendHandler sendHandler) {
    private static final int MAX_RESEARCH_SIZE = 10; //Cannot exceed 10

    private static final TimeUnit SEARCH_CONFIRM_TIMEOUT_UNIT = TimeUnit.MINUTES;
    private static final long SEARCH_CONFIRM_TIMEOUT_DELAY = 10;

    public GuildMusicBridge(Guild guild, AudioPlayer audioPlayer, InactivityChecker inactivityChecker) {
        this(guild.getId(), audioPlayer, new TrackScheduler(guild.getId(), inactivityChecker, audioPlayer), new LavaSendHandler(audioPlayer));

        guild.getAudioManager().setSendingHandler(sendHandler);
    }

    public GuildMusicBridge {
        audioPlayer.setVolume(JafarBot.get().getConfig().DEFAULT_VOLUME);
    }

    public CompletableFuture<Boolean> playOrQueue(GuildMusicManager musicManager, String source, InteractionHook hook) {
        return musicManager.loadTrack(this, source, future -> new AudioLoadResultAdapter() {
            @Override
            public void trackLoaded(AudioTrack track) {
                final AudioTrackInfo trackInfo = track.getInfo();

                String musicTitle = trackInfo.title;
                if(trackInfo.uri != null)
                    musicTitle  = "[" + musicTitle + "](" + trackInfo.uri + ")";

                final EmbedBuilder embed = EmbedUtils.defaultSuccess(String.format(
                        """
                        **%s**
                        à été ajouté à la file d'attente.
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

                hook.editOriginalEmbeds(embed.build()).setReplace(true).queue();
            }

            @Override
            public void onPlaylistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();

                //Calculate total duration
                long totalDurationMs = 0;
                for (AudioTrack track : tracks)
                    totalDurationMs += track.getDuration();

                //Format playlist name
                String playlistName = playlist.getName();
                if(Utils.isURL(source))
                    playlistName = "[" + playlistName + "](" + source + ")";

                //Retrieve thumbnail
                String thumbnail;

                AudioTrack thumbnailTrack = playlist.getSelectedTrack();
                if(thumbnailTrack == null)
                    thumbnailTrack = tracks.getFirst();

                final AudioTrackInfo trackInfo = thumbnailTrack.getInfo();
                if(trackInfo.artworkUrl != null)
                    thumbnail = trackInfo.artworkUrl;
                else
                    thumbnail = Utils.retrieveVideoThumbnail(trackInfo.uri);

                //Send results
                hook.editOriginalEmbeds(
                        EmbedUtils.defaultEmbed(EmbedUtils.SUCCESS_COLOR)
                                .setTitle("\uD83C\uDFA7┃Musique")
                                .setDescription("Ajout de la playlist " + playlistName + " à la file d'attente")
                                .addField("Titres", "``" + tracks.size() + "``", true)
                                .addField("Durée", "``" + Utils.formatMsToHMS(totalDurationMs) + "``", true)
                                .setThumbnail(thumbnail)
                                .build()
                ).queue();
            }

            @Override
            public void onSearchResultFound(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();

                final StringBuilder description = new StringBuilder(String.format(
                        """
                        Veuillez choisir la musique adéquat dans la liste ci-dessous.
                        Si vous ne trouvez pas votre bonheur, essayez de reformuler votre recherche.
                        
                        Ce message expirera dans <t:%d:R>.
                        
                        """,
                        (System.currentTimeMillis() / 1000) + SEARCH_CONFIRM_TIMEOUT_UNIT.toSeconds(SEARCH_CONFIRM_TIMEOUT_DELAY)
                ));

                final List<SelectOption> options = new ArrayList<>();

                for(int i = 0; i < Math.min(MAX_RESEARCH_SIZE, tracks.size()); i++) {
                    final AudioTrack track = tracks.get(i);
                    final AudioTrackInfo info = track.getInfo();

                    final String formattedDuration = Utils.formatMsToHMS(track.getDuration());
                    final EmojiUnion digit = Utils.formatDigitToDiscordEmoji(i + 1);

                    description.append(digit.getFormatted() + " | ``[" + formattedDuration + "]`` - [" + info.title + "](" + info.uri + ")\n");
                    description.append("   de __" + info.author + "__\n\n");

                    options.add(
                            SelectOption.of(info.title, String.valueOf(i))
                                    .withDescription("[" + formattedDuration + "] " + info.author)
                                    .withEmoji(digit)
                    );
                }

                final LazyAccessor<InteractiveComponentMenu> menuAccess = new LazyAccessor<>();

                final MessageRegistry<StringSelectMenu> selectMenu = InteractiveStringSelectMenu.of(
                        StringSelectMenu.create(InteractiveComponent.generateComponentUID())
                                .setPlaceholder("Sélectionnez un résultat")
                                .addOptions(options).build(),

                        (event, invalidatable) -> {
                            try {
                                final int selectedIndex = Integer.parseInt(event.getSelectedOptions().getFirst().getValue());
                                if(selectedIndex < 0 || selectedIndex >= tracks.size())
                                    return;

                                event.deferEdit().queue();
                                menuAccess.ifPresent(menu -> {
                                    menu.cancelTimeout();
                                    menu.invalidateComponents(event.getJDA());
                                });

                                final AudioTrack selected = tracks.get(selectedIndex);
                                trackScheduler.add(selected);
                                trackLoaded(selected);

                                future.complete(true);
                            }catch (Exception ignored) {}
                        }
                );

                final MessageRegistry<Button> cancelButton = InteractiveButton.of(ButtonStyle.DANGER)
                        .label("Annuler")
                        .action((event, invalidatable) -> {
                            event.deferEdit().queue();
                            event.getMessage().delete().queue();
                        }).build();

                final InteractiveComponentMenu menu = InteractiveComponentMenu.builder()
                        .addEmbed(EmbedUtils.defaultSuccess(description.toString()).build())
                        .addActionRow(selectMenu)
                        .addActionRow(cancelButton)
                        .setTimeout(SEARCH_CONFIRM_TIMEOUT_DELAY, SEARCH_CONFIRM_TIMEOUT_UNIT)
                        .onInvalidate(InvalidateActions.DELETE_MESSAGE_ON_INVALIDATE)
                        .build();

                menuAccess.set(menu);
                menu.display(hook, true).queue();
            }

            @Override
            public void noMatches() {
                hook.editOriginalEmbeds(EmbedUtils.defaultError("Aucun résultat n'a pus être trouvé !").setTitle("\uD83C\uDFA7┃Musique").build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                hook.editOriginalEmbeds(
                        EmbedUtils.defaultError("Une erreur est survenue lors du chargement de la musique !")
                                .setTitle("\uD83C\uDFA7┃Musique")
                                .addField("⚠️ Severity", "```" + exception.severity.name() + "```", true)
                                .addField("\uD83D\uDCAC Message", "```" + exception.getMessage() + "```", false).build()
                ).queue();
            }
        });
    }

    public boolean tryDisconnect(Guild guild) {
        final AudioManager audioManager = guild.getAudioManager();
        if(!audioManager.isConnected())
            return false;

        audioManager.closeAudioConnection();

        return silentDestroy(guild);
    }

    public boolean silentDestroy(Guild guild) {
        trackScheduler.clear();
        audioPlayer.stopTrack();

        return JafarBot.get().getMusicManager().remove(guild);
    }
}
