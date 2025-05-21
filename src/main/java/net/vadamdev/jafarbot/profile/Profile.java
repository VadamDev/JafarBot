package net.vadamdev.jafarbot.profile;

import net.vadamdev.jafarbot.utils.JSONUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.Serializable;

/**
 * @author VadamDev
 * @since 09/06/2023
 */
public class Profile implements Serializable {
    private static final int ACTIVITY_BUFFER_SIZE = 8;

    private final String userId;

    private long[][] activityData;
    private transient long connectionTime;

    private transient CaptainedFleet captainedFleet;

    public Profile(String userId) {
        this.userId = userId;

        this.activityData = new long[ACTIVITY_BUFFER_SIZE][2];

        final long currentTime = System.currentTimeMillis();
        this.activityData[0][0] = currentTime;
        this.activityData[0][1] = currentTime;

        this.connectionTime = 0;

        this.captainedFleet = null;
    }

    public String getUserId() {
        return userId;
    }

    /*
       Activity
     */

    public void updateConnectionTime() {
        this.connectionTime = System.currentTimeMillis();
    }

    public void updateAndComputeDeconnectionTime() {
        if(connectionTime == 0)
            return;

        final long deconnectionTime = System.currentTimeMillis();
        if(deconnectionTime - connectionTime < 600000) {
            connectionTime = 0;
            return;
        }

        final long[][] newData = new long[ACTIVITY_BUFFER_SIZE][2];
        newData[0] = new long[] { connectionTime, deconnectionTime };

        for(int i = 1; i < activityData.length; i++)
            newData[i] = activityData[i - 1];

        activityData = newData;
        connectionTime = 0;
    }

    public long getLastActivity() {
        return Math.max(activityData[0][0], activityData[0][1]);
    }

    public long[][] getActivityData() {
        return activityData;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    /*
       Captained Boat
     */

    @Nullable
    public CaptainedFleet getCaptainedFleet() {
        return captainedFleet;
    }

    public void setCaptainedFleet(@Nullable CaptainedFleet captainedFleet) {
        this.captainedFleet = captainedFleet;
    }

    /*
       Utils
     */

    public boolean isInVC() {
        return connectionTime != 0;
    }

    public JSONObject toJSON(JSONParser parser) throws ParseException {
        final JSONObject json = (JSONObject) parser.parse(JSONUtils.getGson().toJson(this));

        if(captainedFleet != null)
            json.put("captainedBoat", captainedFleet.toJSON());

        return json;
    }

    @NotNull
    public static Profile fromJSON(JSONObject jsonObject) {
        final Profile result = JSONUtils.getGson().fromJson(jsonObject.toJSONString(), Profile.class);

        final JSONObject captainedBoatJson = (JSONObject) jsonObject.get("captainedBoat");
        if(captainedBoatJson != null)
            result.setCaptainedFleet(CaptainedFleet.fromJSON(captainedBoatJson));

        return result;
    }
}

