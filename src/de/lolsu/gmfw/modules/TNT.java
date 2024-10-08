package de.lolsu.gmfw.modules;

import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.main.Logger;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import sun.nio.cs.HistoricallyNamedCharset;

import java.lang.reflect.Field;

public class TNT implements Listener {

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent e) {
        if(e.getBlock().getType() == Material.TNT) {
            e.getBlock().setType(Material.AIR);
            TNTPrimed tnt = (TNTPrimed) e.getBlock().getWorld().spawnEntity(e.getBlock().getLocation().add(0.5, 0.5, 0.5), EntityType.PRIMED_TNT);

            // Set the source of the tnt, so when somebody takes damage from it, we know which player was responsible
            EntityLiving source = ((CraftPlayer) e.getPlayer()).getHandle();
            EntityTNTPrimed nmsTNT = ((CraftTNTPrimed) tnt).getHandle();
            try {
                Field field = EntityTNTPrimed.class.getDeclaredField("source");
                field.setAccessible(true);
                field.set(nmsTNT, source);
            } catch (ReflectiveOperationException exception) {
                Logger.error("Failed to set source of TNT", exception);
            }

            // Set napalm metadata and set the fuse time to 2,5 seconds if the tnt was a napalm, if it was a normal tnt set the fuse time to 1,5 seconds
            if(e.getItemInHand().hasItemMeta() && e.getItemInHand().getItemMeta().hasDisplayName()
                    && e.getItemInHand().getItemMeta().getDisplayName().contains("Napalm")) {
                tnt.setMetadata("napalm", new FixedMetadataValue(Gamemode.getPlugin(), true));
                tnt.setFuseTicks(60);
            }
            else tnt.setFuseTicks(40);
        }
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent e) {
        e.setCancelled(true);
        boolean isNapalm = e.getEntity().hasMetadata("napalm");
        Location loc = e.getLocation();
        loc.getWorld().createExplosion(
                loc.getX(), loc.getY(), loc.getZ(),
                isNapalm ? 5f : 3f, isNapalm, true
        );
    }

}
