package net.vadamdev.jafarbot.profile;

import net.vadamdev.jafarbot.captaincy.CaptainedBoat;

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

    private transient CaptainedBoat captainedBoat;

    public Profile(String userId) {
        this.userId = userId;

        this.activityData = new long[ACTIVITY_BUFFER_SIZE][2];

        final long currentTime = System.currentTimeMillis();
        this.activityData[0][0] = currentTime;
        this.activityData[0][1] = currentTime;

        this.connectionTime = 0;

        this.captainedBoat = null;
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
        newData[0] = new long[]{connectionTime, deconnectionTime};

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

    public CaptainedBoat getCaptainedBoat() {
        return captainedBoat;
    }

    public void setCaptainedBoat(CaptainedBoat captainedBoat) {
        this.captainedBoat = captainedBoat;
    }

    /*
       Utils
     */

    public boolean isInVocal() {
        return connectionTime != 0;
    }
}
