package net.vadamdev.jafarbot.captaincy.menu;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.vadamdev.jafarbot.captaincy.CaptainedBoat;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jafarbot.utils.Utils;
import net.vadamdev.jdautils.smart.entities.SmartButton;
import net.vadamdev.jdautils.smart.messages.MessageContent;

import java.util.function.Consumer;

/**
 * @author VadamDev
 * @since 23/01/2024
 */
public class BoatSettingsDLMenu extends AbstractBoatSettingsMenu {
    @Override
    public void init(CaptainedBoat boat, MessageContent contents) {
/*
           Embed
         */

        contents.setEmbed(new JafarEmbed()
                .setTitle("JafarBot - Bateaux Capitainé")
                .setDescription(
                        "**Informations:**\n" +
                        "> Verrouiller par default: " + Utils.displayBoolean(boat.isDefaultLocked())
                )
                .setColor(JafarEmbed.NEUTRAL_COLOR)
                .build());

        /*
           Components
         */

        final boolean defaultLocked = boat.isDefaultLocked();

        final Button trueButton = Button.secondary("JafarBot-CaptainedBoat-Settings-DefaultLocked-True", Emoji.fromUnicode("✅"));
        final Button falseButton = Button.secondary("JafarBot-CaptainedBoat-Settings-DefaultLocked-False", Emoji.fromUnicode("❌"));

        final Consumer<ButtonInteractionEvent> consumer = event -> {
            boat.setDefaultLocked(!defaultLocked);

            replySuccessMessage(event);
            BoatSettingsMainMenu.MAIN_MENU.open(event.getMessage());
        };


        contents.addComponents(
                SmartButton.of(!defaultLocked ? trueButton : trueButton.asDisabled(), consumer),
                SmartButton.of(defaultLocked ? falseButton : falseButton.asDisabled(), consumer),

                SmartButton.of(Button.secondary("JafarBot-CaptainedBoat-SettingsMenu-Back", "Retour"), event -> {
                    if(!canUse(event.getMember(), boat))
                        return;

                    event.deferEdit().queue();
                    BoatSettingsMainMenu.MAIN_MENU.open(event.getMessage());
                })
        );
    }
}
