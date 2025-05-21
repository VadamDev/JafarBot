package net.vadamdev.jafarbot.profile;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.vadamdev.jafarbot.captaincy.BoatType;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author VadamDev
 * @since 12/04/2025
 */
public class CaptainedFleet {
    private final EnumMap<BoatType, String> names;

    private BoatType preferredBoatType;
    private boolean defaultLocked;

    private String openedChannelId;

    public CaptainedFleet(EnumMap<BoatType, String> names, BoatType preferredBoatType, boolean defaultLocked) {
        this.names = names;

        this.preferredBoatType = preferredBoatType;
        this.defaultLocked = defaultLocked;
    }

    public CaptainedFleet() {
        this(new EnumMap<>(BoatType.class), BoatType.GALLEON, false);
    }

    public String getNameByPreference() {
        if(names.containsKey(preferredBoatType))
            return names.get(preferredBoatType);

        return names.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .findFirst().map(Map.Entry::getValue)
                .orElse("Unknown");
    }

    public Collection<SelectOption> createNameSelectOptions() {
        return names.entrySet().stream()
                .map(entry -> {
                    final BoatType boatType = entry.getKey();

                    return boatType.toSelectOption()
                            .withDescription(entry.getValue())
                            .withDefault(preferredBoatType != null && preferredBoatType.equals(boatType));
                }).toList();
    }

    /*
       Getters & Setters
     */

    @Nullable
    public String getNameByBoatType(BoatType boatType) {
        if(!names.containsKey(boatType))
            return null;

        return names.get(boatType);
    }

    public void setName(BoatType boatType, @Nullable String name) {
        if(name == null)
            names.remove(boatType);
        else
            names.put(boatType, name);
    }

    public boolean isEmpty() {
        return names.isEmpty();
    }

    public BoatType getPreferredBoatType() {
        return preferredBoatType;
    }

    public void setPreferredBoatType(BoatType preferredBoatType) {
        this.preferredBoatType = preferredBoatType;
    }

    public boolean isDefaultLocked() {
        return defaultLocked;
    }

    public void setDefaultLocked(boolean defaultLocked) {
        this.defaultLocked = defaultLocked;
    }

    public boolean isInSea() {
        return openedChannelId != null;
    }

    @Nullable
    public String getOpenedChannelId() {
        return openedChannelId;
    }

    public void setOpenedChannelId(@Nullable String channelId) {
        this.openedChannelId = channelId;
    }

    /*
       Serialization
     */

    public JSONObject toJSON() {
        final JSONObject json = new JSONObject();

        json.put("name", names);
        json.put("prefferedBoatType", preferredBoatType); //Old typo in serialized name, too lazy to fix
        json.put("defaultLocked", defaultLocked);

        return json;
    }

    public static CaptainedFleet fromJSON(JSONObject json) {
        final EnumMap<BoatType, String> names = new EnumMap<>(BoatType.class);
        ((JSONObject) json.get("names")).forEach((k, v) -> names.put(BoatType.valueOf((String) k), (String) v));

        return new CaptainedFleet(
                names,
                BoatType.valueOf((String) json.get("prefferedBoatType")), //Old typo in serialized name, too lazy to fix
                (boolean) json.get("defaultLocked")
        );
    }
}
