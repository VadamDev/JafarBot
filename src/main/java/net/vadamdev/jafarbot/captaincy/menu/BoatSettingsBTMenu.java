package net.vadamdev.jafarbot.captaincy.menu;

import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.vadamdev.jafarbot.captaincy.BoatType;
import net.vadamdev.jafarbot.captaincy.CaptainedBoat;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jdautils.smart.entities.SmartButton;
import net.vadamdev.jdautils.smart.entities.SmartStringSelectMenu;
import net.vadamdev.jdautils.smart.messages.MessageContent;

/**
 * @author VadamDev
 * @since 23/01/2024
 */
public class BoatSettingsBTMenu extends AbstractBoatSettingsMenu {
    @Override
    public void init(CaptainedBoat boat, MessageContent contents) {
        final BoatType preferredType = boat.getPreferredBoatType();

        /*
           Embed
         */

        contents.setEmbed(new JafarEmbed()
                .setTitle("JafarBot - Bateaux Capitainé")
                .setDescription(
                        "**Informations:**\n" +
                        "> Type préféré: " + preferredType.getDisplayName()
                )
                .setColor(JafarEmbed.NEUTRAL_COLOR)
                .build());

        /*
           Components
         */

        final StringSelectMenu menu = StringSelectMenu.create("JafarBot-CaptainedBoat-Settings-BoatTypeSelectMenu")
                .addOptions(
                        BoatType.GALLEON.toSelectOption(),
                        BoatType.BRIGANTINE.toSelectOption(),
                        BoatType.SLOOP.toSelectOption()
                )
                .setDefaultOptions(preferredType.toSelectOption()).build();

        contents.addComponents(
                SmartStringSelectMenu.of(menu, event -> {
                    if(!canUse(event.getMember(), boat))
                        return;

                    boat.setPreferredBoatType(BoatType.valueOf(event.getValues().get(0)));

                    replySuccessMessage(event);
                    BoatSettingsMainMenu.MAIN_MENU.open(event.getMessage());
                })
        );

        contents.addComponents(
                SmartButton.of(Button.danger("JafarBot-CaptainedBoat-Settings-Back", "Retour"), event -> {
                    if(!canUse(event.getMember(), boat))
                        return;

                    event.deferEdit().queue();
                    BoatSettingsMainMenu.MAIN_MENU.open(event.getMessage());
                })
        );
    }
}
