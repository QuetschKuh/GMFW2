package de.lolsu.gmfw.events.ingame;

import de.lolsu.gmfw.managers.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Spectator implements Listener {

    public PlayerManager pm;

    public Spectator(PlayerManager pm) {
        this.pm = pm;
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if(pm.isSpectator(e.getPlayer()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(pm.isSpectator(e.getPlayer()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventory(InventoryInteractEvent e) {
        if(e.getWhoClicked() instanceof Player && pm.isSpectator((Player) e.getWhoClicked()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onTakeDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player && pm.isSpectator((Player) e.getEntity()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onDealDamage(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player && pm.isSpectator((Player) e.getDamager()))
            e.setCancelled(true);
    }

}
