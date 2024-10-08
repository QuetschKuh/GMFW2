package de.lolsu.gmfw.admin;

import de.lolsu.gmfw.api.ItemStacks;
import de.lolsu.gmfw.api.Skulls;
import de.lolsu.gmfw.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AdminLocationListCreator implements AdminInventories.InventoryCreator {
    private Config config;
    private String path;
    private Material material;

    public AdminLocationListCreator(Config config, String path, Material material) {
        this.config = config;
        this.path = path;
        this.material = material;
    }

    public void create(AdminInventories inventory) {
        int index = 0;
        for(Location location : config.getLocationList(path, null)) {
            ItemStack stack = ItemStacks.create(material, path + " " + index, location.getX() + " " + location.getY() + " " + location.getZ(), ChatColor.RED + "Right-click to remove.");
            final int fi = index;
            inventory.addItem(stack, click -> {
                if(!click.isRightClick()) return;
                List<Location> list = config.getLocationList(path, null);
                list.remove(fi);
                config.setLocationList(path, list);
                config.save();
                inventory.refreshInventory();
            });
            index++;
        }

        inventory.setItem(Skulls.Textures.GREEN_PLUS.getSkullItem("Add Locations"), 26, $ -> inventory.closeAndRun("gmsetup configurator " + path + " true"));
    }

}