package net.vadamdev.jdautils.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.vadamdev.jdautils.commands.data.ICommandData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a JDAUtils command
 *
 * @author VadamDev
 * @since 17/10/2022
 */
public abstract class Command {
    protected final String name;

    private String[] aliases;
    private Permission permission;

    public Command(String name) {
        this.name = name;

        this.aliases = new String[0];
        this.permission = null;
    }

    public abstract void execute(@Nonnull Member sender, @Nonnull ICommandData commandData);

    public void setAliases(String... aliases) {
        if(aliases != null)
            this.aliases = aliases;
        else
            this.aliases = new String[0];
    }

    public void setPermission(@Nullable Permission permission) {
        this.permission = permission;
    }

    @Nullable
    public Permission getPermission() {
        return permission;
    }

    boolean check(String str) {
        for(String alias : aliases) {
            if(alias.equalsIgnoreCase(str))
                return true;
        }

        return name.equalsIgnoreCase(str);
    }
}
