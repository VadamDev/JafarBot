package net.vadamdev.jafarbot.captaincy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jafarbot.utils.Utils;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CaptainedBoat {
    private final String ownerId;
    private final EnumMap<BoatType, String> names;

    private BoatType preferredBoatType;
    private boolean defaultLocked;

    private String channelId, currentName, configMessageId;
    private boolean locked, heavyLocked;

    CaptainedBoat(String ownerId, EnumMap<BoatType, String> names, BoatType preferredBoatType, boolean defaultLocked) {
        this.ownerId = ownerId;
        this.names = names;
        this.preferredBoatType = preferredBoatType;
        this.defaultLocked = defaultLocked;
    }

    CaptainedBoat(String ownerId) {
        this(ownerId, new EnumMap<>(BoatType.class), BoatType.GALLEON, false);
    }

    /*
       Create / Delete
     */

    void createChannel(Guild guild) {
        final Member owner = guild.getMemberById(ownerId);

        if(isAlive()) {
            guild.moveVoiceMember(owner, guild.getVoiceChannelById(channelId)).queue();
            return;
        }

        currentName = getNameByPreference();
        guild.createVoiceChannel(currentName, guild.getCategoryById(Main.jafarBot.mainConfig.CAPTAINED_BOAT_CREATOR_CATEGORY)).setUserlimit(preferredBoatType != null && defaultLocked ? preferredBoatType.getCrewSize() : 0).queue(channel -> {
            channelId = channel.getId();

            updateOrCreateConfigMessage(guild);
            guild.moveVoiceMember(owner, channel).queue();
        });
    }

    void deleteChannel(Guild guild, boolean skipDelete) {
        if(!isAlive())
            return;

        if(!skipDelete) {
            final VoiceChannel voiceChannel = guild.getVoiceChannelById(channelId);

            if(voiceChannel != null)
                voiceChannel.delete().queue();
        }

        channelId = null;
        currentName = null;
        configMessageId = null;
        locked = false;
        heavyLocked = false;
    }

    /*
       Lock / Unlock
     */

    void setLocked(Guild guild, boolean locked, boolean heavy) {
        final VoiceChannel voiceChannel = guild.getVoiceChannelById(channelId);
        if(voiceChannel == null)
            return;

        if(locked && !this.locked) {
            if(heavy)
                heavyLocked = true;

            final int memberSize = voiceChannel.getMembers().size();
            voiceChannel.getManager().setUserLimit(memberSize <= 1 ? 2 : Math.min(memberSize, 4)).queue(channel -> updateOrCreateConfigMessage(guild));
        }else if(!locked && this.locked) {
            heavyLocked = false;
            voiceChannel.getManager().setUserLimit(0).queue(channel -> updateOrCreateConfigMessage(guild));
        }

        this.locked = locked;
    }

    /*
       Config Message
     */

    private void updateOrCreateConfigMessage(Guild guild) {
        final VoiceChannel voiceChannel = guild.getVoiceChannelById(channelId);
        if(voiceChannel == null)
            return;

        final Member owner = guild.getMemberById(ownerId);

        if(configMessageId != null) {
            voiceChannel.retrieveMessageById(configMessageId).queue(message -> {
                final Button forceLockButton = Button.secondary("JafarBot-CaptainedBoat-ForceLock", Emoji.fromUnicode("⛔"));

                message.editMessageEmbeds(createConfigMessage(owner.getUser())).setActionRow(
                        Button.secondary("JafarBot-CaptainedBoat-Lock", Emoji.fromUnicode(voiceChannel.getUserLimit() != 0 ? "\uD83D\uDD13" : "\uD83D\uDD12")),
                        owner.hasPermission(Permission.ADMINISTRATOR) && !locked && !heavyLocked ? forceLockButton : forceLockButton.asDisabled(),
                        Button.secondary("JafarBot-CaptainedBoat-Delete", Emoji.fromUnicode("\uD83D\uDDD1️")),
                        Button.secondary("JafarBot-CaptainedBoat-Settings", Emoji.fromUnicode("⚙️"))
                ).queue();
            });
        }else {
            voiceChannel.sendMessageEmbeds(createTypeConfigMessage())
                    .setActionRow(StringSelectMenu.create("JafarBot-CaptainedBoat-ChooseBoatType").addOptions(getBoatNameOptions()).build()).queue();

            final Button forceLockButton = Button.secondary("JafarBot-CaptainedBoat-ForceLock", Emoji.fromUnicode("⛔"));
            voiceChannel.sendMessageEmbeds(createConfigMessage(owner.getUser())).setActionRow(
                    Button.secondary("JafarBot-CaptainedBoat-Lock", Emoji.fromUnicode(voiceChannel.getUserLimit() != 0 ? "\uD83D\uDD13" : "\uD83D\uDD12")),
                    owner.hasPermission(Permission.ADMINISTRATOR) && !locked && !heavyLocked ? forceLockButton : forceLockButton.asDisabled(),
                    Button.secondary("JafarBot-CaptainedBoat-Delete", Emoji.fromUnicode("\uD83D\uDDD1️")),
                    Button.secondary("JafarBot-CaptainedBoat-Settings", Emoji.fromUnicode("⚙️"))
            ).queue(message -> configMessageId = message.getId());
        }
    }

    private MessageEmbed createConfigMessage(User owner) {
        return new JafarEmbed()
                .setTitle(currentName.split("┃")[1] + " (Commandé par " + owner.getEffectiveName() + ")")
                .setDescription(
                        "**Informations:**\n" +
                        "> Status: " + (locked ? "\uD83D\uDD12" : "\uD83D\uDD13") + "\n" +
                        "> ForceLock: " + Utils.displayBoolean(heavyLocked) + "\n" +
                        "\n" +
                        "**Boutons:**\n" +
                        "> " + (!locked ? "\uD83D\uDD12" : "\uD83D\uDD13") + " *: " + (locked ? "Déverrouiller" : "Verrouiller") + " le salon*\n" +
                        "> ⛔ *: Force lock le salon __(Admin uniquement)__*\n" +
                        "> \uD83D\uDDD1 *: Fermer le salon*\n" +
                        "> ⚙️ *: Paramètres*"
                ).setColor(JafarEmbed.NEUTRAL_COLOR).build();
    }

    private MessageEmbed createTypeConfigMessage() {
        return new JafarEmbed()
                .setTitle("Type de bateau")
                .setDescription(
                        "Choisissez dans la liste déroulante le type de bateau que vous souhaitez utiliser."
                ).setColor(JafarEmbed.NEUTRAL_COLOR).build();
    }

    private String getNameByPreference() {
        if(preferredBoatType != null && names.containsKey(preferredBoatType))
            return names.get(preferredBoatType);

        final Optional<Map.Entry<BoatType, String>> optional = names.entrySet().stream().filter(entry -> entry.getValue() != null).findFirst();
        if(optional.isPresent())
            return optional.get().getValue();
        else
            return "Unknown";
    }

    /*
       Utils
     */

    void setNameByBoatType(Guild guild, BoatType boatType) {
        if(!isAlive() || !names.containsKey(boatType))
            return;

        currentName = names.get(boatType);
        guild.getVoiceChannelById(channelId).getManager().setName(currentName).queue();

        updateOrCreateConfigMessage(guild);
    }

    private Collection<SelectOption> getBoatNameOptions() {
        final List<SelectOption> options = new ArrayList<>();

        for(BoatType boatType : BoatType.values()) {
            if(!names.containsKey(boatType))
                continue;

            options.add(boatType.toSelectOption()
                    .withDescription(names.get(boatType))
                    .withDefault(preferredBoatType != null && preferredBoatType.equals(boatType)));
        }

        return options;
    }

    /*
       Getters & Setters
     */

    public String getOwnerId() {
        return ownerId;
    }

    String getChannelId() {
        return channelId;
    }

    public BoatType getPreferredBoatType() {
        return preferredBoatType;
    }

    public boolean isDefaultLocked() {
        return defaultLocked;
    }

    boolean isAlive() {
        return channelId != null;
    }

    boolean isLocked() {
        return locked;
    }

    boolean isHeavyLocked() {
        return heavyLocked;
    }

    public void setName(@Nonnull BoatType boatType, String name) {
        if(name == null)
            names.remove(boatType);
        else
            names.put(boatType, name);
    }

    public void setPreferredBoatType(@Nullable BoatType preferredBoatType) {
        this.preferredBoatType = preferredBoatType;
    }

    public void setDefaultLocked(boolean defaultLocked) {
        this.defaultLocked = defaultLocked;
    }

    /*
       Serialization
     */

    public JSONObject toJsonObject() {
        final JSONObject jsonObject = new JSONObject();

        jsonObject.put("names", names);
        jsonObject.put("prefferedBoatType", preferredBoatType);
        jsonObject.put("defaultLocked", defaultLocked);

        return jsonObject;
    }

    public static CaptainedBoat fromJsonObject(JSONObject jsonObject, String ownerId) {
        final EnumMap<BoatType, String> names = new EnumMap<>(BoatType.class);
        ((JSONObject) jsonObject.get("names")).forEach((k, v) -> names.put(BoatType.valueOf((String) k), (String) v));

        return new CaptainedBoat(
                ownerId,
                names,
                BoatType.valueOf((String) jsonObject.get("prefferedBoatType")),
                (boolean) jsonObject.get("defaultLocked"));
    }
}
