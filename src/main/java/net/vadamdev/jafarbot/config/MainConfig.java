package net.vadamdev.jafarbot.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.vadamdev.jdautils.configuration.ConfigValue;
import net.vadamdev.jdautils.configuration.Configuration;

import javax.annotation.Nonnull;
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

    @ConfigValue(path = "roles.friend")
    public String FRIEND_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.separatorOne")
    public String SEPARATOR_1_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.separatorTwo")
    public String SEPARATOR_2_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.separatorThree")
    public String SEPARATOR_3_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.pvp")
    public String PVP_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.pve")
    public String PVE_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.tdm")
    public String TDM_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.helm")
    public String HELM_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.flex")
    public String FLEX_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.main")
    public String MAIN_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.matesearch")
    public String MATE_SEARCH_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.carpenter")
    public String CARPENTER_ROLE = "ID_HERE";

    @ConfigValue(path = "roles.stucked")
    public String STUCKED_ROLE = "ID_HERE";

    /*
       Channel Creator
     */

    @ConfigValue(path = "channelCreator.boatCreator")
    public String BOAT_CREATOR = "ID_HERE";

    @ConfigValue(path = "channelCreator.boatCreatorCategory")
    public String BOAT_CREATOR_CATEGORY = "ID_HERE";

    @ConfigValue(path = "channelCreator.gamesCreator")
    public String GAMES_CREATOR = "ID_HERE";

    @ConfigValue(path = "channelCreator.gamesCreatorCategory")
    public String GAMES_CREATOR_CATEGORY = "ID_HERE";

    @ConfigValue(path = "channelCreator.captainedBoatCreator")
    public String CAPTAINED_BOAT_CREATOR = "ID_HERE";

    @ConfigValue(path = "channelCreator.captainedBoatCreatorCategory")
    public String CAPTAINED_BOAT_CREATOR_CATEGORY = "ID_HERE";

    /*
       Music
     */

    @ConfigValue(path = "music.defaultVolume")
    public int MUSIC_DEFAULT_VOLUME = 25;

    @ConfigValue(path = "music.maxVolume")
    public int MUSIC_MAX_VOLUME = 100;

    public MainConfig() {
        super("JafarBot/config.yml");
    }

    public Activity formatActivity() {
        return Activity.of(Activity.ActivityType.valueOf(ACTIVITY_TYPE), ACTIVITY);
    }

    public void updateActivity(@Nonnull JDA jda, @Nonnull Activity.ActivityType activityType, @Nonnull String activity) throws IOException {
        setValue("ACTIVITY_TYPE", activityType.name());
        setValue("ACTIVITY", activity);
        save();

        jda.getPresence().setActivity(formatActivity());
    }
}
