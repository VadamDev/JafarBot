package net.vadamdev.jafarbot.rolereaction;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author VadamDev
 * @since 02/03/2023
 */
public final class RoleReactionManager {
    private final Map<String, RoleReaction> roleReactions;

    public RoleReactionManager() {
        this.roleReactions = new HashMap<>();
    }

    public void handleButtonEvent(@Nonnull ButtonInteractionEvent event) {
        final String buttonId = event.getComponentId();
        if(!buttonId.startsWith("roleReaction"))
            return;

        final String[] split = buttonId.split("-");

        roleReactions.values().stream().filter(roleReaction -> roleReaction.getName().equals(split[1])).findFirst()
                .flatMap(roleReaction -> roleReaction.findRoleOptionByRoleId(split[2]))
                .ifPresent(roleOption -> roleOption.computeRole(event.getMember(), event));
    }

    public void addRoleReaction(RoleReaction roleReaction) {
        roleReactions.put(roleReaction.getName(), roleReaction);
    }

    public Optional<RoleReaction> findRoleReactionByName(String name) {
        return Optional.ofNullable(roleReactions.get(name));
    }

    public Set<String> getRoleReactions() {
        return roleReactions.keySet();
    }
}
