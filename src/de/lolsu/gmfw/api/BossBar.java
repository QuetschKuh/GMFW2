package de.lolsu.gmfw.api;

import de.lolsu.gmfw.main.Gamemode;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A handy interface for handling boss-bars in 1.8<br>
 * Creates a wither with the given name and health (percent) and makes it invisible
 * */
public class BossBar extends BukkitRunnable {

    private String title;
    private float progress;

    private final HashMap<Player, EntityWither> witherMap = new HashMap<>();

    /**
     * Creates a new boss bar
     * @param title The title of the boss bar
     * @param progress The progress of the boss bar (from 0.0f to 1.0f)
     * @param players The players that should be added to it
     * */
    public BossBar(String title, float progress, Player... players) {
        this.title = title;
        this.progress = progress;
        runTaskTimer(Gamemode.getPlugin(), 4L, 4L);   // Not sure why it needs to be synchronous but seems better this way
        for(Player p : players) addPlayer(p);
    }

    public void addPlayer(Player p) {
        // Assemble wither and spawn packet and send
        EntityWither wither = new EntityWither(((CraftWorld) p.getWorld()).getHandle());
        Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(50));
        wither.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);

        // Create and send the packet
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(wither);

        try {
            Field field = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("l");
            field.setAccessible(true);
            field.set(packet, getWatcher(wither));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return;
        }

        witherMap.put(p, wither);
        Packets.send(p, packet);
    }

    public void addPlayers(Player... players) {
        for(Player p : players) addPlayer(p);
    }

    public void removePlayer(Player p) {
        if(!p.isOnline()) { witherMap.remove(p); return; }  // Make sure player hasn't disconnected

        EntityWither wither = witherMap.remove(p);
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(wither.getId());
        Packets.send(p, packet);
    }

    /**
     * @param newTitle The new title of the bossbar
     * @param newProgress The new progress of the bossbar, from 0.0 to 1.0
     * */
    public void modify(String newTitle, float newProgress) {
        title = newTitle;
        progress = newProgress;

        for(Map.Entry<Player, EntityWither> entry : witherMap.entrySet()) {
            Player p = entry.getKey();
            if(!p.isOnline()) { witherMap.remove(p); continue; }    // Make sure player hasn't disconnected

            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(entry.getValue().getId(), getWatcher(witherMap.get(p)), true);
            Packets.send(p, packet);
        }
    }

    /**
     * FUCKING MAGIC
     * */
    private DataWatcher getWatcher(EntityWither wither) {
        DataWatcher watcher = new DataWatcher(null);
        watcher.a(0, (byte) 0b11111111);    // 0 = Flags, 5th bit = Invisible
        watcher.a(2, title);                // 2, 11 = Custom Name
        watcher.a(11, title);
        watcher.a(3, (byte) 1);             // 3, 12 = Show Name
        watcher.a(12, (byte) 1);
        watcher.a(4, (byte) 1);             // 4 = Silent
        watcher.a(5, (byte) 1<<5);      // Not sure what or why but my question is why not? why shouldnt i set id 5 to 0b00010000? WHO THE FUCK IS GONNA STOP ME FROM DOING IT HUH?! WHAT THE FUCK ARE YOU GONNA DO LITTLE BITCH BOY, NOTHINGS STOPPING ME FROM SETTING THIS ID TO THAT BYTE ASSHOLE. AND IF YOU THINK ITS IRRESPONSIBLE THEN GO FUCK YOURSELF BECAUSE THIS IS MY CODE MY RULES. MATTER OF FACT, IF THE CODE COULD SPEAK IT WOULD PROBABLY ASK WHY YOURE HERE BITCHING ABOUT IT INSTEAD OF MOVING ON WITH YOUR GODDAMN LIFE! haha sorry, always wanted to write a comment like this, from now on the comments will be normal again alright ^^
        watcher.a(6, progress * wither.getMaxHealth()); // 6 = Health
        watcher.a(17, 0);               // Make small
        watcher.a(18, 0);
        watcher.a(19, 0);
        watcher.a(20, 1000);
        return watcher;
    }

    public void destroy() {
        cancel();
        for(Player p : witherMap.keySet()) removePlayer(p);
    }

    /**
     * Updates the location of the withers for continuous display of the boss bar
     * */
    @Override
    public void run() {
        for(Map.Entry<Player, EntityWither> entry : witherMap.entrySet()) {
            Player p = entry.getKey();
            if(!p.isOnline()) { witherMap.remove(p); continue; }    // Make sure player hasn't disconnected

            EntityWither wither = entry.getValue();
            Location loc = p.getLocation().add(p.getLocation().getDirection().multiply(50));
            wither.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(wither);
            Packets.send(p, packet);
        }
    }
}
