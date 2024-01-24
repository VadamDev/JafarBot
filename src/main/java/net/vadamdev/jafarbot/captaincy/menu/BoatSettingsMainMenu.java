package net.vadamdev.jafarbot.captaincy.menu;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.vadamdev.jafarbot.captaincy.CaptainedBoat;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jafarbot.utils.Utils;
import net.vadamdev.jdautils.smart.SmartInteractionsManager;
import net.vadamdev.jdautils.smart.entities.SmartButton;
import net.vadamdev.jdautils.smart.messages.MessageContent;
import net.vadamdev.jdautils.smart.messages.SmartMessage;

/**
 * @author VadamDev
 * @since 23/01/2024
 */
public class BoatSettingsMainMenu extends AbstractBoatSettingsMenu {
    public static final SmartMessage MAIN_MENU = SmartMessage.fromProvider(new BoatSettingsMainMenu());

    private static final SmartMessage DEFAULT_LOCKED_MENU = SmartMessage.fromProvider(new BoatSettingsDLMenu());
    private static final SmartMessage BOAT_TYPE_MENU = SmartMessage.fromProvider(new BoatSettingsBTMenu());

    @Override
    public void init(CaptainedBoat boat, MessageContent contents) {
        /*
           Embed
         */

        contents.setEmbed(new JafarEmbed()
                .setTitle("JafarBot - Bateaux Capitainé")
                .setDescription(
                        "**Informations:**\n" +
                        "> Verrouiller par default: " + Utils.displayBoolean(boat.isDefaultLocked()) + "\n" +
                        "> Type préféré: " + boat.getPreferredBoatType().getDisplayName() + "\n" +
                        "\n" +
                        "**Boutons:**\n" +
                        "> \uD83D\uDD12 *: Choisis si ton bateau sera verrouiller par default*\n" +
                        "> ⛵ *: Choisis quel type de bateau sera créé dans le <#1087574158680018945>*"
                )
                .setColor(JafarEmbed.NEUTRAL_COLOR)
                .build());

        /*
           Components
         */

        contents.addComponents(
                SmartButton.of(Button.secondary("JafarBot-CaptainedBoat-Settings-DefaultLocked", Emoji.fromUnicode("\uD83D\uDD12")), event -> {
                    if(!canUse(event.getMember(), boat))
                        return;

                    event.deferEdit().queue();
                    DEFAULT_LOCKED_MENU.open(event.getMessage());
                }),

                SmartButton.of(Button.secondary("JafarBot-CaptainedBoat-Settings-BoatType", Emoji.fromUnicode("⛵")), event -> {
                    if(!canUse(event.getMember(), boat))
                        return;

                    event.deferEdit().queue();
                    BOAT_TYPE_MENU.open(event.getMessage());
                }),

                SmartButton.of(Button.danger("JafarBot-CaptainedBoat-Settings-Close", "Fermer"), event -> {
                    if(!canUse(event.getMember(), boat))
                        return;

                    event.deferEdit().queue();

                    SmartInteractionsManager.unregisterSmartMessage(event.getGuild().getId(), event.getMessageId());
                    event.getMessage().delete().queue();
                })
        );
    }
}
