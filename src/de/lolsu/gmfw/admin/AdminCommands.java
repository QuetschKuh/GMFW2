package de.lolsu.gmfw.admin;

import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.managers.GamestateManager;
import de.lolsu.gmfw.phases.MapHandler;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class AdminCommands implements CommandExecutor {

    private static final String help =
            "/gmsetup setmode [setup/normal] – Restarts the server into either setup mode or normal operation.\n" +
            "/gmsetup [start/stop] – Starts and stops the countdown to the game, only available in normal mode.\n" +
                "=== Basics ===\n" +
            "/gmsetup – Opens the Setup Menu, entries with three dots will navigate to submenus and clicking on empty slots will navigate back\n" +
            "/gmsetup configurator {path} *[true/false] – Starts the location configurator for the given path. Optionally lets you select whether the path is a location list. Will save to the map config if you're in the map world\n" +
            "/gmsetup itemlist {path} – Lets you modify an item list in gm_config.yml by dropping/removing the items into/from the inventory\n" +
            "/gmsetup setvar {path} *{type} – Lets you modify a variable in gm_config.yml.\n" +
            "/gmsetup tp {world} – Teleports you to the given world, loads the world if it isn't yet.\n" +
                "=== Map Operators ===\n" +
            "/gmsetup loadmap {map} – Loads the given map.\n" +
            "/gmsetup unloadmap *[nosave] – Unloads the current map, also saves it by default.\n" +
            "/gmsetup createmap {map} – Creates a new map with the given name.\n" +
            "/gmsetup savemap – Saves the current map.\n" +
            "/gmsetup renamemap {map} {name} – Renames the given map to the given name.\n" +
            "/gmsetup delmap {map} – Deletes the given map, will ask for confirmation.\n";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        if(!sender.hasPermission("gmsetup")) return true;

        Player player = (Player) sender;

        if(args.length > 0 && Objects.equals(args[0], "setmode")) {
            if(args.length < 2) return error(player);
            if(!Objects.equals(args[1], "setup") && !Objects.equals(args[1], "normal")) return error(player, "This mode is not allowed");
            Gamemode.getPlugin().getConfig().set("setupmode", Objects.equals(args[1], "setup"));
            Gamemode.getPlugin().saveConfig();
            if(Gamemode.getStateMan() == null) new GamestateManager().onShutdown();
            else Gamemode.getStateMan().onShutdown();
            return true;
        }

        if(!Gamemode.getInstance().setupMode) {
            if(args.length > 0 && Objects.equals(args[0], "start")) {
                Gamemode.getStateMan().onCountdownQuick();
            } else if(args.length > 0 && Objects.equals(args[0], "stop")) {
                Gamemode.getStateMan().onCountdownCancel();
            } else player.sendMessage("Server not in setup mode. Enter setup mode using '/gmsetup setmode setup'");
            return true;
        }

        if(args.length == 0) {
            new AdminInventories((Player) sender);
            return true;
        }

        switch (args[0]) {
            case "configurator":
                if(args.length < 2) return error(player);
                new AdminConfigurator(player, args[1], args.length > 2 && args[2].equalsIgnoreCase("true"));
                break;

            case "itemlist":
                if(args.length < 2) return error(player);
                new AdminItemList(Gamemode.getConfig(), args[1], player);
                break;

            case "setvar":
                if(args.length < 2) return error(player);
                String path = args[1];
                new AdminInput(player, path, args.length > 2 ? args[2] : "Unknown", input -> {
                    Object value = input;
                    try {
                        value = Integer.parseInt(input);
                    } catch (NumberFormatException ignored) {}
                    Gamemode.getConfig().set(path, value);
                    Gamemode.getConfig().save();
                    player.sendMessage(ChatColor.RED + path + " set to " + value + ".");
                    player.sendMessage(ChatColor.RED + "Most variables require a server restart to take effect.");
                });
                break;

            case "tp":
                if(args.length < 2) return error(player);
                World world = Bukkit.getWorld(args[1]);
                if(world == null) world = MapHandler.loadWorld(args[1]);
                if (world == null) return error(player, "World couldn't be loaded");
                player.teleport(new Location(world, 0.5, 196, 0.5));
                break;

            case "loadmap":
                if(args.length < 2) return error(player);
                if(!MapHandler.mapsContain(args[1])) return error(player, "Map doesn't exist");
                if(MapHandler.getMap() != null) MapHandler.unloadMap();
                MapHandler.loadMap(args[1]);
                player.teleport(new Location(MapHandler.getWorld(), 0.5, 196, 0.5));
                Messenger.sendDev(player, ChatColor.GREEN + "Map loaded.");
                break;

            case "unloadmap":
                if(MapHandler.getMap() == null) return error(player, "There is no map to unload");
                boolean save = args.length < 2 || !args[1].equalsIgnoreCase("nosave");
                if(save) MapHandler.saveMap();
                MapHandler.unloadMap();
                Messenger.sendDev(player, ChatColor.GREEN + (save ? "Saved and unloaded map." : "Unloaded map without saving."));
                break;

            case "createmap":
                if(args.length < 2) return error(player);
                if(MapHandler.mapsContain(args[1])) return error(player, "Map name already exists");
                MapHandler.createMap(args[1]);
                player.teleport(new Location(MapHandler.getWorld(), 0.5, 196, 0.5));
                Messenger.sendDev(player, ChatColor.GREEN + "Map created.");
                break;

            case "savemap":
                if(MapHandler.getMap() == null) return error(player, "There is no map to save");
                MapHandler.saveMap();
                Messenger.sendDev(player, ChatColor.GREEN + "Map saved.");
                break;

            case "renamemap":
                if(args.length < 3) return error(player);
                if(!MapHandler.mapsContain(args[1])) return error(player, "Map doesn't exist");
                if(Objects.equals(MapHandler.getMap(), args[1])) return error(player, "Can't rename a loaded map");
                MapHandler.renameMap(args[1], args[2]);
                Messenger.sendDev(player, ChatColor.GREEN + "Renamed map.");
                break;

            case "delmap":
                if(args.length < 2) return error(player);
                if(Bukkit.getWorld(args[1]) == null && !MapHandler.mapsContain(args[1])) return error(player, "Map doesn't exist!");
                String map = args[1];
                new AdminInput(player, "Enter map name to confirm deletion", "String", input -> {
                    if(!Objects.equals(input, map)) return;
                    MapHandler.deleteMap(map);
                    player.sendMessage(ChatColor.RED + "Map deleted (" + input + ")");
                });
                break;

            default:
                player.sendMessage(ChatColor.RED + "Option doesn't exist!");
                player.sendMessage(help);
                break;
        }

        return true;
    }

    private boolean error(Player player) {
        player.sendMessage(ChatColor.RED + "Too few arguments!");
        player.sendMessage(help);
        return true;
    }

    /** Only exists to make 1-line if checks without brackets possible */
    private boolean error(Player player, String message) {
        player.sendMessage(ChatColor.RED + message);
        return true;
    }

    private boolean verify(Player player) {
        if(AdminConfigurator.isActive()) {
            player.sendMessage(ChatColor.RED + "Please exit the location configurator first. Do this by pressing [SNEAK]");
            return false;
        }
        return true;
    }

}
