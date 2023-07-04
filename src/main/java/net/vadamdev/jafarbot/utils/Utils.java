package net.vadamdev.jafarbot.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

/**
 * @author VadamDev
 * @since 02/03/2023
 */
public final class Utils {
    public static boolean isInteger(@Nonnull String str) {
        try {
            Long.parseLong(str);
            return true;
        }catch (Exception ignored) {
            return false;
        }
    }

    @Nullable
    public static Member formatTarget(String memberId, Mentions mentions, Guild guild) {
        Member target = null;

        if(!mentions.getMembers().isEmpty())
            target = mentions.getMembers().get(0);
        else if(isInteger(memberId) && guild.getMemberById(memberId) != null)
            target = guild.getMemberById(memberId);

        return target;
    }

    @Nonnull
    public static File initFile(String pathName) throws IOException {
        final File file = new File(pathName);
        if(!file.exists())
            file.createNewFile();

        return file;
    }
}
