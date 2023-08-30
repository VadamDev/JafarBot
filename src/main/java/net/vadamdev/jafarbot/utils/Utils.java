package net.vadamdev.jafarbot.utils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * @author VadamDev
 * @since 02/03/2023
 */
public final class Utils {
    @Nonnull
    public static File initFile(String pathName) throws IOException {
        final File file = new File(pathName);
        if(!file.exists())
            file.createNewFile();

        return file;
    }
}
