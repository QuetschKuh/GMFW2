package de.lolsu.gmfw.phases.routines;

import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.api.WorldBorders;
import de.lolsu.gmfw.config.ConfVar;
import de.lolsu.gmfw.managers.GamestateManager;
import de.lolsu.gmfw.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class WarmupRoutine extends BukkitRunnable {

    private final GamestateManager manager;
    private final int lengthWatch;
    private final int lengthPVP;

    private int counter;

    public WarmupRoutine(GamestateManager manager, int lengthWatch, int lengthPVP) {
        this.manager = manager;
        this.lengthWatch = lengthWatch;
        this.lengthPVP = lengthPVP;

        this.counter = -1;
        PlayerManager.forAll(player -> {
            WorldBorders.sendWorldBorder(player, player.getLocation().getX(), player.getLocation().getZ(), ConfVar.WB_WATCH_DIAMETER.getInt(), 0);
        });
    }

    @Override
    public void run() {
        counter++;

        // No idea how to do it better :(
        if(counter == lengthWatch || counter == lengthPVP){
            if(counter == lengthWatch) manager.onWatchEnd();
            if(counter == lengthPVP) {
                manager.onWarmupEnd();
                cancel();
            }
            return;
        }

        boolean sound = false;

        // Watch countdown
        if(counter < lengthWatch && counter >= lengthWatch - 5) {
            sound = true;
            Messenger.sendActionBars(Bukkit.getOnlinePlayers(), ChatColor.GREEN + "Game starting in " + ChatColor.DARK_GREEN + (lengthWatch - counter));
        }

        // Warmup countdown
        if(counter < lengthPVP && counter >= lengthPVP - 5) {
            sound = true;
            Messenger.sendActionBars(Bukkit.getOnlinePlayers(), ChatColor.RED + "PVP enabling in " + ChatColor.DARK_RED + (lengthPVP - counter));
        }

        if(sound) Messenger.broadcastSound(Sound.SUCCESSFUL_HIT);
    }

}
