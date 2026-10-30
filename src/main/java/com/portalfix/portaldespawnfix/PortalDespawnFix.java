package com.portalfix.portaldespawnfix;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PortalDespawnFix extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("PortalDespawnFix aktiviert!");
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Monster) {
            Monster monster = (Monster) entity;
            monster.setRemoveWhenFarAway(false);

            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (monster.isValid() && !monster.isDead()) {
                    monster.setRemoveWhenFarAway(true);
                }
            }, 600L);
        }
    }
}