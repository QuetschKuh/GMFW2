package de.lolsu.gmfw.modules;

import org.bukkit.DyeColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

public class AutoLapis implements Listener {

    public ItemStack lapis;

    public AutoLapis() {
        Dye dye = new Dye();
        dye.setColor(DyeColor.BLUE);
        lapis = dye.toItemStack(3);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if(e.getInventory() instanceof EnchantingInventory)
            e.getInventory().setItem(1, lapis);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getClickedInventory() instanceof EnchantingInventory && e.getSlot() == 1)
            e.setCancelled(true);
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent e) {
        e.getInventory().setItem(1, lapis);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if(e.getInventory() instanceof EnchantingInventory)
            e.getInventory().setItem(1, null);
    }

}
