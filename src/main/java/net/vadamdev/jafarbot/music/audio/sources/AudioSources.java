package net.vadamdev.jafarbot.music.audio.sources;

import com.github.topi314.lavasrc.applemusic.AppleMusicSourceManager;
import com.github.topi314.lavasrc.deezer.DeezerAudioSourceManager;
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.github.topi314.lavasrc.tidal.TidalSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.*;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;

/**
 * @author VadamDev
 * @since 15/07/2024
 */
public enum AudioSources {
    BANDCAMP {
        @Override
        protected AudioSourceManager parse(AudioPlayerManager playerManager, ConfigurationSection params) {
            return new BandcampAudioSourceManager(params.getBoolean("allowSearch"));
        }

        @Override
        protected void compose(ConfigurationSection params) {
            params.addDefault("allowSearch", true);
        }

        @Override
        public String getSearchPrefix() {
            return "bcsearch:";
        }
    },

    YOUTUBE {
        @Override
        protected AudioSourceManager parse(AudioPlayerManager playerManager, ConfigurationSection params) {
            final YoutubeSourceOptions options = new YoutubeSourceOptions()
                    .setAllowSearch(params.getBoolean("allowSearch"))
                    .setAllowDirectVideoIds(params.getBoolean("allowDirectVideoIds"))
                    .setAllowDirectPlaylistIds(params.getBoolean("allowDirectPlaylistIds"));

            //Default ones are: Music, AndroidVr, Web, WebEmbedded
            //They are from the common package of YouTube source... We are using v2 ones here and new ones for more success rate and OAuth usage
            final YoutubeAudioSourceManager result = new YoutubeAudioSourceManager(
                    options,

                    new MusicWithThumbnail(),
                    new WebWithThumbnail(), new MWebWithThumbnail(), new WebEmbeddedWithThumbnail(), //Web
                    new AndroidMusicWithThumbnail(), new AndroidVrWithThumbnail(), //Android
                    new IosWithThumbnail(), //Ios
                    new Tv(), new TvHtml5EmbeddedWithThumbnail() //Tv, REQUIRE OAUTH

            );

            final ConfigurationSection handlerSection = params.getConfigurationSection("handler");
            YoutubeAudioHandlerType.of(handlerSection.getString("type")).handle(result, handlerSection);

            return result;
        }

        @Override
        protected void compose(ConfigurationSection params) {
            params.addDefault("allowSearch", true);
            params.addDefault("allowDirectVideoIds", true);
            params.addDefault("allowDirectPlaylistIds", true);

            final ConfigurationSection handlerSection = params.createSection("handler");
            handlerSection.addDefault("type", YoutubeAudioHandlerType.NONE.name());

            YoutubeAudioHandlerType.composeAll(handlerSection);
        }

        @Override
        public String getSearchPrefix() {
            return YoutubeAudioSourceManager.SEARCH_PREFIX;
        }
    },

    SPOTIFY(false) {
        @Override
        protected AudioSourceManager parse(AudioPlayerManager playerManager, ConfigurationSection params) {
            return new SpotifySourceManager(
                    params.getString("clientId"),
                    params.getString("clientSecret"),
                    params.getBoolean("preferAnonymousToken"),
                    null,
                    params.getString("countryCode"),
                    unused -> playerManager,
                    new DefaultMirroringAudioTrackResolver(null)
            );
        }

        @Override
        protected void compose(ConfigurationSection params) {
            params.addDefault("clientId", "SPOTIFY_CLIENT_ID");
            params.addDefault("clientSecret", "SPOTIFY_CLIENT_SECRET");
            params.addDefault("preferAnonymousToken", false);
            params.addDefault("countryCode", "US");
        }

        @Override
        public String getSearchPrefix() {
            return SpotifySourceManager.SEARCH_PREFIX;
        }
    },

    APPLE_MUSIC(false) {
        @Override
        protected AudioSourceManager parse(AudioPlayerManager playerManager, ConfigurationSection params) {
            return new AppleMusicSourceManager(
                    params.getString("mediaAPIToken"),
                    params.getString("countryCode"),
                    unused -> playerManager,
                    new DefaultMirroringAudioTrackResolver(null)
            );
        }

        @Override
        protected void compose(ConfigurationSection params) {
            params.addDefault("mediaAPIToken", "APPLE_MUSIC_MEDIA_API_TOKEN");
            params.addDefault("countryCode", "US");
        }

        @Override
        public String getSearchPrefix() {
            return AppleMusicSourceManager.SEARCH_PREFIX;
        }
    },

    DEEZER(false) {
        @Override
        protected AudioSourceManager parse(AudioPlayerManager playerManager, ConfigurationSection params) {
            return new DeezerAudioSourceManager(
                    params.getString("masterDecryptionKey"),
                    params.getString("arl")
            );
        }

        @Override
        protected void compose(ConfigurationSection params) {
            params.addDefault("masterDecryptionKey", "DEEZER_MASTER_DECRYPTION_KEY");
            params.addDefault("arl", "DEEZER_ARL");
        }

        @Override
        public String getSearchPrefix() {
            return DeezerAudioSourceManager.SEARCH_PREFIX;
        }
    },

    TIDAL(false) {
        @Override
        protected AudioSourceManager parse(AudioPlayerManager playerManager, ConfigurationSection params) {
            return new TidalSourceManager(
                    params.getString("countryCode"),
                    unused -> playerManager,
                    new DefaultMirroringAudioTrackResolver(null),
                    params.getString("tidalToken")
            );
        }

        @Override
        protected void compose(ConfigurationSection params) {
            params.addDefault("tidalToken", "TIDAL_TOKEN");
            params.addDefault("countryCode", "US");
        }

        @Override
        public String getSearchPrefix() {
            return TidalSourceManager.SEARCH_PREFIX;
        }
    },

    //Done
    SOUNDCLOUD {
        @Override
        public AudioSourceManager parse(AudioPlayerManager playerManager, ConfigurationSection params) {
            return SoundCloudAudioSourceManager.builder()
                    .withAllowSearch(params.getBoolean("allowSearch"))
                    .build();
        }

        @Override
        protected void compose(ConfigurationSection params) {
            params.addDefault("allowSearch", true);
        }

        @Override
        public String getSearchPrefix() {
            return "scsearch:";
        }
    };

    private final boolean defaultEnabled;

    AudioSources(boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
    }

    AudioSources() {
        this(true);
    }

    @Nullable
    protected abstract AudioSourceManager parse(AudioPlayerManager playerManager, ConfigurationSection params);
    protected abstract void compose(ConfigurationSection params);

    @Nullable
    public String getSearchPrefix() {
        return null;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }
}
