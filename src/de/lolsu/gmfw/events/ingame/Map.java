package de.lolsu.gmfw.events.ingame;

import de.lolsu.gmfw.config.ConfVar;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.statistics.StatManager;
import de.lolsu.gmfw.statistics.Stats;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;

/**
 * Handles the loaded map, {@link ConfVar ConfVars} determine what blocks can be placed and broken.<br>
 * Placed blocks are automatically assigned a metadata value, so they can be broken again, even if {@link ConfVar#MAP_BREAKABLE} is turned off.
 * */
public class Map implements Listener {

    private final boolean placeable;
    private final boolean breakable;
    private final ArrayList<Material> placeWhitelist;
    private final ArrayList<Material> breakWhitelist;

    public Map() {
        placeable = ConfVar.MAP_PLACEABLE.getState();
        breakable = ConfVar.MAP_BREAKABLE.getState();
        placeWhitelist = (ArrayList<Material>) ConfVar.MAP_PLACE_WHITELIST.getObject();
        breakWhitelist = (ArrayList<Material>) ConfVar.MAP_BREAK_WHITELIST.getObject();
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        if(placeable || placeWhitelist.contains(event.getBlockPlaced().getType())) {
            StatManager.increase(event.getPlayer(), Stats.BLOCKS_PLACED);
            if(breakable || breakWhitelist.contains(event.getBlock().getType())) return;
            event.getBlockPlaced().setMetadata("breakable", new FixedMetadataValue(Gamemode.getPlugin(), true));
            return;
        }

        event.setCancelled(true);
        sendDisallowedParticles(event.getBlock().getLocation().add(0.5, 0.5, 0.5), event.getPlayer());
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        if(breakable || breakWhitelist.contains(event.getBlock().getType()) ||
            (event.getBlock().hasMetadata("breakable") && event.getBlock().getMetadata("breakable").get(0).asBoolean())) {
            StatManager.increase(event.getPlayer(), Stats.BLOCKS_BROKEN);
            return;
        }

        event.setCancelled(true);
        sendDisallowedParticles(event.getBlock().getLocation().add(0.5, 0.5, 0.5), event.getPlayer());
    }

    public void sendDisallowedParticles(Location location, Player player) {
        for(int i = 0; i < 6; i++) {
            // "offsetX/Y/Z" are rgb values
            player.spigot().playEffect(
                    location.clone().add(i == 0 ? 0.6 : i == 1 ? -0.6 : 0, i == 2 ? 0.6 : i == 3 ? -0.6 : 0, i == 4 ? 0.6 : i == 5 ? -0.6 : 0),
                    Effect.COLOURED_DUST,
                    0, 0,
                    1, 0, 0,
                    1, 0, 10
            );
        }
        player.playSound(player.getLocation(), Sound.CAT_HISS, 0.5f, 0.5f);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
        event.getWorld().setWeatherDuration(72000);
    }

}
