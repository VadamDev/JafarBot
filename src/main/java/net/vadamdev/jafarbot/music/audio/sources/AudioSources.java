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
            //TODO: poToken & OAuth
            return new YoutubeAudioSourceManager(
                    params.getBoolean("allowSearch"),
                    params.getBoolean("allowDirectVideoIds"),
                    params.getBoolean("allowDirectPlay")
            );
        }

        @Override
        protected void compose(ConfigurationSection params) {
            params.addDefault("allowSearch", true);
            params.addDefault("allowDirectVideoIds", true);
            params.addDefault("allowDirectPlay", true);
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
