package net.vadamdev.jafarbot.channelcreator.impl;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.vadamdev.jafarbot.Main;
import net.vadamdev.jafarbot.channelcreator.ChannelCreator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.NameParser;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author VadamDev
 * @since 27/08/2023
 */
public class GamesChannelCreator extends ChannelCreator<GamesChannelCreator.GamesCreatedChannel> {
    private static final String NAME_PREFIX = "\uD83C\uDFAE┃";

    public GamesChannelCreator() {
        super(() -> Main.jafarBot.mainConfig.GAMES_CREATOR, GamesCreatedChannel.class);
    }

    @Nonnull
    @Override
    protected String getChannelName(Member owner) {
        final List<Activity> activities = owner.getActivities().stream()
                .filter(activity -> activity.getType().equals(Activity.ActivityType.PLAYING))
                .collect(Collectors.toList());

        if(!activities.isEmpty()) {
            final String activity = activities.get(0).getName();

            String formattedActivity = activity;
            if(activity.length() > 10) {
                if(getUpperCaseAmount(activity) > 1) {
                    final StringBuilder b = new StringBuilder();

                    for (int i = 0; i < activity.length(); i++) {
                        final char c = activity.charAt(i);

                        if(Character.isUpperCase(c) || Character.isSpaceChar(c) || Character.isDigit(c))
                            b.append(c);
                    }

                    if(b.length() > 10)
                        formattedActivity = b.substring(0, 11);
                    else
                        formattedActivity = b.toString();
                }else
                    formattedActivity = activity.substring(0, 11);
            }

            return NAME_PREFIX + formattedActivity;
        }else
            return NAME_PREFIX + "Vocal #%index%";
    }

    @Nullable
    @Override
    protected String getCategoryId(Member owner) {
        return Main.jafarBot.mainConfig.GAMES_CREATOR_CATEGORY;
    }

    private int getUpperCaseAmount(String str) {
        int j = 0;

        for (int i = 0; i < str.length(); i++) {
            if(Character.isUpperCase(str.charAt(i)))
                j++;
        }

        return j;
    }

    public static class GamesCreatedChannel extends LockeableCreatedChannel {
        public GamesCreatedChannel(String channelId, String ownerId) {
            super(channelId, ownerId);
        }

        @Override
        public void handleButtonInteractionEvent(@Nonnull ButtonInteractionEvent event) {
            if(event.getComponentId().equals("jafarBot-GamesChannel-Rename")) {
                if(!isOwner(event.getMember().getId(), event))
                    return;

                event.replyModal(Modal.create("jafarBot-GamesChannel-NameModal", "PLACEHOLDER")
                        .addComponents(ActionRow.of(
                                TextInput.create("jafarBot-GamesChannel-NewName", "Nom du salon", TextInputStyle.SHORT)
                                        .setMinLength(1)
                                        .setMaxLength(10).build())
                        ).build()).queue();
            }else
                super.handleButtonInteractionEvent(event);
        }

        @Override
        public void handleModalInteractionEvent(@Nonnull ModalInteractionEvent event) {
            if(!event.getModalId().equals("jafarBot-GamesChannel-NameModal"))
                return;

            event.getChannel().asVoiceChannel().getManager().setName(NAME_PREFIX + event.getValue("jafarBot-GamesChannel-NewName").getAsString()).queue();
            event.deferEdit().queue();
        }

        @Override
        protected ItemComponent[] getComponents() {
            return new ItemComponent[] {
                    Button.primary("jafarBot-LockeableChannel-Lock", Emoji.fromUnicode(isLocked() ? "\uD83D\uDD13" : "\uD83D\uDD12")),
                    Button.primary("jafarBot-GamesChannel-Rename", Emoji.fromUnicode("✒")),
                    Button.danger("jafarBot-LockeableChannel-Delete", Emoji.fromUnicode("\uD83D\uDDD1️"))
            };
        }
    }
}
