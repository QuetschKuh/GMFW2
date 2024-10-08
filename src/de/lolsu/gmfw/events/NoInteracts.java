package de.lolsu.gmfw.events;

import de.lolsu.gmfw.events.pregame.Lobby;
import de.lolsu.gmfw.main.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class NoInteracts implements Listener {

    /**
     * Disables PlayerInteractEvents (except for books)
     * */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getItem() != null && e.getItem().getType() == Material.WRITTEN_BOOK) return;
        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    /**
     * Disables PlayerDropItemEvents
     * */
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if(e.getPlayer().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    /**
     * Disables InventoryInteractEvents
     * */
    @EventHandler
    public void onInvInteract(InventoryInteractEvent e) {
        if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    /**
     * Disables InventoryClickEvents
     * */
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE) e.setCancelled(true);
    }

    /**
     * Disables EntityDamageEvents and respawns the player at the lobby spawn if they're in that world
     * */
    @EventHandler
    public void onTakeDamage(EntityDamageEvent e) {
        if(e.getCause() == EntityDamageEvent.DamageCause.VOID && e.getEntity().getWorld() == Gamemode.getInstance().lobbyWorld)
                e.getEntity().teleport(Gamemode.getInstance().spawnLocation);
        if(!(e instanceof EntityDamageByEntityEvent)) e.setCancelled(true);
    }

    /**
     * Disables EntityDamageByEntityEvents but allows knockback by sneaking players
     * */
    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player && ((Player) e.getDamager()).isSneaking()) {
            e.setDamage(0); e.getEntity().setVelocity(e.getDamager().getLocation().getDirection().multiply(3).setY(80));
        } else e.setCancelled(true);
    }

    @EventHandler
    public void onSaturation(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onPlaceEntity(VehicleCreateEvent e) {
        e.getVehicle().remove();
    }

}
