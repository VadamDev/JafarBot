package net.vadamdev.jafarbot.utils;

import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author VadamDev
 * @since 05/04/2023
 */
public final class JSONUtils {
    public static Object parseFile(File file) throws ParseException, IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_16));
        final StringBuilder jsonString = new StringBuilder();

        String line;
        while((line = reader.readLine()) != null)
            jsonString.append(line);

        reader.close();

        if(jsonString.toString().equals(""))
            return new JSONArray();

        return new JSONParser().parse(jsonString.toString());
    }

    public static void saveJSONAwareToFile(JSONAware jsonAware, File file) throws IOException {
        final BufferedWriter writter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_16));
        writter.write(new GsonBuilder().setPrettyPrinting().create().toJson(jsonAware));
        writter.close();
    }
}
