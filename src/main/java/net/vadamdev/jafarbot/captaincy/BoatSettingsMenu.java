package net.vadamdev.jafarbot.captaincy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.vadamdev.jafarbot.JafarBot;
import net.vadamdev.jafarbot.profile.CaptainedFleet;
import net.vadamdev.jafarbot.utils.EmbedUtils;
import net.vadamdev.jafarbot.utils.ShadowComponent;
import net.vadamdev.jafarbot.utils.Utils;

/**
 * @author VadamDev
 * @since 11/05/2025
 */
public final class BoatSettingsMenu {
    private BoatSettingsMenu() {}

    private static final String PREFERRED_TYPE_SELECT_MENU_ID;
    private static final String LOCKING_SELECT_MENU_ID;

    static {
        final JDA jda = JafarBot.get().getJDA();

        //Preferred type parameter
        PREFERRED_TYPE_SELECT_MENU_ID = "JafarBot-CaptainedBoat-Settings-PreferredType";
        ShadowComponent.of(PREFERRED_TYPE_SELECT_MENU_ID, event -> {
            final CaptainedFleet fleet = JafarBot.get().getProfileManager().getOrCreateProfile(event.getUser().getId()).getCaptainedFleet();
            if(fleet == null)
                return;

            try {
                fleet.setPreferredBoatType(BoatType.valueOf(event.getSelectedOptions().get(0).getValue()));
                event.getInteraction().editMessageEmbeds(createMenuEmbed(fleet)).setComponents(createMenuComponents(fleet)).queue();
            }catch (Exception ignored) {}
        }, StringSelectInteractionEvent.class).register(jda);

        // Default locked parameter
        LOCKING_SELECT_MENU_ID = "JafarBot-CaptainedBoat-Settings-BoatLocking";
        ShadowComponent.of(LOCKING_SELECT_MENU_ID, event -> {
            final CaptainedFleet fleet = JafarBot.get().getProfileManager().getOrCreateProfile(event.getUser().getId()).getCaptainedFleet();
            if(fleet == null)
                return;

            try {
                fleet.setDefaultLocked(Boolean.valueOf(event.getSelectedOptions().get(0).getValue()));
                event.getInteraction().editMessageEmbeds(createMenuEmbed(fleet)).setComponents(createMenuComponents(fleet)).queue();
            }catch (Exception ignored) {}
        }, StringSelectInteractionEvent.class).register(jda);
    }

    public static void open(CaptainedFleet fleet, IReplyCallback callback) {
        callback.replyEmbeds(createMenuEmbed(fleet)).setComponents(createMenuComponents(fleet)).setEphemeral(true).queue();
    }

    private static MessageEmbed createMenuEmbed(CaptainedFleet fleet) {
        final BoatType preferredBoatType = fleet.getPreferredBoatType();

        String preferredBoatName = fleet.getNameByBoatType(preferredBoatType);
        if(preferredBoatName == null)
            preferredBoatName = preferredBoatType.getDisplayName();

        return EmbedUtils.defaultEmbed()
                .setTitle("Bateau Capitainé - Paramètres")
                .addField("Bateau préféré", "> ``" + preferredBoatName + "``", true)
                .addField("Verrouiller par default", "> ``" + Utils.formatBoolean(fleet.isDefaultLocked()) + "``", true).build();
    }

    private static ActionRow[] createMenuComponents(CaptainedFleet fleet) {
        return new ActionRow[] {
                ActionRow.of(
                        StringSelectMenu.create(PREFERRED_TYPE_SELECT_MENU_ID).addOptions(fleet.createNameSelectOptions()).build()
                ),

                ActionRow.of(
                        StringSelectMenu.create(LOCKING_SELECT_MENU_ID).addOptions(
                                SelectOption.of("Oui", "true")
                                        .withEmoji(Emoji.fromUnicode("✅"))
                                        .withDescription("Le salon sera verrouillé par défaut")
                                        .withDefault(fleet.isDefaultLocked()),
                                SelectOption.of("Non", "false")
                                        .withEmoji(Emoji.fromUnicode("❌"))
                                        .withDescription("Le salon ne sera pas verrouillé par défaut")
                                        .withDefault(!fleet.isDefaultLocked())
                        ).build()
                )
        };
    }
}
