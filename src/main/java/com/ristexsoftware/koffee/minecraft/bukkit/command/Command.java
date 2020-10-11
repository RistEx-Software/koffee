/* 
 *  Koffee - A simple collection of utilities I commonly use
 *  Copyright (C) 2019-2020 Justin Crawford <justin@Stacksmash.net>
 *  Copyright (C) 2019-2020 Zachery Coleman <Zachery@Stacksmash.net>
 *  Copyright (C) 2019-2020 Skye Elliot <actuallyori@gmail.com>
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ristexsoftware.koffee.minecraft.bukkit.command;

import java.util.List;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.TabCompleter;

/**
 * This class is designed to handle execution of commands given by a user or the
 * console, it runs the command code synchronously with the server thread and
 * can cause tick lag if the command takes too long.
 */
public abstract class Command extends org.bukkit.command.Command implements PluginIdentifiableCommand {
    private Plugin owner;
    private TabCompleter completer;

    /**
     * Create a new command for the associated plugin
     * 
     * @param commandName The name of the command the user will execute
     * @param owner       The plugin that owns this command.
     */
    public Command(String commandName, Plugin owner) {
        super(commandName);
        this.owner = owner;
    }

    /**
     * Show a syntax error message when the user fails to enter thr proper syntax
     * 
     * @param sender Who failed the command
     * @param label  The command itself
     * @param args   Arguments provided to the command
     */
    public abstract void onSyntaxError(CommandSender sender, String label, String[] args);

    /**
     * Show a permission denied error when the user does not have permission to
     * execute the command
     * 
     * @param sender Who failed the command
     * @param label  The command itself
     * @param args   Arguments provided to the command
     */
    public abstract void onPermissionDenied(CommandSender sender, String label, String[] args);

    /**
     * Show a error when the command fails to execute
     * 
     * @param sender Who failed the command
     * @param label  The command itself
     * @param args   Arguments provided to the command
     */
    public abstract void onError(CommandSender sender, String label, String[] args);

    /**
     * Execute the command itself (part of the derived class)
     * 
     * @param sender       Who is executing the command
     * @param commandLabel The command string triggering this command
     * @param args         The arguments provided to this command
     * @return Whether or not the command succeeded, returning false will trigger
     *         onSyntaxError()
     */
    public abstract ExitCode executeCommand(CommandSender sender, String commandLabel, String[] args);

    /**
     * This is a vastly simplified command class. We only check if the plugin is
     * enabled before we execute whereas spigot's `PluginCommand` will attempt to
     * check permissions beforehand.
     * 
     * This also allows us to do async commands if we so desire and it nulls the
     * point of CommandExecutors because they were fucking pointless to begin with.
     * 
     * @param sender       The person executing the command
     * @param commandLabel The command that was executed
     * @param args         The arguments given to the command.
     * @return True if the command succeeded, otherwise it will execute
     *         onSyntaxError().
     */
    @Override
    public final boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.owner.isEnabled())
            throw new CommandException(String.format("Cannot execute command \"%s\" in plugin %s - plugin is disabled.",
                    commandLabel, this.owner.getDescription().getFullName()));

        try {
            switch (this.executeCommand(sender, commandLabel, args)) {
                case EXIT_SUCCESS:
                    break;
                case EXIT_INVALID_SYNTAX:
                    onSyntaxError(sender, commandLabel, args);
                    break;
                case EXIT_PERMISSION_DENIED:
                    onPermissionDenied(sender, commandLabel, args);
                    break;
                case EXIT_ERROR:
                    onError(sender, commandLabel, args);
                    break;
                default:
                    throw new IllegalArgumentException("The exit code "
                            + executeCommand(sender, commandLabel, args) + " is out of range");
            }
        } catch (Exception ex) {
            throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin "
                    + this.owner.getDescription().getFullName(), ex);
        }
        return true;
    }

    /**
     * Gets the owner of this PluginCommand
     *
     * @return Plugin that owns this command
     */
    @Override
    public Plugin getPlugin() {
        return this.owner;
    }

    /**
     * Sets the {@link CommandExecutor} to run when parsing this command NOTE: This
     * function does nothing.
     * 
     * @param executor New executor to run
     */
    public void setExecutor(CommandExecutor executor) {
        // no-op
    }

    /**
     * Gets the {@link CommandExecutor} associated with this command NOTE: These
     * commands will never have CommandExecutors and will *ALWAYS* return the plugin
     * itself.
     * 
     * @return Plugin.
     */
    public CommandExecutor getExecutor() {
        return this.owner;
    }

    /**
     * Sets the {@link TabCompleter} to run when tab-completing this command.
     * <p>
     * If no TabCompleter is specified, and the command's executor implements
     * TabCompleter, then the executor will be used for tab completion.
     *
     * @param completer New tab completer
     */
    public void setTabCompleter(TabCompleter completer) {
        this.completer = completer;
    }

    /**
     * Gets the {@link TabCompleter} associated with this command.
     *
     * @return TabCompleter object linked to this command
     */
    public TabCompleter getTabCompleter() {
        return this.completer;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the tab completer if present.
     * <p>
     * If it is not present or returns null, will delegate to the current command
     * executor if it implements {@link TabCompleter}. If a non-null list has not
     * been found, will default to standard player name completion in
     * {@link Command#tabComplete(CommandSender, String, String[])}.
     * <p>
     * This method does not consider permissions.
     *
     * @throws CommandException         if the completer or executor throw an
     *                                  exception during the process of
     *                                  tab-completing.
     * @throws IllegalArgumentException if sender, alias, or args is null
     */
    @Override
    public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args)
            throws CommandException, IllegalArgumentException {
        if (sender == null || alias == null || args == null)
            throw new NullPointerException("arguments to tabComplete cannot be null");

        List<String> completions = null;
        try {
            if (completer != null)
                completions = completer.onTabComplete(sender, this, alias, args);
        } catch (Throwable ex) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
            for (String arg : args)
                message.append(arg).append(' ');

            message.deleteCharAt(message.length() - 1).append("' in plugin ")
                    .append(owner.getDescription().getFullName());
            throw new CommandException(message.toString(), ex);
        }

        if (completions == null)
            return super.tabComplete(sender, alias, args);

        return completions;
    }

    /**
     * Convert this command name to a string
     * 
     * @return the human readable name of the class
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(", ").append(owner.getDescription().getFullName()).append(')');
        return stringBuilder.toString();
    }
}
