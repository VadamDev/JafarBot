package net.vadamdev.jafarbot.captaincy;

public enum BoatType {
    SLOOP("Sloop", "\uD83D\uDEA4", 2),
    BRIGANTINE("Brigantin", "⛵", 3),
    GALLEON("Galion", "\uD83D\uDEA2", 4);

    private final String displayName, icon;
    private final int maxCrewSize;

    BoatType(String displayName, String icon, int maxCrewSize) {
        this.displayName = displayName;
        this.icon = icon;
        this.maxCrewSize = maxCrewSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public int getMaxCrewSize() {
        return maxCrewSize;
    }
}
