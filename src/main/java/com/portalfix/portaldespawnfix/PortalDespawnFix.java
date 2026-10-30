package com.portalfix.portaldespawnfix;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PortalDespawnFix extends JavaPlugin implements Listener {

    // Read-only configuration (compile-time constants, no config file): change here and rebuild.
    private static final Set<EntityType> PROTECTED_TYPES = EnumSet.of(EntityType.ZOMBIFIED_PIGLIN);
    private static final long PROTECT_TICKS = 600L;

    // The entity instance is replaced on a dimension change, so mobs are tracked by UUID.
    // Only mobs that were despawnable before the portal are tracked, so existing persistence is never touched.
    private final Set<UUID> protectedMobs = new HashSet<>();
    // Protection window is over, but the mob was not loaded; restored when its chunk loads.
    private final Set<UUID> overdueMobs = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("PortalDespawnFix aktiviert!");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        Entity entity = event.getEntity();

        if (!PROTECTED_TYPES.contains(entity.getType()) || !(entity instanceof Mob)) {
            return;
        }

        Mob mob = (Mob) entity;
        UUID id = mob.getUniqueId();

        if (!mob.getRemoveWhenFarAway() || !protectedMobs.add(id)) {
            return;
        }

        mob.setRemoveWhenFarAway(false);
        Bukkit.getScheduler().runTaskLater(this, () -> restore(id), PROTECT_TICKS);
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if (overdueMobs.isEmpty()) {
            return;
        }

        for (Entity entity : event.getEntities()) {
            if (overdueMobs.remove(entity.getUniqueId()) && entity instanceof Mob) {
                ((Mob) entity).setRemoveWhenFarAway(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        protectedMobs.remove(id);
        overdueMobs.remove(id);
    }

    private void restore(UUID id) {
        if (!protectedMobs.remove(id)) {
            return;
        }

        Entity entity = Bukkit.getEntity(id);
        if (entity instanceof Mob) {
            ((Mob) entity).setRemoveWhenFarAway(true);
        } else {
            overdueMobs.add(id);
        }
    }
}
