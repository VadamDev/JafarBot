package net.vadamdev.jafarbot.profile;

import net.vadamdev.dbk.framework.DBKFramework;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.utils.JSONUtils;
import net.vadamdev.jafarbot.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class ProfileManager {
    private final Map<String, Profile> profiles;
    private final File dataFile;

    public ProfileManager(String dataFilePath) throws IOException {
        this.profiles = new HashMap<>();
        this.dataFile = Utils.createFile(dataFilePath);

        deserialize();

        DBKFramework.getScheduledExecutorMonoThread().scheduleAtFixedRate(this::serialize, 6, 6, TimeUnit.HOURS);
    }

    @NotNull
    public Profile getOrCreateProfile(String userId) {
        return profiles.computeIfAbsent(userId, Profile::new);
    }

    public void deleteProfile(String userId) {
        profiles.remove(userId);
    }

    public Collection<Profile> getProfiles() {
        return profiles.values();
    }

    /*
       Serialization
     */

    public void serialize() {
        final Logger logger = JafarBot.getLogger();

        final JSONArray jsonArray = new JSONArray();
        final JSONParser parser = new JSONParser();

        logger.info("Serializing " + profiles.size() + " profiles to " + dataFile.getName() + "...");

        profiles.forEach((userId, profile) -> {
            try {
                jsonArray.add(profile.toJSON(parser));
            }catch (Exception e) {
                logger.error("Failed to serialize profile for user " + userId, e);
            }
        });

        try {
            JSONUtils.saveJSONAwareToFile(jsonArray, dataFile);
            logger.info("-> Done !");
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void deserialize() {
        final Logger logger = JafarBot.getLogger();
        logger.info("Deserializing profiles from " + dataFile.getName() + "...");

        try {
            for(Object o : JSONUtils.parseFile(dataFile, JSONArray::new)) {
                final JSONObject jsonObject = (JSONObject) o;
                profiles.put((String) jsonObject.get("userId"), Profile.fromJSON(jsonObject));
            }

            logger.info("-> Done !");
        }catch (ParseException | IOException e) {
            logger.error("Failed to deserialize profiles", e);
        }
    }
}
