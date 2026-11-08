package com.enderdevelopment.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class EventListener implements Listener {
    private final EventManager eventManager;

    public EventListener(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        eventManager.removeViewer(event.getPlayer());
    }
}
