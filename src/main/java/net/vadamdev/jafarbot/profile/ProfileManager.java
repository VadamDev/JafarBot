package net.vadamdev.jafarbot.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.captaincy.CaptainedBoat;
import net.vadamdev.jafarbot.utils.JSONUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public final class ProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, Profile> cache;
    private final File profilesFile;

    private final ScheduledExecutorService executorService;

    public ProfileManager(File profilesFile) {
        this.cache = new HashMap<>();
        this.profilesFile = profilesFile;

        unserialize();

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(this::serialize, 6, 6, TimeUnit.HOURS);
    }

    public void onDisable() {
        executorService.shutdownNow();
        serialize();
    }

    public void deleteProfile(@Nonnull String userId) {
        cache.remove(userId);
    }

    @Nonnull
    public Profile getProfile(@Nonnull String userId) {
        return cache.computeIfAbsent(userId, Profile::new);
    }

    public Collection<Profile> getProfiles() {
        return cache.values();
    }

    /*
       Serialize & Unserialize
     */

    public void serialize() {
        final JSONArray jsonArray = new JSONArray();
        final JSONParser parser = new JSONParser();

        Main.logger.info("Serializing " + cache.size() + " profiles...");

        cache.forEach((userId, profile) -> {
            try {
                final JSONObject json = (JSONObject) parser.parse(GSON.toJson(profile));

                if(profile.getCaptainedBoat() != null)
                    json.put("captainedBoat", profile.getCaptainedBoat().toJsonObject());

                jsonArray.add(json);
            } catch (ParseException e) {
                Main.logger.error("An error occured while serializing " + profile.getUserId() + "'s profile:");
                e.printStackTrace();
            }
        });

        try {
            JSONUtils.saveJSONAwareToFile(jsonArray, profilesFile);
            Main.logger.info("Done !");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unserialize() {
        try {
            Main.logger.info("Unserializing profiles...");

            for (Object o : (JSONArray) JSONUtils.parseFile(profilesFile)) {
                final JSONObject jsonObject = (JSONObject) o;
                final Profile profile = GSON.fromJson(jsonObject.toJSONString(), Profile.class);

                final JSONObject captainedBoatJson = (JSONObject) jsonObject.get("captainedBoat");
                if(captainedBoatJson != null)
                    profile.setCaptainedBoat(CaptainedBoat.fromJsonObject(captainedBoatJson, profile.getUserId()));

                cache.put((String) jsonObject.get("userId"), profile);
            }

            Main.logger.info("Done !");
        } catch (IOException | ParseException e) {
            Main.logger.error("An error occured while unserializing profiles:");
            e.printStackTrace();
        }
    }
}
