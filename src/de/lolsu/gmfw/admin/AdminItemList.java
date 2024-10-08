package de.lolsu.gmfw.admin;

import de.lolsu.gmfw.config.Config;
import de.lolsu.gmfw.events.Listeners;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminItemList implements Listener {

    private final Config config;
    private final String path;
    private final Player player;

    public AdminItemList(Config config, String path, Player player) {
        this.config = config;
        this.path = path;
        this.player = player;

        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + "Item List: " + path);
        List<ItemStack> contents = (List<ItemStack>) config.get(path, new ArrayList<ItemStack>());
        inv.setContents(contents.toArray(new ItemStack[0]));

        player.openInventory(inv);

        Listeners.registerLight(this);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if(event.getPlayer() != player) return;

        config.set(path,
                Arrays.stream(event.getView().getTopInventory().getContents())
                        .filter(stack -> stack != null && stack.getType() != Material.AIR)
                        .collect(Collectors.toList())
        );
        config.save();

        Listeners.removeLight(this);
    }

}