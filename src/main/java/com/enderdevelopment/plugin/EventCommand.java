package com.enderdevelopment.plugin;

import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EventCommand implements CommandExecutor, TabCompleter {
    private final EventManager eventManager;

    public EventCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("start")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only a player can choose the event location.");
                return true;
            }
            eventManager.startEvent(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("stop")) {
            eventManager.stopEvent(true);
            return true;
        }
        if (args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(eventManager.getStatus());
            return true;
        }
        if (args[0].equalsIgnoreCase("points")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only a player can view participation points.");
                return true;
            }
            sender.sendMessage(eventManager.getPoints(player));
            return true;
        }
        sender.sendMessage("Usage: /event <start|stop|status|points>");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "status", "points");
        }
        return List.of();
    }
}
