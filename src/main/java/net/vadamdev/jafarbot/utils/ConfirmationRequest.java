package net.vadamdev.jafarbot.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.Checks;
import net.vadamdev.dbk.framework.interactive.InteractiveComponents;
import net.vadamdev.dbk.framework.interactive.api.registry.MessageRegistry;
import net.vadamdev.dbk.framework.interactive.entities.buttons.InteractiveButton;
import net.vadamdev.dbk.framework.menu.InteractiveComponentMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author VadamDev
 * @since 02/04/2025
 */
public class ConfirmationRequest {
    protected final InteractiveComponentMenu menu;

    protected ConfirmationRequest(InteractiveComponentMenu menu) {
        this.menu = menu;
    }

    public void send(IReplyCallback callback) {
        menu.display(callback, false).queue();
    }

    /*
       Builder
     */

    public static Builder builder() {
        return new Builder().timeout(15, TimeUnit.MINUTES);
    }

    public static class Builder {
        private final InteractiveComponentMenu.Builder menuBuilder;

        private final List<Permission> requiredPermissions;
        private final List<String> authorizedUserIds;

        private Consumer<GenericComponentInteractionCreateEvent> onConfirm, onDeny;
        private Button confirmButton, denyButton;

        protected Builder() {
            this.menuBuilder = InteractiveComponentMenu.builder();

            this.requiredPermissions = new ArrayList<>();
            this.authorizedUserIds = new ArrayList<>();
        }

        public Builder requirePermissions(Permission permission, Permission... morePermissions) {
            requiredPermissions.add(permission);

            if(morePermissions.length > 0)
                requiredPermissions.addAll(List.of(morePermissions));

            return this;
        }

        public Builder onlyAuthorizedUsers(User user, User... moreUsers) {
            authorizedUserIds.add(user.getId());

            for(User anotherUser : moreUsers)
                authorizedUserIds.add(anotherUser.getId());

            return this;
        }

        public Builder whenConfirmed(Button confirmButton, Consumer<GenericComponentInteractionCreateEvent> action) {
            this.confirmButton = confirmButton;
            this.onConfirm = action;

            return this;
        }

        public Builder whenDenied(Button denyButton, Consumer<GenericComponentInteractionCreateEvent> action) {
            this.denyButton = denyButton;
            this.onDeny = action;

            return this;
        }

        public Builder addEmbed(MessageEmbed embed, MessageEmbed... moreEmbeds) {
            menuBuilder.addEmbed(embed, moreEmbeds);
            return this;
        }

        public Builder timeout(long timeout, TimeUnit unit) {
            menuBuilder.setTimeout(timeout, unit);
            return this;
        }

        protected Predicate<Member> createMemberPredicate() {
            if(requiredPermissions.isEmpty() && authorizedUserIds.isEmpty())
                return member -> true;

            if(!requiredPermissions.isEmpty() && !authorizedUserIds.isEmpty())
                return member -> authorizedUserIds.contains(member.getId()) && member.hasPermission(requiredPermissions);

            if(!requiredPermissions.isEmpty())
                return member -> member.hasPermission(requiredPermissions);

            return member -> authorizedUserIds.contains(member.getId());
        }

        public ConfirmationRequest build() {
            Checks.check(onConfirm != null && confirmButton != null, "The confirm button must be set!");

            final Predicate<Member> memberPredicate = createMemberPredicate();

            final MessageRegistry<Button> confirm = InteractiveButton.of(confirmButton, (event, invalidatable) -> {
                if(!memberPredicate.test(event.getMember()))
                    return;

                InteractiveComponents.findComponentManager(event.getJDA())
                        .ifPresent(manager -> manager.invalidateMessageAttachedComponents(event.getMessageIdLong()));

                onConfirm.accept(event);
            });

            MessageRegistry<Button> deny = null;
            if(denyButton != null && onDeny != null) {
                deny = InteractiveButton.of(denyButton, (event, invalidatable) -> {
                    if(!memberPredicate.test(event.getMember()))
                        return;

                    InteractiveComponents.findComponentManager(event.getJDA())
                            .ifPresent(manager -> manager.invalidateMessageAttachedComponents(event.getMessageIdLong()));

                    onDeny.accept(event);
                });
            }

            if(deny != null)
                menuBuilder.addActionRow(confirm, deny);
            else
                menuBuilder.addActionRow(confirm);

            return new ConfirmationRequest(menuBuilder.build());
        }
    }
}
