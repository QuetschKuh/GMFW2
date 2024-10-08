package de.lolsu.gmfw.admin;

import de.lolsu.gmfw.api.ItemStacks;
import de.lolsu.gmfw.api.Skulls;
import de.lolsu.gmfw.config.Config;
import de.lolsu.gmfw.events.Listeners;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.phases.MapHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AdminInventories implements Listener {

    /**
     * Stores constructors for the different inventories. Inventories are referenced by index. To view a full graph of default inventories and their indexes view file "inventory_indexes.md"
     * */
    public static List<InventoryCreator> inventories = new ArrayList<InventoryCreator>() {{
        /* 0: Main Menu */
        add(inv -> {
            inv.setItem(Material.WOOD, 10, "Configure Lobby...", $ -> inv.switchInventory(1));
            inv.setItem(Material.GRASS, 13, "Configure Game...", $ -> inv.switchInventory(2));
            inv.setItem(Material.GOLD_BLOCK, 16, "Configure Podium...", $ -> inv.switchInventory(3));
        });
        /* 1: Lobby */
        add(inv -> {
            // Spawn location
            inv.addLocation(Material.GLOWSTONE, Gamemode.getConfig(), "Lobby.Spawn", "Lobby");

            for(String var : new String[] { "Size.Total", "Size.Team", "Countdown.Length", "Countdown.ToStart", "Countdown.ToQuick"}) {
                inv.addItem(ItemStacks.create(Material.ANVIL, "Set variable: " + var, Gamemode.getConfig().getString(var)), $ -> inv.closeAndRun("gmsetup setvar " + var + " Integer"));
            }
        });
        /* 2: Game */
        add(inv -> {
            if(MapHandler.getMap() != null) {
                inv.setItem(ItemStacks.create(Material.BEDROCK, "Unload map", ChatColor.GREEN + "Left-click to save and unload", ChatColor.RED + "Shift + Right-click to unload without saving"), 24, click -> {
                    if(click.isLeftClick()) {
                        inv.closeAndRun("gmsetup unloadmap");
                    } else if(click == ClickType.SHIFT_RIGHT) {
                        inv.closeAndRun("gmsetup unloadmap nosave");
                    }
                });
                inv.setItem(Material.JUKEBOX, 25, "Save map", $ -> inv.runCommand("gmsetup savemap"));
                inv.setItem(ItemStacks.create(Material.ENDER_PORTAL_FRAME, "Teleport", "Some features are only available within the map world"), 26, $ -> inv.closeAndRun("gmsetup tp " + MapHandler.getMap()));

                // Default spawns
                if(Objects.equals(inv.player.getWorld(), MapHandler.getWorld())) {
                    inv.addItem(Material.ARMOR_STAND, "Player Spawns...", $ -> inv.switchInventory(new AdminLocationListCreator(MapHandler.getConfig(), "Spawns.Game", Material.ARMOR_STAND)));
                    inv.addLocation(Material.GLASS, MapHandler.getConfig(), "Spawns.Spec", MapHandler.getMap());
                    gameSettings.create(inv);
                    gameSettingsLoaded.create(inv);
                } else {
                    gameSettings.create(inv);
                }
            } else {
                inv.setItem(Material.EMPTY_MAP, 26, "Load Map...", $ -> inv.switchInventory(4));
                gameSettings.create(inv);
            }
        });
        /* 3: Podium */
        add(inv -> {
            inv.addLocation(Material.GLOWSTONE, Gamemode.getConfig(), "Podium.Spawn", "Podium");
            inv.addLocation(Material.ARMOR_STAND, Gamemode.getConfig(), "Podium.NPCs.Winners", "Podium");
            inv.addLocation(Material.ARMOR_STAND, Gamemode.getConfig(), "Podium.NPCs.Stat1", "Podium");
            inv.addLocation(Material.ARMOR_STAND, Gamemode.getConfig(), "Podium.NPCs.Stat2", "Podium");
            inv.addLocation(Material.ARMOR_STAND, Gamemode.getConfig(), "Podium.NPCs.Stat3", "Podium");
            inv.addLocation(Material.ARMOR_STAND, Gamemode.getConfig(), "Podium.Fireworks.1", "Podium");
            inv.addLocation(Material.ARMOR_STAND, Gamemode.getConfig(), "Podium.Fireworks.2", "Podium");
            inv.setItem(Material.ENDER_PORTAL_FRAME, 26, "Teleport", $ -> inv.closeAndRun("gmsetup tp Podium"));
        });
        /* 4: Map selection */
        add(inv -> {
            for(String map : MapHandler.getMaps()) {
                ItemStack item = ItemStacks.create(Material.EMPTY_MAP, map, ChatColor.GREEN + "Left-click to load and teleport", ChatColor.YELLOW + "Right-click to rename", ChatColor.DARK_RED + "Control + Drop to delete");
                inv.addItem(item, click -> {
                    if(click.isLeftClick()) {
                        inv.closeAndRun("gmsetup loadmap " + map);
                    } else if(click.isRightClick()) {
                        inv.destroy();
                        new AdminInput(inv.player, "Enter new name", "String", input -> inv.runCommand("gmsetup renamemap " + map + " " + input));
                    } else if(click == ClickType.CONTROL_DROP) {
                        inv.closeAndRun("gmsetup delmap " + map);
                    }
                });
            }

            inv.setItem(Skulls.Textures.GREEN_PLUS.getSkullItem("Create map"), 26, $ -> {
                inv.destroy();
                new AdminInput(inv.player, "Enter map name", "String", input -> inv.runCommand("gmsetup createmap " + input));
            });
        });
    }};

    /** Called after the game menu is loaded */
    public static InventoryCreator gameSettings = $ -> {};
    /** Additionally called after the game menu is loaded, if and only if there is a map loaded and the player is in the map world */
    public static InventoryCreator gameSettingsLoaded = $ -> {};

    public final Player player;
    private final Inventory inventory;

    private Stack<InventoryCreator> stack = new Stack<>();
    private InventoryAction[] actions = new InventoryAction[27];

    public AdminInventories(Player player) {
        this.player = player;

        Listeners.registerLight(this);

        inventory = Bukkit.createInventory(null, 27, ChatColor.RED + "Gamemode Configuration");
        player.openInventory(inventory);
        switchInventory(0);
    }

    public void destroy() {
        player.closeInventory();
        disable();
    }

    private void disable() {
        Listeners.removeLight(this);
    }

    @EventHandler
    public void clickInventory(InventoryClickEvent event) {
        if(event.getWhoClicked() != player) return;
        event.setCancelled(true);

        // Go back
        if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            lastInventory();
            return;
        }

        if(actions[event.getSlot()] != null) {
            actions[event.getSlot()].execute(event.getClick());
        }


    }

    @EventHandler
    public void closeInventory(InventoryCloseEvent event) {
        if(event.getPlayer() != player) return;

        disable();
    }

    /*
    * All following are functions used by InventoryCreator for a comprehensive experience
    * */

    public void closeAndRun(String command) {
        this.destroy();
        this.runCommand(command);
    }

    public void runCommand(String command) {
        player.performCommand(command);
    }

    public void addItem(Material material, String name, InventoryAction function) {
        setItem(material, inventory.firstEmpty(), name, function);
    }

    public void setItem(Material material, int slot, String name, InventoryAction function) {
        actions[slot] = function;
        inventory.setItem(slot, ItemStacks.create(material, name));
    }

    public void addItem(ItemStack item, InventoryAction function) {
        setItem(item, inventory.firstEmpty(), function);
    }

    public void setItem(ItemStack item, int slot, InventoryAction function) {
        actions[slot] = function;
        inventory.setItem(slot, item);
    }

    public void addLocation(Material material, Config config, String path, String world) {
        Location loc = config.contains(path) ? config.getLocation(path, Bukkit.getWorld(world)) : null;
        ItemStack stack = ItemStacks.create(
                material,
                "Set Location: " + path,
                loc == null ? "None" : loc.getX() + " " + loc.getY() + " " + loc.getZ(),
                ChatColor.GREEN + "Left-click to set.",
                loc == null ? "" : ChatColor.GREEN + "Right-click to teleport."
        );
        InventoryAction function = click -> {
            if(click.isLeftClick())
                this.closeAndRun("gmsetup configurator " + path + " false");
            else if (loc != null && click.isRightClick()) {
                this.destroy();
                if(loc.getWorld() == null) loc.setWorld(MapHandler.loadWorld(world));
                this.player.teleport(loc);
            }
        };
        addItem(stack, function);
    }

    public void switchInventory(int index) {
        clearInventory();
        stack.push(inventories.get(index)).create(this);
    }

    public void switchInventory(InventoryCreator creator) {
        clearInventory();
        stack.push(creator).create(this);
    }

    public void refreshInventory() {
        clearInventory();
        stack.peek().create(this);
    }

    public void lastInventory() {
        clearInventory();
        stack.pop();
        if(stack.empty()) destroy();
        else stack.peek().create(this);
    }

    private void clearInventory() {
        inventory.clear();
        actions = new InventoryAction[27];
    }

    public interface InventoryCreator {
        void create(AdminInventories inventory);
    }

    public interface InventoryAction {
        void execute(ClickType click);
    }

}
