package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private EventManager eventManager;

    @Override
    public void onEnable() {
        eventManager = new EventManager(this);
        EventCommand command = new EventCommand(eventManager);
        if (getCommand("event") != null) {
            getCommand("event").setExecutor(command);
            getCommand("event").setTabCompleter(command);
        }
        Bukkit.getPluginManager().registerEvents(new EventListener(eventManager), this);
        getLogger().info("GrandEvent enabled with eight cinematic phases.");
    }

    @Override
    public void onDisable() {
        if (eventManager != null) {
            eventManager.stopEvent(false);
        }
    }
}
