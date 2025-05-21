package net.vadamdev.jafarbot.utils;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.vadamdev.dbk.framework.interactive.api.components.InteractiveComponent;
import net.vadamdev.jafarbot.JafarBot;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VadamDev
 * @since 30/03/2025
 */
public final class Utils {
    private Utils() {}

    /*
       File Creation
     */

    public static File createFile(File file) throws IOException {
        if(!file.exists()) {
            final File parent = file.getParentFile();
            if(!parent.exists())
                parent.mkdirs();

            file.createNewFile();
        }

        return file;
    }

    public static File createFile(String pathName) throws IOException {
        return createFile(new File(pathName));
    }

    /*
        Confirmation Request
     */

    private static final TimeUnit CONFIRMATION_TIMEOUT_UNIT = TimeUnit.MINUTES;
    private static final long CONFIRMATION_TIMEOUT_DELAY = 5;

    public static ConfirmationRequest createDefaultConfirmationRequest(Consumer<GenericComponentInteractionCreateEvent> onConfirm) {
        final long timeoutTime = (System.currentTimeMillis() / 1000) + CONFIRMATION_TIMEOUT_UNIT.toSeconds(CONFIRMATION_TIMEOUT_DELAY);

        return ConfirmationRequest.builder()
                .timeout(CONFIRMATION_TIMEOUT_DELAY, CONFIRMATION_TIMEOUT_UNIT)
                .whenConfirmed(Button.success(InteractiveComponent.generateComponentUID(), "Confirmer"), onConfirm)
                .addEmbed(EmbedUtils.defaultEmbed(Color.WHITE)
                        .setTitle("Êtes vous sur(e) de faire cela ?")
                        .setDescription(String.format(
                                """
                                Veuillez confirmer votre choix en cliquant sur le bouton ci-dessous.
                                
                                -# Si vous ne confirmez pas, ce message expirera <t:%d:R>.
                                """,
                                timeoutTime
                        )).build()
                ).build();
    }

    /*
       Emoji
     */

    //Thanks to https://www.baeldung.com/java-check-letter-emoji
    private static final String EMOJI_UNICODE_REGEX  = "[\\uD800-\\uDBFF\\uDC00-\\uDFFF]+";

    public static boolean isEmoji(String emoji) {
        return emoji.matches(EMOJI_UNICODE_REGEX);
    }

    /*
       Misc
     */

    public static boolean isURL(String str) {
        try {
            new URL(str); //Deprecated since Java 20. TOO BAD!
            return true;
        }catch (MalformedURLException ignored) {
            return false;
        }
    }

    public static String formatBoolean(boolean value) {
        return value ? "✅" : "❌";
    }

    public static boolean hasRole(Member member, String... roleIds) {
        for(Role role : member.getRoles()) {
            final String roleId = role.getId();

            for(String id : roleIds) {
                if(roleId.equals(id))
                    return true;
            }
        }

        return false;
    }

    public static List<String> getMessageImages(Message message) {
        return message.getAttachments().stream()
                .filter(Message.Attachment::isImage)
                .map(Message.Attachment::getProxyUrl)
                .toList();
    }

    @Nullable
    public static Icon retrieveBotAvatarAsIcon() {
        final String avatarUrl = JafarBot.get().getJDA().getSelfUser().getAvatarUrl();
        if(avatarUrl == null)
            return null;

        try {
            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(avatarUrl)).build();

            final Icon result = Icon.from(client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body());
            client.close();

            return result;
        }catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String formatMsToHMS(long ms) {
        final StringBuilder result = new StringBuilder();

        final long hours = (ms / 3600000) % 24;
        if(hours > 0)
            result.append(hours + "h ");

        final long minutes = (ms / 60000) % 60;
        if(minutes > 0)
            result.append(minutes + "m ");

        final long seconds = (ms / 1000) % 60;
        if(seconds > 0)
            result.append(seconds + "s");

        return result.toString();
    }

    public static String formatMsToDHMS(long ms) {
        final long totalSeconds = ms / 1000;
        return String.format("%dd %dh %dm %ds", totalSeconds / 86400, (totalSeconds % 86400) / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
    }

    public static EmojiUnion formatDigitToDiscordEmoji(int digit) {
        //You cannot format theses unicodes inside Intellij, discord doesn't like it. WTF?
        final String unicode = switch(digit) {
            case 0 -> "0\uFE0F⃣";
            case 1 -> "1\uFE0F⃣";
            case 2 -> "2\uFE0F⃣";
            case 3 -> "3\uFE0F⃣";
            case 4 -> "4\uFE0F⃣";
            case 5 -> "5\uFE0F⃣";
            case 6 -> "6\uFE0F⃣";
            case 7 -> "7\uFE0F⃣";
            case 8 -> "8\uFE0F⃣";
            case 9 -> "9\uFE0F⃣";
            case 10 -> "\uD83D\uDD1F";
            default -> "❔";
        };

        return Emoji.fromFormatted(unicode);
    }

    /*
       Youtube Thumbnail
     */

    //Thanks to https://gist.github.com/jvanderwee/b30fdb496acff43aef8e
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
