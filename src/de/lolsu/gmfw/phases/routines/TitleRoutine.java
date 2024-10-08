package de.lolsu.gmfw.phases.routines;

import de.lolsu.gmfw.api.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Displays the given title, first decreasing the distance between characters, then going through the letters, painting them gold.<br>
 * Should be run asynchronously at a period of 2 ticks for the best experience.
 * */
public class TitleRoutine extends BukkitRunnable {

    final String title;
    int countdown = 4;

    public TitleRoutine(String title) {
        this.title = title;
    }

    @Override
    public void run() {
        String text;
        // First five frames, shrink distance between letters
        if(countdown >= 0) {
            String spaces = new String(new char[countdown * 2]).replace("\0", " ");
            text = title.replace("", spaces);
        }

        // Iterate through text
        else if(countdown >= title.length() * -1) {
            StringBuilder builder = new StringBuilder(title);
            builder.insert(Math.abs(countdown), ChatColor.RED);
            builder.insert(Math.abs(countdown) - 1, ChatColor.GOLD);
            text = builder.toString();
        }

        // Done
        else {
            cancel();
            return;
        }

        Messenger.sendTitleBars(Bukkit.getOnlinePlayers(), ChatColor.DARK_RED + text, ChatColor.BLUE + "May the best player win!", 0, 20, 5);
        countdown--;
    }

}
