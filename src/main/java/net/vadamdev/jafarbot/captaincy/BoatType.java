package net.vadamdev.jafarbot.captaincy;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

/**
 * @author VadamDev
 */
public enum BoatType {
    SLOOP("Sloop", "\uD83D\uDEA4", 2),
    BRIGANTINE("Brigantin", "⛵", 3),
    GALLEON("Galion", "\uD83D\uDEA2", 4);

    private final String displayName, icon;
    private final int crewSize;

    BoatType(String displayName, String icon, int crewSize) {
        this.displayName = displayName;
        this.icon = icon;
        this.crewSize = crewSize;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCrewSize() {
        return crewSize;
    }

    public SelectOption toSelectOption() {
        return SelectOption.of(displayName, name()).withEmoji(Emoji.fromUnicode(icon));
    }

    public static Command.Choice[] asChoicesList() {
        final BoatType[] values = values();
        final Command.Choice[] result = new Command.Choice[values.length];

        for(int i = 0; i < values.length; i++)
            result[i] = new Command.Choice(values[i].getDisplayName(), values[i].name());

        return result;
    }
}
