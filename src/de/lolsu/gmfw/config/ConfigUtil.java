package de.lolsu.gmfw.config;

import de.lolsu.gmfw.main.Logger;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Arrays;

/**
 * Utility for configuration, including converters between formats
 * */
public class ConfigUtil {
// All functions in here are "pure", meaning they all have a return value and don't use any variables other than the parameters given. This means we can safely have these static

    public static Location fallbackLocation = new Location(null, 0.5, 196, 0.5);

    /** Modifies a given String to be nicer to look at<br>
     * Especially good to convert programming stuff into user readable stuff
     * @param string The string to be modified (e.g. 'my-VERY_UglyString')
     * @return The modified/fancier string (e.g. 'My Very Ugly String')
     * */
    public static String fancy(String string) {
        return
                WordUtils.capitalizeFully(string        // Capitalize first letters
                        .replace("-", " ")      // Replace - and _ with spaces
                        .replace("_", " ")
                        .replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2")) // Add a space between capitalized words if there is none
                ;
    }

    /** Modifies a given Location to be smoother<br>
     * Snaps x,z to middle of block, y to nearest .5, yaw to nearest 45 and sets pitch to 0
     * @param location The location that should be modified (e.g. x: 5.291, y: 2.41, z: -2.744, yaw: 98.512, pitch: 14.213)
     * @return The modified/smoother location (e.g. x: 5.5, y: 2.5, z: -2.5, yaw: 90, pitch: 0)
     * */
    public static Location smoothLocation(Location location) {
        World w =  location.getWorld();
        double x = Math.floor(location.getX()) + 0.5;
        double y = (double) Math.round(location.getY() * 2) / 2;
        double z = Math.floor(location.getZ()) + 0.5;
        float yaw = Math.round(location.getYaw() / 45) * 45;
        float pitch = 0;
        return new Location(w, x, y, z, yaw, pitch);
    }

    /**
     * Converts a location into string format while discarding the world.<br>
     * Used in {@link Config#setLocation(String, Location)}.
     * @param location The location you want to convert
     * @return A string representation without the world
     * */
    public static String locationToString(Location location) {
        return location.getX() + ":" + location.getY() + ":" + location.getZ() + ":" +
                location.getYaw() + ":" + location.getPitch();
    }

    /**
     * Converts a string in location format into a location with world null.<br>
     * Used in {@link Config#getLocation(String)}.
     * @param string The string from which you want to parse a location
     * @return The location parsed from the string
     * */
    public static Location stringToLocation(String string) {
        return stringToLocation(string, null);
    }

    /**
     * Converts a string in location format into a location with the given world.<br>
     * Used in {@link Config#getLocation(String, World)}.
     * @param string The string from which you want to parse a location
     * @param world The world you would like to attach to the parsed location
     * @return The location parsed from the string with the given world
     * */
    public static Location stringToLocation(String string, World world) {
        try {
            double[] l = Arrays.stream(string.split(":")).mapToDouble(Double::parseDouble).toArray();
            return new Location(world, l[0], l[1], l[2], (float) l[3], (float) l[4]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException exception) {
            Logger.error("Failed to parse String '" + string + "' to location. Returning fallback location", exception);
            Location location = fallbackLocation.clone();
            location.setWorld(world);
            return location;
        }
    }

}
