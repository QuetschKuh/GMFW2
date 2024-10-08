package de.lolsu.gmfw.statistics;

import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.main.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class StatManager {

    /** Stat Folder Location */
    private static String SFL;
    private static Map<UUID, Map<String, Integer>> statsLocal = new HashMap<>();
    private static Map<UUID, Map<String, Integer>> statsGlobal = new HashMap<>();

    /**
     * Sets the location of the folder containing player statistics to the given path.<br>
     * Required for global stat loading and saving.
     * */
    public static void setStatFolderLocation(String path) {
        if(path == null || path.isEmpty()) {
            Logger.warn("Tried to set stat folder location to empty path");
            return;
        }

        File file = new File(path);
        if(file.exists() && !file.isDirectory()) {
            Logger.warn("Tried to set stat folder location to non-directory file");
            return;
        }

        if(!file.exists()) file.mkdirs();
        SFL = path;
    }

    /**
     * Loads the statistics of the player with the given UUID to {@link #statsGlobal}
     * */
    public static void loadFromFile(UUID uuid) {
        if (SFL == null) {
            Logger.warn("Tried to load global stats from file, but stat folder location hasn't been set!");
            return;
        }

        Map<String, Integer> map = new HashMap<>();

        File statFile = new File(SFL + uuid.toString());
        if (statFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(statFile.toPath())));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(":");
                    map.put(split[0], Integer.parseInt(split[1]));
                }
            } catch (IOException exception) {
                Logger.error("Failed to load statistics of UUID " + uuid.toString() + " from file.", exception);
            }
        }

        statsGlobal.put(uuid, map);
    }

    /**
     * Loads the statistics of the given player to {@link #statsGlobal}
     * */
    public static void loadFromFile(OfflinePlayer player) {
        loadFromFile(player.getUniqueId());
    }

    /**
     * Loads the statistics of the given players to {@link #statsGlobal}
     * */
    public static void loadFromFile(Collection<? extends OfflinePlayer> players) {
        for(OfflinePlayer player : players) loadFromFile(player.getUniqueId());
    }

    /**
     * Combines local and global stats and saves them to disk afterward
     * */
    public static void saveAndRemove(UUID uuid) {
        if(!statsGlobal.containsKey(uuid)) statsGlobal.put(uuid, new HashMap<>());

        // Transfer local data to global
        if(statsLocal.containsKey(uuid))
            for(Map.Entry<String, Integer> entry : statsLocal.get(uuid).entrySet()) {
                if(!statsGlobal.get(uuid).containsKey(entry.getKey())) statsGlobal.get(uuid).put(entry.getKey(), entry.getValue());
                else statsGlobal.get(uuid).put(entry.getKey(), statsGlobal.get(uuid).get(entry.getKey()) + entry.getValue());
            }

        // Write
        try {
            PrintWriter writer = new PrintWriter(SFL + uuid.toString());
            for (Map.Entry<String, Integer> entry : statsGlobal.get(uuid).entrySet())
                writer.println(entry.getKey() + ":" + entry.getValue());
            writer.close();
        } catch (IOException | SecurityException exception) {
            Logger.error("Failed to save statistics of UUID " + uuid.toString() + " to file.", exception);
        }
    }

    public static void saveAndRemove(OfflinePlayer player) {
        saveAndRemove(player.getUniqueId());
    }

    public static int getStat(UUID uuid, String statistic) {
        if(!statsLocal.containsKey(uuid)) statsLocal.put(uuid, new HashMap<>());
        return statsLocal.get(uuid).getOrDefault(statistic, 0);
    }

    public static int getStat(OfflinePlayer player, String statistic) {
        return getStat(player.getUniqueId(), statistic);
    }

    public static Map<String, Integer> getAll(UUID uuid) {
        if(!statsLocal.containsKey(uuid)) statsLocal.put(uuid, new HashMap<>());
        return statsLocal.get(uuid);
    }

    public static Map<String, Integer> getAll(OfflinePlayer player) {
        return getAll(player.getUniqueId());
    }

    public static int getGlobal(UUID uuid, String statistic) {
        if(!statsGlobal.containsKey(uuid)) return 0;
        return statsGlobal.get(uuid).getOrDefault(statistic, 0);
    }

    public static int getGlobal(OfflinePlayer player, String statistic) {
        return getGlobal(player.getUniqueId(), statistic);
    }

    public static Map<String, Integer> getAllGlobal(UUID uuid) {
        if(!statsGlobal.containsKey(uuid)) return new HashMap<>();
        return statsGlobal.get(uuid);
    }

    public static Map<String, Integer> getAllGlobal(OfflinePlayer player) {
        return getAllGlobal(player.getUniqueId());
    }

    public static void increase(UUID uuid, String statistic, int amount) {
        if(!statsLocal.containsKey(uuid)) statsLocal.put(uuid, new HashMap<>());
        statsLocal.get(uuid).put(statistic, statsLocal.get(uuid).getOrDefault(statistic, 0) + amount);
    }

    public static void increase(UUID uuid, String statistic) {
        increase(uuid, statistic, 1);
    }

    public static void increase(OfflinePlayer player, String statistic, int amount) {
        increase(player.getUniqueId(), statistic, amount);
    }

    public static void increase(OfflinePlayer player, String statistic) {
        increase(player.getUniqueId(), statistic, 1);
    }

    public static UUID getHighest(String statistic) {
        UUID current = null;
        int highest = 0;
        for(Map.Entry<UUID, Map<String, Integer>> stat : statsLocal.entrySet()) {
            if(highest > stat.getValue().getOrDefault(statistic, -1)) continue;
            highest = stat.getValue().get(statistic);
            current = stat.getKey();
        }
        if(current == null) current = Gamemode.getPlayerMan().getPlayers().get(0).getUniqueId();
        return current;
    }

}
