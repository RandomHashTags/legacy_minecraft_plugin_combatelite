package me.randomHashTags.CombatElite;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public final class CombatElite extends JavaPlugin implements Listener {
    public static Plugin getPlugin;
    private List<EntityType> blacklisted;
    private boolean players = false, mobs = false;
    private BukkitScheduler scheduler;
    private int playerTicks, entityTicks;
    public void onEnable() {
        getPlugin = this;
        enable();
    }
    public void onDisable() {
        disable();
    }

    public void enable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        scheduler = Bukkit.getScheduler();
        final FileConfiguration config = getConfig();
        mobs = config.getBoolean("enable-tick-delay-mobs");
        players = config.getBoolean("enable-tick-delay-players");
        playerTicks = config.getInt("tick-delay-players");
        if(playerTicks < 0) playerTicks = 0;
        entityTicks = config.getInt("tick-delay-entities");
        if(entityTicks < 0) entityTicks = 0;
        blacklisted = new ArrayList<>();
        for(String s : config.getStringList("blacklisted-entities")) {
            blacklisted.add(EntityType.valueOf(s.toUpperCase()));
        }
    }
    public void disable() {
        blacklisted.clear();
        HandlerList.unregisterAll((Plugin) this);
    }
    public void reload() {
        disable();
        enable();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        final Entity d = event.getDamager();
        final Player damager = d instanceof Player ? (Player) d : null;
        if(damager != null) {
            final Entity e = event.getEntity();
            if(!blacklisted.contains(e.getType()) && e instanceof LivingEntity) {
                final boolean isPlayer = e instanceof Player;
                if(isPlayer && players || !isPlayer && mobs) {
                    final LivingEntity le = (LivingEntity) e;
                    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                        public void run() {
                            le.setNoDamageTicks(0);
                        }
                    }, isPlayer ? playerTicks : entityTicks);
                }
            }
        }
    }
}