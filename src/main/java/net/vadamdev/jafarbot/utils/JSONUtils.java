package net.vadamdev.jafarbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONAware;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * @author VadamDev
 * @since 05/04/2023
 */
public final class JSONUtils {
    private JSONUtils() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Gson getGson() { return GSON; }

    public static <T extends JSONAware> T parseFile(File file, Supplier<T> fallback) throws ParseException, IOException, ClassCastException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        final StringBuilder jsonString = new StringBuilder();

        String line;
        while((line = reader.readLine()) != null)
            jsonString.append(line);

        reader.close();

        if(jsonString.isEmpty())
            return fallback.get();

        return (T) new JSONParser().parse(jsonString.toString());
    }

    public static void saveJSONAwareToFile(JSONAware jsonAware, File file) throws IOException {
        final BufferedWriter writter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
        writter.write(GSON.toJson(jsonAware));
        writter.close();
    }
}
