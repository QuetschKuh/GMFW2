package de.lolsu.gmfw.phases.routines;

import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.api.WorldBorders;
import de.lolsu.gmfw.phases.MapHandler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class ShrinkWorldBorder extends BukkitRunnable {

    private final double diameter;
    private final long time;
    private final int warnDistance;
    private final int warnTime;

    public ShrinkWorldBorder(double diameter, long time, int warnDistance, int warnTime) {
        this.diameter = diameter;
        this.time = time;
        this.warnDistance = warnDistance;
        this.warnTime = warnTime;
    }

    @Override
    public void run() {
        Location location = MapHandler.getConfig().getLocation("Spawns.Spec", MapHandler.getWorld());
        WorldBorders.setWorldBorder(location.getX(), location.getZ(), diameter, time, warnDistance, warnTime);
        Messenger.broadcast(ChatColor.RED + "Worldborder is starting to shrink!");
    }

}
