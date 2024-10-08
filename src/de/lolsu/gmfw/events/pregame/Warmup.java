package de.lolsu.gmfw.events.pregame;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Warmup implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // If player moves non-vertically
        if(from.getX() != to.getX() || from.getZ() != to.getZ()) {

            // Teleport back
            player.teleport(from.setDirection(to.getDirection()));

            // Quite violent indicator that movement is totally not cool rn
            player.playSound(player.getLocation(), Sound.CAT_HISS, 0.25f, 1);
            for (int i = 0; i < 4; i++) {
                for (int y = 0; y < 5; y++) {
                    player.spigot().playEffect(
                            player.getLocation().add(i == 0 ? -1 : i == 3 ? 1 : 0, y * 0.4, i == 1 ? 1 : i == 2 ? -1 : 0),
                            Effect.COLOURED_DUST, 0, 0,
                            1, 0, 0,
                            1, 0, 2
                    );
                }
            }

        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

}
