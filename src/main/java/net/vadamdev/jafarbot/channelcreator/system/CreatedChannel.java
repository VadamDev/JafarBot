package net.vadamdev.jafarbot.channelcreator.system;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

/**
 * @author VadamDev
 * @since 28/08/2023
 */
public class CreatedChannel {
    protected final String channelId;
    protected final String ownerId;

    public CreatedChannel(String channelId, String ownerId) {
        this.channelId = channelId;
        this.ownerId = ownerId;
    }

    protected void onChannelCreation(VoiceChannel voiceChannel, Member owner) {}

    protected void handleButtonInteractionEvent(ButtonInteractionEvent event) {}
    protected void handleSelectInteractionEvent(StringSelectInteractionEvent event) {}
    protected void handleModalInteractionEvent(ModalInteractionEvent event) {}

    public String getChannelId() {
        return channelId;
    }

    public String getOwnerId() {
        return ownerId;
    }
}
