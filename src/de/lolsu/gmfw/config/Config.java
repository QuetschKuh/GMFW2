package de.lolsu.gmfw.config;

import de.lolsu.gmfw.main.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** An extended interface for a Yaml Configuration based on {@link ConfigReader}. Supports write operations, saving, and full access with {@link #getFC()}.<br>
 * Further expands functionality with serialization of {@link Location locations} and location lists. */
public class Config extends ConfigReader {

    /** Gets the spigot implementation of the config (FileConfiguration) */
    public FileConfiguration getFC() { return config; }

    public Config(String path) {
        super(path);
    }

    public Config(String path, InputStream def) {
        super(path, def);
    }

    /** Sets the specified {@code path} to the given {@code value}.<br>
     * Keep in mind that most Bukkit Objects can't be serialized, including {@link Location Locations}, which is the reason for our own Location {@link #getLocation(String) getters} and {@link #setLocation(String, Location) setters}.<br>
     * {@link org.bukkit.inventory.ItemStack ItemStacks} however can be serialized.*/
    public void set(String path, Object value) {
        config.set(path, value);
    }

    /* Custom Getters/Setters */
    /**
     * Gets the string at the given path and converts it to a location
     * @param path The path of the location
     * @return The location at that path, with world null.<br>
     * If the location isn't found, 0.5 196 0.5 is returned instead.
     * */
    public Location getLocation(String path) {
        if(config.contains(path)) return ConfigUtil.stringToLocation(config.getString(path));
        else {
            Logger.warn("Tried to get location " + path + " from " + file.getName() + ", but doesn't exist.");
            return new Location(null, 0.5, 196, 0.5);
        }
    }

    /**
     * Gets the string at the given path and converts it to a location, setting the world of the location in the process
     * @param path The path of the location
     * @param world The world you would like the gotten location to have
     * @return The location at that path, with the world you assigned.<br>
     * If the location isn't found, 0.5 196 0.5 is returned instead.
     * */
    public Location getLocation(String path, World world) {
        if(config.contains(path)) return ConfigUtil.stringToLocation(config.getString(path), world);
        else {
            Logger.warn("Tried to get location " + path + " from " + file.getName() + ", but doesn't exist.");
            return new Location(world, 0.5, 196, 0.5);
        }
    }

    /**
     * Converts the given location into string format and stores it at the given path.<br>
     * Worlds aren't preserved when saved in this format!
     * @param path The path of the location
     * @param location The actual location you want to save
     * */
    public void setLocation(String path, Location location) {
        config.set(path, ConfigUtil.locationToString(location));
    }

    /**
     * Gets the string list at the given path and converts each string to a location, setting the world in the process
     * @param path The path of the location list
     * @param world The world you would like the locations to have, can be null
     * @return A list of locations, converted from the given path<br>
     * If the list isn't found, an empty list is returned
     * */
    public List<Location> getLocationList(String path, World world) {
        if(config.contains(path)) {
            List<String> stringList = config.getStringList(path);
            List<Location> locList = new ArrayList<>();
            for(String s : stringList) locList.add(ConfigUtil.stringToLocation(s, world));
            return locList;
        } else {
            Logger.warn("Tried to get location list " + path + " from " + file.getName() + ", but doesn't exist.");
            return new ArrayList<>();
        }
    }

    public void setLocationList(String path, List<Location> locationList) {
        List<String> stringList = new ArrayList<>();
        for(Location loc : locationList) stringList.add(ConfigUtil.locationToString(loc));
        config.set(path, stringList);
    }

    /**
     * Saves the currently loaded and possibly modified configuration back to the .yml file.
     * */
    public void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            Logger.error("Couldn't save config (" + file.getName() + ")", exception);
        }
    }

}
