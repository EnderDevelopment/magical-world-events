package com.enderdevelopment.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class EventManager {
    private static final int TOTAL_TICKS = 20 * 190;
    private static final double PARTICIPATION_RADIUS_SQUARED = 18 * 18;
    private final JavaPlugin plugin;
    private final Map<World, WorldState> savedWorlds = new HashMap<>();
    private final Map<UUID, Integer> participationPoints = new HashMap<>();
    private final Set<Integer> awardedPhases = new HashSet<>();
    private BukkitTask task;
    private Location origin;
    private int tick;
    private int phase = 1;

    public EventManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startEvent(Player starter) {
        if (task != null) {
            starter.sendMessage("§cThere is already an event running.");
            return;
        }
        origin = starter.getLocation().clone().add(0, 0.1, 0);
        World world = origin.getWorld();
        if (world == null) {
            starter.sendMessage("§cThe event needs a valid world.");
            return;
        }
        savedWorlds.put(world, new WorldState(world));
        participationPoints.clear();
        awardedPhases.clear();
        world.setTime(18000L);
        world.setStorm(false);
        world.setThundering(false);
        tick = 0;
        phase = 1;
        announce("§5§l✦ GRAND EVENT ✦", "§dA cinematic event has started at §f" + formatLocation(origin));
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::runTick, 0L, 1L);
    }

    public void stopEvent(boolean announce) {
        if (task == null) {
            return;
        }
        task.cancel();
        task = null;
        restoreWorlds();
        if (announce) {
            announce("§c§lEVENT STOPPED", "§7The cinematic event was stopped without rewards.");
        }
        participationPoints.clear();
        awardedPhases.clear();
        origin = null;
    }

    public void removeViewer(Player player) {
        if (origin != null && player.getWorld().equals(origin.getWorld())) {
            player.stopSound(Sound.MUSIC_DISC_CREATOR);
        }
    }

    public String getStatus() {
        if (task == null) {
            return "§7No event is running. Use §f/event start§7.";
        }
        return "§dGrand Event §7phase §f" + phase + "§7, remaining §f" + ((TOTAL_TICKS - tick) / 20) + "s§7.";
    }

    public String getPoints(Player player) {
        int points = participationPoints.getOrDefault(player.getUniqueId(), 0);
        return "§6§lParticipation §8» §e" + points + " points§7. Stay within 18 blocks during the event to earn more.";
    }

    private void runTick() {
        if (origin == null || tick >= TOTAL_TICKS) {
            finishEvent();
            return;
        }
        phase = Math.min(8, (tick / 480) + 1);
        awardParticipation();
        if (tick % 20 == 0) {
            phaseTitle();
        }
        switch (phase) {
            case 1 -> awakening();
            case 2 -> spiral();
            case 3 -> fallingTreasures();
            case 4 -> heartGarden();
            case 5 -> lightningCrown();
            case 6 -> cosmicRings();
            case 7 -> fireworkRush();
            case 8 -> finale();
            default -> { }
        }
        tick++;
    }

    private void awardParticipation() {
        if (tick % 20 != 0 || origin == null) {
            return;
        }
        for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
            if (isNearEvent(player)) {
                participationPoints.merge(player.getUniqueId(), 2, Integer::sum);
                player.sendActionBar("§6Event points: §e" + participationPoints.get(player.getUniqueId()));
            }
        }
        if (awardedPhases.add(phase)) {
            for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
                if (isNearEvent(player)) {
                    participationPoints.merge(player.getUniqueId(), 10, Integer::sum);
                    player.sendMessage("§d✦ §fPhase bonus: §e+10 participation points§d!");
                }
            }
        }
    }

    private boolean isNearEvent(Player player) {
        return player.getWorld().equals(origin.getWorld()) && player.getLocation().distanceSquared(origin) <= PARTICIPATION_RADIUS_SQUARED;
    }

    private void finishEvent() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        rewardParticipants();
        restoreWorlds();
        announce("§a§lEVENT COMPLETE", "§fRewards have been given to the participants!");
        participationPoints.clear();
        awardedPhases.clear();
        origin = null;
    }

    private void rewardParticipants() {
        for (Map.Entry<UUID, Integer> entry : new HashMap<>(participationPoints).entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                continue;
            }
            int points = entry.getValue();
            int emeralds = Math.max(1, points / 10);
            int gold = Math.max(1, points / 25);
            player.getInventory().addItem(new ItemStack(Material.EMERALD, Math.min(64, emeralds)));
            player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, Math.min(64, gold)));
            if (points >= 100) {
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
            }
            player.sendMessage("§a§lReward received! §fYou earned §e" + points + " participation points§f, §a" + emeralds
                    + " emeralds§f and §6" + gold + " gold ingot(s)§f.");
            if (points >= 100) {
                player.sendMessage("§bAmazing participation! §fYou also received a §bdiamond§f.");
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        }
    }

    private void restoreWorlds() {
        for (Map.Entry<World, WorldState> entry : savedWorlds.entrySet()) {
            entry.getValue().restore(entry.getKey());
        }
        savedWorlds.clear();
    }

    private void phaseTitle() {
        String[] titles = {"§8§lTHE AWAKENING", "§b§lSKY SPIRAL", "§6§lTREASURE RAIN", "§c§lHEART GARDEN",
                "§e§lTHUNDER CROWN", "§d§lCOSMIC GATES", "§5§lSTAR RUSH", "§f§lTHE FINALE"};
        announce(titles[phase - 1], "§7Phase " + phase + " of 8");
        origin.getWorld().playSound(origin, Sound.BLOCK_BEACON_ACTIVATE, 1.2f, 0.7f + phase * 0.08f);
    }

    private void awakening() {
        double radius = 1.5 + tick / 180.0;
        ring(0.25, radius, Particle.END_ROD, 18);
        ring(0.1, radius * 0.65, Particle.ENCHANT, 14);
        particles(origin.clone().add(0, tick / 100.0, 0), Particle.GLOW, 4, 0.3, 0.2, 0.3, 0);
    }

    private void spiral() {
        double angle = tick * 0.16;
        for (int arm = 0; arm < 3; arm++) {
            double current = angle + arm * Math.PI * 2 / 3;
            double radius = 0.5 + (tick % 480) / 80.0;
            Location point = origin.clone().add(Math.cos(current) * radius, 0.4 + radius * 0.35, Math.sin(current) * radius);
            particles(point, Particle.WITCH, 3, 0.08, 0.08, 0.08, 0);
            particles(point, Particle.END_ROD, 2, 0.04, 0.04, 0.04, 0);
        }
        if (tick % 15 == 0) origin.getWorld().playSound(origin, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 0.8f + (tick % 60) / 100f);
    }

    private void fallingTreasures() {
        for (int i = 0; i < 3; i++) {
            double angle = (tick * 0.11) + i * 2.1;
            Location point = origin.clone().add(Math.cos(angle) * 4.2, 7.5 - (tick % 100) * 0.07, Math.sin(angle) * 4.2);
            Material material = i == 0 ? Material.APPLE : i == 1 ? Material.BOOK : Material.GOLD_INGOT;
            origin.getWorld().spawnParticle(Particle.ITEM, point, 1, 0, 0, 0, 0, new ItemStack(material));
            particles(point, Particle.FIREWORK, 2, 0.1, 0.1, 0.1, 0);
        }
        ring(7.5, 4.2, Particle.HAPPY_VILLAGER, 30);
        if (tick % 30 == 0) origin.getWorld().playSound(origin, Sound.ENTITY_ITEM_PICKUP, 1.1f, 0.7f);
    }

    private void heartGarden() {
        double angle = tick * 0.09;
        for (int i = 0; i < 5; i++) {
            double a = angle + i * Math.PI * 2 / 5;
            Location point = origin.clone().add(Math.cos(a) * 3.2, 1.4 + Math.sin(angle * 1.5 + i) * 0.5, Math.sin(a) * 3.2);
            particles(point, Particle.HEART, 2, 0.1, 0.1, 0.1, 0);
            particles(point.clone().add(0, 0.5, 0), Particle.CHERRY_LEAVES, 4, 0.2, 0.2, 0.2, 0);
        }
        if (tick % 25 == 0) origin.getWorld().playSound(origin, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1f, 1.2f);
    }

    private void lightningCrown() {
        double angle = tick * 0.2;
        Location point = origin.clone().add(Math.cos(angle) * 5, 0, Math.sin(angle) * 5);
        particles(point.clone().add(0, 4, 0), Particle.ELECTRIC_SPARK, 35, 0.3, 2.5, 0.3, 0.05);
        if (tick % 35 == 0) {
            origin.getWorld().strikeLightningEffect(point);
            origin.getWorld().playSound(origin, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.4f);
        }
        ring(0.25, 5, Particle.ELECTRIC_SPARK, 36);
    }

    private void cosmicRings() {
        double radius = 2.5 + Math.sin(tick * 0.08) * 1.5;
        ring(0.8 + Math.sin(tick * 0.05) * 0.7, radius, Particle.PORTAL, 48);
        ring(2.2, radius * 0.7, Particle.DRAGON_BREATH, 32);
        particles(origin.clone().add(0, 2.5, 0), Particle.REVERSE_PORTAL, 12, 0.7, 1, 0.7, 0.02);
    }

    private void fireworkRush() {
        for (int i = 0; i < 4; i++) {
            double angle = tick * 0.13 + i * Math.PI / 2;
            Location point = origin.clone().add(Math.cos(angle) * 5.5, 1 + (tick % 80) * 0.06, Math.sin(angle) * 5.5);
            particles(point, Particle.FIREWORK, 10, 0.3, 0.3, 0.3, 0.02);
            particles(point, Particle.DUST, 3, 0.15, 0.15, 0.15, 0, new Particle.DustOptions(Color.fromRGB(255, 60 + i * 45, 220), 1.3f));
        }
        if (tick % 28 == 0) origin.getWorld().playSound(origin, Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 1.3f, 1f);
    }

    private void finale() {
        int remaining = TOTAL_TICKS - tick;
        double radius = Math.max(0.5, remaining / 50.0);
        ring(1.5, radius, Particle.END_ROD, 70);
        ring(3.2, radius * 0.7, Particle.HEART, 30);
        particles(origin.clone().add(0, 3, 0), Particle.TOTEM_OF_UNDYING, 12, 1, 1.5, 1, 0.05);
        if (tick % 20 == 0) origin.getWorld().playSound(origin, Sound.BLOCK_BEACON_POWER_SELECT, 1.3f, 0.7f + (190 - remaining / 20) / 200f);
        if (remaining <= 5) {
            for (int i = 0; i < 5; i++) particles(origin.clone().add(0, 1, 0), Particle.FIREWORK, 40, 2, 2, 2, 0.1);
        }
    }

    private void ring(double y, double radius, Particle particle, int count) {
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count + tick * 0.02;
            Location point = origin.clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            particles(point, particle, 1, 0, 0, 0, 0);
        }
    }

    private void particles(Location location, Particle particle, int count, double x, double y, double z, double speed) {
        location.getWorld().spawnParticle(particle, location, count, x, y, z, speed);
    }

    private void particles(Location location, Particle particle, int count, double x, double y, double z, double speed, Object data) {
        location.getWorld().spawnParticle(particle, location, count, x, y, z, speed, data);
    }

    private void announce(String title, String subtitle) {
        for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
            if (origin != null && player.getWorld().equals(origin.getWorld())) {
                player.sendTitle(title, subtitle, 8, 35, 12);
            }
        }
    }

    private String formatLocation(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    private record WorldState(long time, boolean storm, boolean thunder, int weatherDuration, int thunderDuration) {
        WorldState(World world) {
            this(world.getTime(), world.hasStorm(), world.isThundering(), world.getWeatherDuration(), world.getThunderDuration());
        }

        void restore(World world) {
            world.setTime(time);
            world.setStorm(storm);
            world.setThundering(thunder);
            world.setWeatherDuration(weatherDuration);
            world.setThunderDuration(thunderDuration);
        }
    }
}
