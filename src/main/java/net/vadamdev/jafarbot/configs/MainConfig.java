package net.vadamdev.jafarbot.configs;

import net.vadamdev.dbk.framework.config.Configuration;
import net.vadamdev.dbk.framework.config.annotations.ConfigRange;
import net.vadamdev.dbk.framework.config.annotations.ConfigValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class MainConfig extends Configuration {
    /*
       Features
     */

    @ConfigValue(path = "features.activityTracker", comment = "Should the activity tracker be enabled? This will automatically set member as inactive")
    public boolean ACTIVITY_TRACKER = false;

    /*
       Ids
     */

    @ConfigValue(path = "ids.welcome", comment = "Channel id where welcome messages will be sent")
    public String WELCOME_CHANNEL = "";

    /*
       Roles
     */

    @ConfigValue(path = "roles.onboardingRoles", comment = "A list of roles that will be given to new members")
    public List<String> ONBOARDING_ROLES = new ArrayList<>();

    @ConfigValue(path = "roles.retired", comment = "The role id of old Jafar's Armada members")
    public String RETIRED_ROLE = "";

    @ConfigValue(path = "roles.friend", comment = "The role id of friends of Jafar's Armada")
    public String FRIEND_ROLE = "";

    @ConfigValue(path = "roles.inactive", comment = "The role id given to inactive members")
    public String INACTIVE_ROLE = "";

    /*
       Music
     */

    @ConfigValue(
            path = "music.maxIdleTime",
            comment = """
                    Define the maximum number of seconds that the bot will stay "idling" in a vc before disconnect
                    The bot is considered as "idling" if:
                    1. The vc is empty
                    2. The bot is not playing music"""
    )
    public int MAX_IDLE_TIME = 60;

    @ConfigRange(min = 1, max = 100)
    @ConfigValue(path = "music.defaultVolume")
    public int DEFAULT_VOLUME = 25;

    @ConfigRange(min = 1, max = 100)
    @ConfigValue(path = "music.maxVolume")
    public int MAX_VOLUME = 75;

    /*
       Channel Creator
     */

    @ConfigValue(path = "channelCreator.boatCreator")
    public String BOAT_CREATOR = "";

    @ConfigValue(path = "channelCreator.boatCreatorCategory")
    public String BOAT_CREATOR_CATEGORY = "";

    @ConfigValue(path = "channelCreator.gamesCreator")
    public String GAMES_CREATOR = "";

    @ConfigValue(path = "channelCreator.gamesCreatorCategory")
    public String GAMES_CREATOR_CATEGORY = "";

    public MainConfig() {
        super("config.yml");
    }
}
