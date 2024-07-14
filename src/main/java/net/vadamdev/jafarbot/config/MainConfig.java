package net.vadamdev.jafarbot.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.vadamdev.jdautils.configuration.ConfigValue;
import net.vadamdev.jdautils.configuration.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class MainConfig extends Configuration {
    /*
       Activity
     */

    @ConfigValue(path = "activity.activityType")
    public String ACTIVITY_TYPE = Activity.ActivityType.PLAYING.name();

    @ConfigValue(path = "activity.activity")
    public String ACTIVITY = "Sea of Thieves";

    /*
       Features
     */

    @ConfigValue(path = "features.activityTracker")
    public boolean ACTIVITY_TRACKER = true;

    /*
       Ids
     */

    @ConfigValue(path = "ids.guild")
    public String GUILD_ID = "ID_HERE";

    @ConfigValue(path = "ids.welcome")
    public String WELCOME_CHANNEL = "ID_HERE";

    /*
       Roles
     */

    @ConfigValue(path = "roles.member")
    public String MEMBER_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.retired")
    public String RETIRED_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.friend")
    public String FRIEND_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.inactive")
    public String INACTIVE_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.separatorOne")
    public String SEPARATOR_1_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.separatorTwo")
    public String SEPARATOR_2_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.separatorThree")
    public String SEPARATOR_3_ROLE = "ID_HERE";

    /*
       Channel Creator
     */

    @ConfigValue(path = "channelCreator.boatCreator")
    public String BOAT_CREATOR = "ID_HERE";

    @ConfigValue(path = "channelCreator.boatCreatorCategory")
    public String BOAT_CREATOR_CATEGORY = "ID_HERE";

    @ConfigValue(path = "channelCreator.captainedBoatCreator")
    public String CAPTAINED_BOAT_CREATOR = "ID_HERE";

    @ConfigValue(path = "channelCreator.captainedBoatCreatorCategory")
    public String CAPTAINED_BOAT_CREATOR_CATEGORY = "ID_HERE";

    @ConfigValue(path = "channelCreator.gamesCreator")
    public String GAMES_CREATOR = "ID_HERE";

    @ConfigValue(path = "channelCreator.gamesCreatorCategory")
    public String GAMES_CREATOR_CATEGORY = "ID_HERE";

    /*
       Music
     */

    @ConfigValue(path = "music.defaultVolume")
    public int MUSIC_DEFAULT_VOLUME = 15;

    @ConfigValue(path = "music.maxVolume")
    public int MUSIC_MAX_VOLUME = 100;

    public MainConfig() {
        super("./config.yml");
    }

    @Nullable
    public Activity formatActivity() {
        if(ACTIVITY_TYPE == null || ACTIVITY == null)
            return null;

        return Activity.of(Activity.ActivityType.valueOf(ACTIVITY_TYPE), ACTIVITY);
    }

    public void updateActivity(@Nonnull JDA jda, @Nullable Activity.ActivityType activityType, @Nullable String activity) throws IOException {
        setValue("ACTIVITY_TYPE", activityType != null ? activityType.name() : null);
        setValue("ACTIVITY", activity);
        save();

        jda.getPresence().setActivity(formatActivity());
    }
}
