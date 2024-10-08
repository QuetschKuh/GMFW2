package de.lolsu.gmfw.admin;

import de.lolsu.gmfw.api.EasyAnvil;
import de.lolsu.gmfw.api.ItemStacks;
import de.lolsu.gmfw.events.Listeners;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class AdminInput implements Listener {

    public final Player player;
    public final String purpose;
    private final InputFunction function;

    /**
     * @param function The function to execute once the player has input something. Won't execute if the player exits out of the inventory
     * */
    public AdminInput(Player player, String purpose, String type, InputFunction function) {
        this.player = player;
        this.purpose = purpose;
        this.function = function;

        Listeners.registerLight(this);

        EasyAnvil anvil = new EasyAnvil(((CraftPlayer) player).getHandle());
        Inventory inventory = anvil.openInventory();
        inventory.setItem(0, ItemStacks.create(Material.PAPER, purpose + " (" + type + ")"));
    }

    public void destroy() {
        player.closeInventory();
        disable();
    }

    public void disable() {
        // Make sure the player doesn't spit out the paper on close
        player.getOpenInventory().getTopInventory().setItem(0, null);
        Listeners.removeLight(this);
    }

    @EventHandler
    public void onItemName(InventoryClickEvent event) {
        if(event.getWhoClicked() != player) return;
        event.setCancelled(true);
        if(event.getSlot() != 2) return;

        String input = event.getCurrentItem().getItemMeta().getDisplayName();

        if(!input.equalsIgnoreCase(purpose)) {
            function.onDone(input);
        }

        destroy();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getPlayer() != player) return;
        disable();
    }

    public interface InputFunction {
        void onDone(String input);
    }

}
