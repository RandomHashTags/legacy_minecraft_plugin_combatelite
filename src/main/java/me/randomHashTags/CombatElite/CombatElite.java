package me.randomHashTags.CombatElite;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class CombatElite extends JavaPlugin implements Listener {

    public static Plugin getPlugin;
    private ArrayList<EntityType> blacklisted = new ArrayList<>();
    private boolean players = false, mobs = false;
    public void onEnable() {
        getPlugin = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        mobs = getConfig().getBoolean("enable-tick-delay-mobs");
        players = getConfig().getBoolean("enable-tick-delay-players");
        for(String s : getConfig().getStringList("blacklisted-entities"))
            blacklisted.add(EntityType.valueOf(s.toUpperCase()));
    }

    @EventHandler
    private void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        final Player damager = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;
        if(damager != null) {
            final Entity e = event.getEntity();
            if(!blacklisted.contains(e.getType()) && e instanceof LivingEntity) {
                final boolean isPlayer = e instanceof Player;
                if(isPlayer && players || !isPlayer && mobs) {
                    int ticks = getConfig().getInt("tick-delay-" + (isPlayer ? "players" :  "entities"));
                    if(ticks < 0) ticks = 0;
                    final LivingEntity le = (LivingEntity) e;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                        public void run() {
                            le.setNoDamageTicks(0);
                        }
                    }, ticks);
                }
            }
        }
    }
}