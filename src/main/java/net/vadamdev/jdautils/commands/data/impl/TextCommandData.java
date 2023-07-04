package net.vadamdev.jdautils.commands.data.impl;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.vadamdev.jdautils.commands.data.ICommandData;

import javax.annotation.Nonnull;

/**
 * @author VadamDev
 * @since 08/06/2023
 */
public class TextCommandData implements ICommandData {
    private final MessageReceivedEvent event;
    private final String[] args;

    public TextCommandData(MessageReceivedEvent event, String[] args) {
        this.event = event;
        this.args = args;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public String[] getArgs() {
        return args;
    }

    @Nonnull
    @Override
    public Type getType() {
        return Type.TEXT;
    }
}
