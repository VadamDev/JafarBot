package net.vadamdev.jafarbot.captaincy.menu;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.captaincy.CaptainedBoat;
import net.vadamdev.jafarbot.utils.JafarEmbed;
import net.vadamdev.jdautils.smart.messages.MessageContent;
import net.vadamdev.jdautils.smart.messages.SmartMessageProvider;

/**
 * @author VadamDev
 * @since 23/01/2024
 */
public abstract class AbstractBoatSettingsMenu implements SmartMessageProvider {
    @Override
    public void init(Guild guild, String channelId, MessageContent contents) {
        Main.jafarBot.getCaptainedBoatManager().getCaptainedBoatByChannel(channelId).ifPresent(boat -> init(boat, contents));
    }

    public abstract void init(CaptainedBoat boat, MessageContent contents);

    protected boolean canUse(Member member, CaptainedBoat boat) {
        return boat.getOwnerId().equals(member.getId());
    }

    protected void replySuccessMessage(IReplyCallback callback) {
        callback.replyEmbeds(new JafarEmbed()
                .setTitle("JafarBot - Bateaux Capitainé")
                .setDescription("Vos changements on été sauvegarder !")
                .setColor(JafarEmbed.SUCCESS_COLOR)
                .build()).setEphemeral(true).queue();
    }
}
