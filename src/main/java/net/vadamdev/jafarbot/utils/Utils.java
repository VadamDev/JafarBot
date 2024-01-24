package net.vadamdev.jafarbot.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VadamDev
 * @since 02/03/2023
 */
public final class Utils {
    public static String displayBoolean(boolean b) {
        return b ? "✅" : "❌";
    }

    @Nonnull
    public static File initFile(String pathName) throws IOException {
        final File file = new File(pathName);
        if(!file.exists())
            file.createNewFile();

        return file;
    }

    //From: https://gist.github.com/jvanderwee/b30fdb496acff43aef8e
    private static final Pattern youtubeRegex = Pattern.compile("^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/");
    private static final Pattern[] videoIdRegex = {
            Pattern.compile("\\?vi?=([^&]*)"),
            Pattern.compile("watch\\?.*v=([^&]*)"),
            Pattern.compile("(?:embed|vi?)/([^/?]*)"),
            Pattern.compile("^([A-Za-z0-9\\-]*)")
    };

    @Nullable
    public static String retrieveVideoThumbnail(String url) {
        final Matcher urlMatcher = youtubeRegex.matcher(url);
        final String youTubeLinkWithoutProtocolAndDomain = urlMatcher.find() ? url.replace(urlMatcher.group(), "") : url;

        for(Pattern pattern : videoIdRegex) {
            final Matcher matcher = pattern.matcher(youTubeLinkWithoutProtocolAndDomain);
            if(!matcher.find())
                continue;

            return "https://img.youtube.com/vi/" + matcher.group(1) + "/0.jpg";
        }

        return null;
    }
}
