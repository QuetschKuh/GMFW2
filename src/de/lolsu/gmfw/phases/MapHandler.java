package de.lolsu.gmfw.phases;

import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.config.Config;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.main.Logger;
import de.lolsu.gmfw.managers.PlayerManager;
import org.bukkit.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class MapHandler {

    private static boolean dirSet = false;

    private static String mapDirectory = "../maps/";
    private static String currentMap;
    private static World currentWorld;
    private static Config config;

    public static void setMapDirectory(String directory) {
        if(dirSet) Logger.warn("Set map directory multiple times");
        if(Files.notExists(Paths.get(directory))) new File(directory).mkdirs();
        mapDirectory = directory;
        dirSet = true;
    }

    public static String getMap() {
        return currentMap;
    }

    public static World getWorld() {
        return currentWorld;
    }

    /** Gets the map configuration, this is loaded once a map is chosen and contains data about the specific map like spawns, weather, and time */
    public static Config getConfig() {
        if(config == null) Logger.error("Tried to get map configuration, but is null", new NullPointerException());
        return config;
    }

    /*
    * World methods
    * */

    /** Creates an empty world (one without any blocks except for a spawn at 0.5, 128, 0.5) */
    public static World createEmpty(String name) {
        if(new File(name).exists()) {
            Logger.warn("Tried to create world, but already exists");
            return loadWorld(name);
        }

        World w = new WorldCreator(name).type(WorldType.FLAT).generatorSettings("2;0;1;").createWorld();
        w.getBlockAt(0, 127, 0).setType(Material.GLOWSTONE);
        w.setSpawnLocation(0, 128, 0);
        return w;
    }

    /** Loads the given world, doesn't create a world if there isn't one already
     * @return The loaded world or null if no world was found */
    public static World loadWorld(String world) {
        if(!new File(world).exists()) {
            Logger.warn("Tried to load world, but doesn't exist");
            return null;
        }

        if(Bukkit.getWorld(world) != null) {
            Logger.warn("Tried to load world, but is already loaded");
            return Bukkit.getWorld(world);
        }

        return new WorldCreator(world).createWorld();
    }

    /** Saves and unloads the given world after kicking all players */
    public static void unloadWorld(String world) {
        World w = Bukkit.getWorld(world);

        if(w == null) {
            Logger.warn("Tried to unload world, but isn't loaded");
            return;
        }

        PlayerManager.forAll(player -> {
            if (player.getWorld() == w) player.teleport(Gamemode.getInstance().spawnLocation);
        });

        Bukkit.unloadWorld(w, true);
    }

    /** Unloads and deletes the given world and folder */
    public static void deleteWorld(String world) {
        if(new File(world).exists()) {
            Logger.warn("Tried to delete world folder, but doesn't exist");
            return;
        }

        if(Bukkit.getWorld(world) != null)
            unloadWorld(world);

        try {
            deleteDirectory(world);
        } catch (IOException exception) {
            Logger.error("Failed to delete world folder", exception);
        }
    }

    /*
    * Map methods
    * */

    /** Gets all directory names from the map directory given in the configuration
     * @return All map names */
    public static String[] getMaps() {
        return new File(mapDirectory).list();
    }

    /** Checks whether the given map exists. Usually used as a value check */
    public static boolean mapsContain(String map) {
        return Arrays.asList(getMaps()).contains(map);
    }


    /** Saves and unloads the current map (if there is one) and loads the given one from the map folder.<br>
     * Basically {@link #unloadMap()} and {@link #loadMap(String)} in sequence with some extra checks.<br>
     * You should always use this instead of {@link #loadMap(String)} to avoid issues.
     * @param map The map you want to switch to
     * @param save Whether to save the previous map before unloading*/
    public static void switchMap(String map, boolean save) {
        if(!mapsContain(map)) {
            Logger.warn("Tried to switch to map '" + map + "', but doesn't exist");
            return;
        }

        if(currentMap != null) {
            if(save) saveMap();
            unloadMap();
        }

        loadMap(map);
    }

    /**
     * Creates a new map
     * */
    public static void createMap(String name) {
        currentWorld = createEmpty(name);
        currentMap = name;
        config = new Config(currentMap + "/gm_map.yml", Gamemode.getPlugin().getResource("gm_map.yml"));
        saveMap();
    }

    /**
     * Unloads and the deletes (!) the current world. Any progress not previously saved using {@link #saveMap()} will be lost!
     * */
    public static void unloadMap() {
        if(currentMap == null) {
            Logger.error("Tried to unload current map, but is null", new NullPointerException());
            return;
        }

        unloadWorld(currentMap);

        try {
            deleteDirectory(currentMap);
        } catch (IOException exception) {
            Logger.error("Failed to delete map folder while unloading (" + currentMap + ")", exception);
            return;
        }

        config = null;
        currentMap = null;
        currentWorld = null;

        Logger.info("Unloaded current map");
    }

    /**
     * Loads a map from the map directory given in the configuration.<br>
     * You should never use this on its own, always execute {@link #unloadMap()} beforehand or just use {@link #switchMap(String, boolean)} instead!
     * @param map The file name of the map to load
     * */
    public static void loadMap(String map) {
        if(!mapsContain(map)) {
            Logger.warn("Tried to load map '" + map + "', but doesn't exist");
            return;
        }

        if(new File(map).exists()) {
            if(Bukkit.getWorld(map) != null) unloadWorld(map);
            try {
                deleteDirectory(map);
            } catch (IOException exception) {
                Logger.error("Failed to delete already existing folder when trying to load map '" + map + "'", exception);
                return;
            }
        }

        try {
            copyDirectory(mapDirectory + map, map);
        } catch (IOException exception) {
            Logger.error("Failed to copy map directory " + mapDirectory + map, exception);
            return;
        }

        World world = loadWorld(map);

        if(world == null) {
            Logger.error("Failed to load map '" + map + "'");
            return;
        }

        currentMap = map;
        currentWorld = world;
        config = new Config(map + "/gm_map.yml");

        Logger.info("Loaded map '" + currentMap + "'");
    }

    /**
     * Saves the current map by saving its world and configuration
     * */
    public static void saveMap() {
        if(currentMap == null) {
            Logger.warn("Tried to save current map, but is null");
            return;
        }

        File mapInUse = new File(currentMap);

        if(!mapInUse.exists() || !mapInUse.isDirectory()) {
            Logger.warn("Tried to save current map, but directory doesn't exist");
            return;
        }

        File mapSaved = new File(mapDirectory + currentMap);

        // Delete the previous save
        if(mapSaved.exists()) {
            try {
                deleteDirectory(mapDirectory + currentMap);
            } catch (IOException exception) {
                Logger.error("Failed to delete previous map directory (" + currentMap + ")", exception);
                return;
            }
        }

        config.save();
        Bukkit.getWorld(currentMap).save();

        try {
            copyDirectory(mapInUse.getPath(), mapSaved.getPath());
        } catch (IOException exception) {
            Logger.error("Failed to copy current map to map directory (" + currentMap + ")", exception);
            return;
        }

        Logger.info("Saved current map ('" + currentMap + "')");
    }

    /** Deletes a map, no compromises */
    public static void deleteMap(String map) {
        if(Objects.equals(map, currentMap))
            unloadMap();

        try {
            deleteDirectory(mapDirectory + map);
        } catch (IOException exception) {
            Logger.error("Failed to delete map in map directory (" + map + ")", exception);
            return;
        }

        Logger.info("Deleted map '" + map + "'");
    }

    /** Simply renames a map from the map folder. Cannot rename a map currently in use */
    public static void renameMap(String map, String name) {
        if(Objects.equals(map, currentMap)) {
            Logger.warn("Tried to rename map currently in use");
            return;
        }

        File file = new File(mapDirectory + map);

        if(!file.exists()) {
            Logger.warn("Tried to rename map, but doesn't exist");
            return;
        }

        if(file.renameTo(new File(mapDirectory + name))) {
            Logger.info("Renamed map '" + map + "' to '" + name + "'");
        } else {
            Logger.error("Failed to rename map");
        }
    }

    /** Recursively deletes a folder. Does not accept absolute paths for safety. Tries for so long until its gone or an error occurs
     * @return Whether the deletion was successful */
    private static boolean deleteDirectory(String path) throws IOException {
        if(!new File(path).exists()) return false;
        if(Paths.get(path).isAbsolute()) return false;

        Files.walk(Paths.get(path)).map(Path::toFile).sorted(Comparator.reverseOrder()).forEach(File::delete);
        if(!new File(path).exists()) deleteDirectory(path);

        return true;
    }

    private static boolean copyDirectory(String pathSource, String pathDestination) throws IOException {
        if(new File(pathDestination).exists()) return false;

        Files.walk(Paths.get(pathSource)).forEach(src -> {
            Path dest = Paths.get(pathDestination, src.toString().substring(pathSource.length()));
            try {
                Files.copy(src, dest);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });

        return true;
    }

    /**
     * Should ONLY be called
     * */
    public static void cleanup() {
        if(currentMap != null) {
            Logger.warn("Tried to do cleanup, even though there is a map loaded");
            return;
        }
        for(String map : getMaps()) {
            File file = new File(map);
            if(file.exists() && file.isDirectory()) {
                try {
                    deleteDirectory(map);
                    Logger.info("Removed folder '" + map + "' in startup cleanup.");
                } catch (IOException exception) {
                    Logger.error("Failed to delete folder during cleanup");
                }
            }
        }
    }

}
