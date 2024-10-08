package de.lolsu.gmfw.modules;

import de.lolsu.gmfw.main.Gamemode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class InvisArmour implements Listener {

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent e) {
        if(e.getItem().getType() != Material.POTION) return;

        // Check for invisibility in the potion and store it
        Potion pot = Potion.fromItemStack(e.getItem());
        PotionEffect invis = null;
        for(PotionEffect effect : pot.getEffects()) if(effect.getType() == PotionEffectType.INVISIBILITY) invis = effect;
        if(invis == null) return;

        // Get and store player armour and remove
        Player player = e.getPlayer();
        ItemStack[] armour = player.getInventory().getArmorContents();
        player.getInventory().setArmorContents(null);

        // Wait until potion has run out to give back the armour
        new BukkitRunnable() {
            @Override public void run() {
                // Check for online to avoid players disconnecting and causing errors :)
                if(player.isOnline()) player.getInventory().setArmorContents(armour);
            }
        }.runTaskLater(Gamemode.getInstance().getPlugin(), invis.getDuration());
    }

}
